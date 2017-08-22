package com.kumuluz.ee.samples.shipments.fressian;

import com.kumuluz.ee.samples.shipments.entity.Keyword;
import org.fressian.Reader;
import org.fressian.handlers.ILookup;
import org.fressian.handlers.ReadHandler;

import java.io.IOException;

/**
 * @author Matija Kljun
 */
public class KeywordReadHandler implements ReadHandler, ILookup<Object,ReadHandler> {

    @Override
    public Object read(Reader reader, Object tag, int componentCount) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        String ns = (String) reader.readObject();
        Object name = reader.readObject();
        if (ns != null && !ns.isEmpty()) {
            stringBuffer.append(ns);
            stringBuffer.append('/');
        }
        stringBuffer.append(name);
        return new Keyword(stringBuffer.toString());
    }

    @Override
    public ReadHandler valAt(Object s) {
        if (s.equals("key")) {
            return this;
        } else {
            return null;
        }
    }
}