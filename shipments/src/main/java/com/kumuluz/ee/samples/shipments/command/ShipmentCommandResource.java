package com.kumuluz.ee.samples.shipments.command;

import com.kumuluz.ee.samples.shipments.entity.Command;
import com.kumuluz.ee.samples.shipments.entity.Shipment;
import com.kumuluz.ee.samples.shipments.producer.CommandProducer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/shipments")
@RequestScoped
public class ShipmentCommandResource {

    @Inject
    private CommandProducer commandProducer;

    @POST
    public Response createShipment(Shipment o) {

        UUID commandUUID = UUID.randomUUID();

        Command command = new Command(commandUUID, "create-shipment", o);

        commandProducer.sendCommand(command);

        return Response.accepted().entity(commandUUID).build();
    }
}
