package com.kumuluz.ee.samples.catalogue.producer;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.samples.catalogue.entity.Command;
import com.kumuluz.ee.samples.catalogue.entity.Keyword;
import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
@ApplicationScoped
public class CommandProducer {

    private static final Logger log = LogManager.getLogger(CommandProducer.class.getName());

    @Inject
    @StreamProducer
    Producer<Object,Object> producer;

    private final String commandTopic = "commandsTopic";

    public void sendCommand(Command command) {

        ProducerRecord<Object,Object> record = new ProducerRecord<>( commandTopic, command.getId(), command.toMap());

        producer.send(record,
                (metadata, e) -> {
                    if(e != null) {
                        e.printStackTrace();
                    } else {
                        log.info("Command sent, offset of the command: " + metadata.offset());
                    }
                });
    }

    public void sendEvent(Map map, String eventTopic) {
        ProducerRecord<Object,Object> record = new ProducerRecord<>( eventTopic, map.get(new Keyword("id")), map);

        producer.send(record,
                (metadata, e) -> {
                    if(e != null) {
                        e.printStackTrace();
                    } else {
                        log.info("Event sent, offset of the event: " + metadata.offset());
                    }
                });
    }
}
