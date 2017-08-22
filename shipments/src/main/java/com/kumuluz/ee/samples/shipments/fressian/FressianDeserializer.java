package com.kumuluz.ee.samples.shipments.fressian;

import org.apache.kafka.common.serialization.Deserializer;
import org.fressian.FressianReader;
import org.fressian.Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Matija Kljun
 */
public class FressianDeserializer implements Deserializer<Object> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {}

    @Override
    public Object deserialize(String s, byte[] bytes) {
        Reader reader = new FressianReader(new ByteArrayInputStream(bytes), new KeywordReadHandler(), false);
        Object ret = null;
        try {
            ret = reader.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return ret;
    }

    @Override
    public void close() {}
}