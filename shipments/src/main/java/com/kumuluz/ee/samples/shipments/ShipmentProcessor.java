package com.kumuluz.ee.samples.shipments;

import com.kumuluz.ee.samples.shipments.entity.Keyword;
import com.kumuluz.ee.samples.shipments.fressian.FressianSerde;
import com.kumuluz.ee.streaming.common.annotations.StreamProcessor;
import com.kumuluz.ee.streaming.common.annotations.StreamProcessorController;
import com.kumuluz.ee.streaming.kafka.utils.streams.StreamsController;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
@ApplicationScoped
public class ShipmentProcessor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String commandsTopic = "commandsTopic";
    private String bookEventsTopic  = "bookEventsTopic";
    private String booksTopic = "booksTopic";
    private String shipmentsEventsTopic = "shipmentsEventsTopic";

    @Inject
    private ShipmentStore shipmentStore;

    @StreamProcessorController(id="shipments-processor")
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

    @StreamProcessor(id="shipments-processor", autoStart = false)
    public KStreamBuilder streamProcessorBuilder() {
        KStreamBuilder builder = new KStreamBuilder();

        Serde<UUID> keySerde = new FressianSerde();
        Serde<Map> valSerde = new FressianSerde();

        KStream<UUID, Map> shipmentEvents = builder.stream(keySerde, valSerde, shipmentsEventsTopic)
                .map((id, event) -> {
                    if(event.get(new Keyword("data")) != null) {
                        return new KeyValue<>((UUID) id, (Map) event.get(new Keyword("data")));
                    } else {//if (event.get(new Keyword("shipment")) != null) {
                        return new KeyValue<>((UUID) id, (Map) event.get(new Keyword("shipment")));
                    }
                });

        StateStoreSupplier store = Stores.create("Shipments")
                .withKeys(keySerde)
                .withValues(valSerde)
                .persistent()
                .build();

        builder.addStateStore(store);

        shipmentEvents.process(shipmentStore, "Shipments");

        return builder;
    }

}