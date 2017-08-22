package com.kumuluz.ee.samples.orders.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
public class Order implements CommandData {

    private UUID id;
    private UUID bookId;
    private Long amount;
    private OrderStatus status;
    private String info;
    private Long timestamp;

    public Order() {}

    public Order(UUID id, UUID bookId, Long amount, OrderStatus status, String info, Long timestamp) {
        this.id = id;
        this.bookId = bookId;
        this.amount = amount;
        this.status = status;
        this.info = info;
        this.timestamp = timestamp;
    }

    public Order(Map<Keyword, Object> params) {
        this((UUID) params.get(new Keyword("id")),
                (UUID) params.get(new Keyword("bookId")),
                (Long) params.get(new Keyword("amount")),
                OrderStatus.valueOf((String)params.get(new Keyword("status"))),
                (String) params.get(new Keyword("info")),
                (Long) params.get(new Keyword("timestamp")));
    }

    @Override
    public Map toMap() {
        Map<Keyword, Object> orderMap = new HashMap<>();

        if(this.getId() != null) orderMap.put(new Keyword("id"), this.getId());
        orderMap.put(new Keyword("bookId"), this.getBookId());
        orderMap.put(new Keyword("amount"), this.getAmount());
        if(this.getStatus() != null) orderMap.put(new Keyword("status"), this.getStatus().toString());
        if(this.getInfo() != null) orderMap.put(new Keyword("info"), this.getInfo());
        if(this.getTimestamp() != null) orderMap.put(new Keyword("timestamp"), this.getTimestamp());

        return orderMap;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public enum OrderStatus {
        PLACED("PLACED"),
        COMPLETED("COMPLETED"),
        CANCELLED("CANCELLED");

        private final String name;

        private OrderStatus(String s) {
            name = s;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
