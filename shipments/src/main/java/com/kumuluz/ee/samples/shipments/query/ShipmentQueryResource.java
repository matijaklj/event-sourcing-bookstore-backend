package com.kumuluz.ee.samples.shipments.query;

import com.kumuluz.ee.samples.shipments.ShipmentStore;
import com.kumuluz.ee.samples.shipments.entity.Shipment;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Matija Kljun
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/shipments")
@RequestScoped
public class ShipmentQueryResource {

    @Inject
    private ShipmentStore shipmentStore;

    @GET
    public Response getShipments() {
        List<Shipment> shipmentList = shipmentStore.getShipments();

        return Response.ok().entity(shipmentList).build();
    }

    @GET
    public Response getShipmentsForBook(@QueryParam("bookId") UUID bookId) {

        List<Shipment> shipments = shipmentStore.getShipments();

        List<Shipment> bookShipments = shipments.stream()
                .filter(shipment -> shipment.getBookId() == bookId)
                .collect(Collectors.toList());

        return Response.ok().entity(bookShipments).build();
    }

    @GET
    @Path("/{id}")
    public Response getShipment(@PathParam("id") UUID id) {
        Shipment shipment = shipmentStore.getShipment(id);

        if(shipment == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.ok().entity(shipment).build();
    }
}
