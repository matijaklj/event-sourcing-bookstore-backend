package com.kumuluz.ee.samples.catalogue;

import com.kumuluz.ee.samples.catalogue.entity.Book;
import com.kumuluz.ee.samples.catalogue.entity.Keyword;
import com.kumuluz.ee.samples.catalogue.producer.CommandProducer;
import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * @author Matija Kljun
 */
@ApplicationScoped
public class BookStore implements TransformerSupplier<UUID, Map, KeyValue<UUID, Map>>, Transformer<UUID, Map, KeyValue<UUID, Map>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProcessorContext context;
    private KeyValueStore<UUID, Map> store;

    private String shipmentsEventsTopic = "shipmentsEventsTopic";

    public List<Book> getBooks() {
        List<Book> books = new ArrayList<>();

        if(store == null)
            return books;

        KeyValueIterator<UUID, Map> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<UUID, Map> entry = iterator.next();
            logger.debug("getCustomers iterator entry: {}", entry);
            books.add(new Book(entry.value));
        }
        iterator.close();
        return books;
    }

    public Book getBook(UUID id) {
        Map map = store.get(id);
        if(map != null)
            return new Book(store.get(id));
        else return null;
    }

    @Override
    public void init(ProcessorContext processorContext) {
        this.context = processorContext;
        this.store = (KeyValueStore<UUID, Map>) context.getStateStore("Books");
    }

    @Override
    public KeyValue<UUID, Map> transform(UUID uuid, Map map) {
        logger.info("BookStore.process(UUID {}, Map {})", uuid, map);
        Keyword action = (Keyword) map.get(new Keyword("action"));

        if(action.equals(new Keyword("book-deleted"))) {
            logger.info("Deleting book UUID {}", uuid);
            store.delete(uuid);
        } else if(action.equals(new Keyword("book-created"))) {
            logger.info("Saving Book: UUID {}, Map {}", uuid, map);
            store.put(uuid, (Map) map.get(new Keyword("book")));
        } else if(action.equals(new Keyword("shipment-created"))) {
            logger.info("New Shipment: UUID {}, Map {}", uuid, map);

            Map shipment = (Map) map.get(new Keyword("shipment"));

            // check if id is in the store
            Map book = store.get((UUID) shipment.get(new Keyword("bookId")));

            if(book == null) {
                // poslji event shipment CANCELLED
                logger.info("Shipment ERROR, no book with bookID: {}", shipment.get(new Keyword("bookId")));
                shipment.put(new Keyword("status"), "CANCELLED");
                shipment.put(new Keyword("info"), "There is no book with the provided bookId.");

                return new KeyValue<>((UUID)shipment.get(new Keyword("id")), map);

            } else {
                // poslji event shipment COMPLETED
                Long amount = (Long) book.get(new Keyword("amount"));
                amount += (Long) shipment.get(new Keyword("amount"));

                book.put(new Keyword("amount"), amount);

                logger.info("updated book amount to {}", amount);

                store.put((UUID) shipment.get(new Keyword("bookId")), book);

                shipment.put(new Keyword("status"), "COMPLETED");
                shipment.put(new Keyword("info"), "Shipment completed, new amount of books: " + amount.toString());

                return new KeyValue<>((UUID)shipment.get(new Keyword("id")), map);
            }
        } else if(action.equals(new Keyword("order-created"))) {
            logger.info("New Order: UUID {}, Map {}", uuid, map);

            Map order = (Map) map.get(new Keyword("data"));
            UUID bookId = (UUID) order.get(new Keyword("bookId"));
            Map book = store.get(bookId);

            if(book == null) {
                // Book don't exist, order is CANCELLED
                order.put(new Keyword("status"), "CANCELLED");
                order.put(new Keyword("info"), "There is no book with the provided bookId.");
                order.put(new Keyword("timestamp"), new Date().getTime());

                return new KeyValue<>((UUID) order.get(new Keyword("id")), map);
            } else {
                // check if there are enough books in the bookstore
                Long orderAmount = (Long) order.get(new Keyword("amount"));
                Long bookAmount = (Long) book.get(new Keyword("amount"));

                if(bookAmount < orderAmount) {
                    // order is CANCELLED
                    order.put(new Keyword("status"), "CANCELLED");
                    order.put(new Keyword("info"), "There is not enough books for the order.");
                    order.put(new Keyword("timestamp"), new Date().getTime());

                    return new KeyValue<>((UUID) order.get(new Keyword("id")), map);
                } else {
                    // update book amount and Order is COMPLETED
                    bookAmount -= orderAmount;

                    book.put(new Keyword("amount"), bookAmount);
                    store.put(bookId, book);

                    order.put(new Keyword("status"), "COMPLETED");
                    order.put(new Keyword("info"), "Order completed, new amount of books: " + bookAmount);
                    order.put(new Keyword("timestamp"), new Date().getTime());

                    return new KeyValue<>((UUID) order.get(new Keyword("id")), map);
                }
            }
        }

        return null;
    }

    @Override
    public KeyValue<UUID, Map> punctuate(long l) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public Transformer<UUID, Map, KeyValue<UUID, Map>> get() {
        return this;
    }


}