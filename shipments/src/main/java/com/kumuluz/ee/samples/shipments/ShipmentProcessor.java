package com.kumuluz.ee.samples.shipments;

import com.kumuluz.ee.samples.shipments.entity.Keyword;
import com.kumuluz.ee.samples.shipments.fressian.FressianSerde;
import com.kumuluz.ee.streaming.common.annotations.StreamListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
@ApplicationScoped
public class ShipmentProcessor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private KafkaStreams kafkaStreams;
    private String commandsTopic;
    private String bookEventsTopic;
    private String booksTopic;
    private String shipmentsEventsTopic;

    //@Resource
    @Inject
    private ShipmentStore shipmentStore;
/*
    @StreamListener(topics = {shipmentsEventsTopic})
    public void onMessage(ConsumerRecord<Object,Object> record) {
        log.info("Shipment events key {}, value {}", record.key(), record.value());

        Map map = (Map) record.value();

        if(map.get(new Keyword("data")) != null) {
            shipmentStore.process((UUID) record.key(), (Map) map.get(new Keyword("data")));
        } else if (map.get(new Keyword("shipment")) != null) {
            shipmentStore.process((UUID) record.key(), (Map) map.get(new Keyword("shipment")));
        }
    }
*/
    private StreamsConfig kafkaStreamsConfig;

    public void kafkaStreamsConfig() {
        this.commandsTopic = "commandsTopic";
        this.bookEventsTopic = "bookEventsTopic";
        this.booksTopic = "booksTopic";
        this.shipmentsEventsTopic = "shipmentsEventsTopic";
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "shipment-processor-1");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        this.setKafkaStreamsConfig(new StreamsConfig(props));
    }

    public StreamsConfig getKafkaStreamsConfig() {
        return kafkaStreamsConfig;
    }

    public void setKafkaStreamsConfig(StreamsConfig kafkaStreamsConfig) {
        this.kafkaStreamsConfig = kafkaStreamsConfig;
    }

    public void startStream(@Observes @Initialized(ApplicationScoped.class) Object init) {
        this.kafkaStreamsConfig();
        this.startStreamProcessor();
    }

    public void destroyStream(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        this.stopStreamProcessor();
    }

    public void startStreamProcessor() {
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

        this.kafkaStreams = new KafkaStreams(builder, kafkaStreamsConfig);
        this.kafkaStreams.start();
    }

    public void stopStreamProcessor() {
        this.kafkaStreams.close();
    }

}