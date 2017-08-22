package com.kumuluz.ee.samples.orders;

import com.kumuluz.ee.samples.orders.entity.Order;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
@ApplicationScoped
public class OrderStore implements ProcessorSupplier<UUID, Map>, Processor<UUID, Map> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProcessorContext context;
    private KeyValueStore<UUID, Map> store;

    public List<Order> getOrders() {
        List<Order> orders = new ArrayList<>();

        if(store == null)
            return orders;

        KeyValueIterator<UUID, Map> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<UUID, Map> entry = iterator.next();
            logger.debug("getOrders iterator entry: {}", entry);
            orders.add(new Order(entry.value));
        }
        iterator.close();
        return orders;
    }

    public Order getOrder(UUID id) {
        Map map = store.get(id);
        if(map != null)
            return new Order(store.get(id));
        else return null;
    }

    @Override
    public void init(ProcessorContext processorContext) {
        this.context = processorContext;
        this.store = (KeyValueStore<UUID, Map>) context.getStateStore("Orders");
    }

    @Override
    public void process(UUID uuid, Map map) {
        logger.info("OrderStore.process(UUID {}, Map {})", uuid, map);
        store.put(uuid, map);

    }

    @Override
    public void punctuate(long l) {

    }

    @Override
    public void close() {

    }

    @Override
    public Processor<UUID, Map> get() {
        return this;
    }

    public void setStore(KeyValueStore<UUID, Map> store) {
        this.store = store;
    }
}