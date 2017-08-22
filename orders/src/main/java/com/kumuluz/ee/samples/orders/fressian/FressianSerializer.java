package com.kumuluz.ee.samples.orders.fressian;

import org.apache.kafka.common.serialization.Serializer;
import org.fressian.FressianWriter;
import org.fressian.Writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Matija Kljun
 */
public class FressianSerializer implements Serializer<Object> {

    @Override
    public void configure(Map<String, ?> map, boolean b) {}

    @Override
    public byte[] serialize(String s, Object o) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new FressianWriter(outputStream, new KeywordWriteHandler());
        try {
            writer.writeObject(o);
            writer.writeFooter();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    @Override
    public void close() {}
}