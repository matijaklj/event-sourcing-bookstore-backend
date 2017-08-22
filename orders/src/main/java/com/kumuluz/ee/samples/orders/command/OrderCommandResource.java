package com.kumuluz.ee.samples.orders.command;

import com.kumuluz.ee.samples.orders.entity.Command;
import com.kumuluz.ee.samples.orders.entity.Order;
import com.kumuluz.ee.samples.orders.producer.CommandProducer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/orders")
@RequestScoped
public class OrderCommandResource {

    @Inject
    private CommandProducer commandProducer;

    @POST
    public Response createrOrder(Order o) {

        o.setTimestamp(new Date().getTime());

        UUID orderId = UUID.randomUUID();
        Command command = new Command(orderId, "create-order", o);

        commandProducer.sendCommand(command);

        return Response.accepted().entity(orderId).build();
    }
}
