package com.kumuluz.ee.samples.shipments;

import com.kumuluz.ee.samples.shipments.entity.Shipment;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.StateStore;
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
public class ShipmentStore implements ProcessorSupplier<UUID, Map>, Processor<UUID, Map> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProcessorContext context;
    private KeyValueStore<UUID, Map> store;

    public List<Shipment> getShipments() {
        List<Shipment> shipments = new ArrayList<>();

        if(store == null)
            return shipments;

        KeyValueIterator<UUID, Map> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<UUID, Map> entry = iterator.next();
            logger.debug("getShipments iterator entry: {}", entry);
            shipments.add(new Shipment(entry.value));
        }
        iterator.close();
        return shipments;
    }

    public Shipment getShipment(UUID id) {
        Map map = store.get(id);
        if(map != null)
            return new Shipment(store.get(id));
        else return null;
    }

    @Override
    public void init(ProcessorContext processorContext) {
        this.context = processorContext;
        this.store = (KeyValueStore<UUID, Map>) context.getStateStore("Shipments");
    }

    @Override
    public void process(UUID uuid, Map map) {
        logger.info("BookStore.process(UUID {}, Map {})", uuid, map);
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