package com.kumuluz.ee.samples.shipments.producer;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.samples.shipments.entity.Command;
import com.kumuluz.ee.streaming.common.annotations.StreamListener;
import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

        log.info("sent command id {}",  command.getId());

        producer.send(record,
                (metadata, e) -> {
                    if(e != null) {
                        e.printStackTrace();
                    } else {
                        log.info("Command sent, offset of the command: " + metadata.offset());
                    }
                });
    }

}
