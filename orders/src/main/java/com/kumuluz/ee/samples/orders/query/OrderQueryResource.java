package com.kumuluz.ee.samples.orders.query;

import com.kumuluz.ee.samples.orders.OrderStore;
import com.kumuluz.ee.samples.orders.entity.Order;
import com.sun.org.apache.xpath.internal.operations.Or;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Matija Kljun
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/orders")
@RequestScoped
public class OrderQueryResource {

    @Inject
    private OrderStore orderStore;

    @GET
    public Response getOrders() {

        List<Order> orders = orderStore.getOrders();

        return Response.ok().entity(orders).build();
    }

    @GET
    @Path("/book")
    public Response getOrdersForBook(@QueryParam("bookId") UUID bookId) {

        List<Order> orders = orderStore.getOrders();

        List<Order> bookOrders = orders.stream()
                .filter(order -> order.getBookId() == bookId)
                .collect(Collectors.toList());

        return Response.ok().entity(bookOrders).build();
    }

    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") UUID id) {

        Order order = orderStore.getOrder(id);

        if(order == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.ok().entity(order).build();
    }
}
