package com.kumuluz.ee.samples.orders.fressian;

import com.kumuluz.ee.samples.orders.entity.Keyword;
import org.fressian.Writer;
import org.fressian.handlers.ILookup;
import org.fressian.handlers.WriteHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matija Kljun
 */
public class KeywordWriteHandler implements WriteHandler, ILookup<Class, Map<String, WriteHandler>> {

    @Override
    public void write(Writer writer, Object o) throws IOException {
        Keyword keyword = (Keyword) o;
        writer.writeTag("key", 2);
        writer.writeObject(keyword.getNamespace(), true);
        writer.writeObject(keyword.getName(), true);
    }

    @Override
    public Map<String, WriteHandler> valAt(Class aClass) {
        if (aClass.equals(Keyword.class)) {
            Map handlerMap = new HashMap();
            handlerMap.put("key", this);
            return handlerMap;
        } else {
            return null;
        }
    }
}
