
package com.kumuluz.ee.samples.catalogue;

import com.kumuluz.ee.samples.catalogue.entity.Keyword;
import com.kumuluz.ee.samples.catalogue.fressian.FressianSerde;
import com.kumuluz.ee.streaming.common.annotations.StreamProcessor;
import com.kumuluz.ee.streaming.common.annotations.StreamProcessorController;
import com.kumuluz.ee.streaming.kafka.utils.streams.StreamsController;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class CommandProcessor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String commandsTopic = "commandsTopic";
    private String bookEventsTopic = "bookEventsTopic";
    private String booksTopic = "booksTopic";
    private String shipmentsEventsTopic = "shipmentsEventsTopic";
    private String ordersEventsTopic = "ordersEventsTopic";

    @Inject
    private BookStore bookStore;

    @StreamProcessorController(id = "catalogue-stream-processor")
    private StreamsController streams;

    public void startStream(@Observes @Initialized(ApplicationScoped.class) Object init) {

        Runtime.getRuntime().addShutdownHook(new Thread("streams-catalogue-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
            }
        });

        streams.start();
    }

    @StreamProcessor(id = "catalogue-stream-processor", autoStart = false)
    public KStreamBuilder streamProcessorBuilder() {

        KStreamBuilder builder = new KStreamBuilder();

        Serde<UUID> keySerde = new FressianSerde();
        Serde<Map> valSerde = new FressianSerde();

        KStream<UUID, Map> commands = builder.stream(keySerde, valSerde, commandsTopic);

        KStream<UUID, Map> booksEvents = commands
                .filter((id, command) -> command.get(new Keyword("action")).equals("create-book") ||
                                         command.get(new Keyword("action")).equals("delete-book") ||
                                         command.get(new Keyword("action")).equals("create-shipment") ||
                                         command.get(new Keyword("action")).equals("create-order"))
                .map((id, command) -> {
                    if (command.get(new Keyword("action")).equals("create-book")) {
                        log.info("New book command received");
                        Map<Object, Object> bookEvent = new HashMap<Object, Object>(command);
                        bookEvent.put(new Keyword("action"), new Keyword("book-created"));
                        bookEvent.put(new Keyword("parent"), id);
                        Map<Object, Object> bookValue = (Map) bookEvent.get(new Keyword("data"));
                        bookValue.put(new Keyword("id"), UUID.randomUUID());
                        bookValue.put(new Keyword("amount"), 0);
                        return new KeyValue<>(UUID.randomUUID(), bookEvent);
                    } else if (command.get(new Keyword("action")).equals("delete-book")) {
                        log.info("Delete book command received");
                        Map<Object, Object> deleteBookEvent = new HashMap<Object, Object>(command);
                        deleteBookEvent.put(new Keyword("action"), new Keyword("book-deleted"));
                        deleteBookEvent.put(new Keyword("parent"), id);
                        return new KeyValue<>(UUID.randomUUID(), deleteBookEvent);
                    } else if (command.get(new Keyword("action")).equals("create-shipment")) { // "create-shipment"
                        log.info("New shipment command received");
                        Map<Object, Object> shipmentEvent = new HashMap<Object, Object>(command);
                        shipmentEvent.put(new Keyword("action"), new Keyword("shipment-created"));
                        shipmentEvent.put(new Keyword("parent"), id);
                        Map<Object, Object> shipmentValue = (Map) shipmentEvent.get(new Keyword("data"));
                        shipmentValue.put(new Keyword("id"), id);
                        shipmentValue.put(new Keyword("status"), "PLACED");
                        return new KeyValue<>(id, shipmentEvent);
                    } else {//create-order
                        log.info("New order command received");
                        Map<Object, Object> orderEvent = new HashMap<Object, Object>(command);
                        orderEvent.put(new Keyword("action"), new Keyword("order-created"));
                        orderEvent.put(new Keyword("parent"), id);
                        Map<Object, Object> orderValue = (Map) orderEvent.get(new Keyword("data"));
                        orderValue.put(new Keyword("id"), id);
                        orderValue.put(new Keyword("status"), "PLACED");
                        return new KeyValue<>(id, orderEvent);
                    }
                });

        booksEvents.filter((id, command) -> command.get(new Keyword("action")).equals(new Keyword("create-book")) ||
                                            command.get(new Keyword("action")).equals(new Keyword("delete-book")))
                .through(keySerde, valSerde, bookEventsTopic);

        booksEvents.filter((id, command) -> command.get(new Keyword("action")).equals(new Keyword("shipment-created")))
                .through(keySerde, valSerde, shipmentsEventsTopic);

        booksEvents.filter((id, command) -> command.get(new Keyword("action")).equals(new Keyword("order-created")))
                .through(keySerde, valSerde, ordersEventsTopic);

        // TODO vrzi to vn, nepotreben se en korak ??
        KStream<UUID, Map> books = booksEvents
                .map((id, event) -> {
                    log.info("New book event {}", event);
                    Map bookEvent = new HashMap();
                    Map data = (Map) event.get(new Keyword("data"));
                    UUID bookId = (UUID) data.get(new Keyword("id"));
                    bookEvent.put(new Keyword("action"), event.get(new Keyword("action")));
                    if (event.get(new Keyword("action")).equals(new Keyword("book-deleted"))) {
                        bookEvent.put(new Keyword("book"), null);
                        return new KeyValue<>(bookId, bookEvent);
                    } else if (event.get(new Keyword("action")).equals(new Keyword("book-created"))) {
                        bookEvent.put(new Keyword("book"), data);
                        return new KeyValue<>(bookId, bookEvent);
                    } else if (event.get(new Keyword("action")).equals(new Keyword("shipment-created"))) {
                        bookEvent.put(new Keyword("shipment"), data);
                        return new KeyValue<>(bookId, bookEvent);
                    } else {
                        bookEvent.put(new Keyword("data"), data);
                        return new KeyValue<>(bookId, event);
                    }
                });

        books.through(keySerde, valSerde, booksTopic);

        StateStoreSupplier store = Stores.create("Books")
                .withKeys(keySerde)
                .withValues(valSerde)
                .persistent()
                .build();

        builder.addStateStore(store);

        //books.transform(bookStore, "Books");
        KStream<UUID, Map> responseEvents = books.transform((TransformerSupplier) bookStore, "Books");

        // filter return stream according to action

        responseEvents
                .filter((id, event) -> event.get(new Keyword("action")).equals(new Keyword("shipment-created")))
                .to(keySerde, valSerde, shipmentsEventsTopic);

        responseEvents
                .filter((id, event) -> event.get(new Keyword("action")).equals(new Keyword("order-created")))
                .to(keySerde, valSerde, ordersEventsTopic);

        /* trenutno se to ne uporablja
        responseEvents
                .filter((id, event) -> event.get(new Keyword("action")).equals(new Keyword("create-book")) ||
                                       event.get(new Keyword("action")).equals(new Keyword("delete-book")))
                .to(keySerde, valSerde, bookEventsTopic);
         */
        return builder;
    }

}
