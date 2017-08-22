package com.kumuluz.ee.samples.orders;

import com.kumuluz.ee.samples.orders.entity.Keyword;
import com.kumuluz.ee.samples.orders.fressian.FressianSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.StateStoreSupplier;
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
public class OrderProcessor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private KafkaStreams kafkaStreams;

    private String ordersEventsTopic;

    @Inject
    private OrderStore ordersStore;

    private StreamsConfig kafkaStreamsConfig;

    public void kafkaStreamsConfig() {
        this.ordersEventsTopic = "ordersEventsTopic";
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "order-processor");
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

        KStream<UUID, Map> shipmentEvents = builder.stream(keySerde, valSerde, ordersEventsTopic)
                .map((id, event) -> new KeyValue<>((UUID) id, (Map) event.get(new Keyword("data"))));

        StateStoreSupplier store = Stores.create("Orders")
                .withKeys(keySerde)
                .withValues(valSerde)
                .persistent()
                .build();

        builder.addStateStore(store);

        shipmentEvents.process(ordersStore, "Orders");

        this.kafkaStreams = new KafkaStreams(builder, kafkaStreamsConfig);
        this.kafkaStreams.start();
    }

    public void stopStreamProcessor() {
        this.kafkaStreams.close();
    }

}