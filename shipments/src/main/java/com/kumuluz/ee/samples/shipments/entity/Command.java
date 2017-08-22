package com.kumuluz.ee.samples.shipments.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
public class Command {

    private UUID id;
    private String action;
    private CommandData data;

    public Command(UUID id, String action, CommandData data) {
        this.id = id;
        this.action = action;
        this.data = data;
    }

    public Map<Keyword, Object> toMap() {
        Map<Keyword, Object> commandMap = new HashMap<Keyword, Object>();

        commandMap.put(new Keyword("id"), this.getId());
        commandMap.put(new Keyword("action"), this.getAction());
        commandMap.put(new Keyword("data"), this.getData().toMap());

        return commandMap;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public CommandData getData() {
        return data;
    }

    public void setData(CommandData data) {
        this.data = data;
    }
}
