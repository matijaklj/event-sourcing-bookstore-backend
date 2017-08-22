package com.kumuluz.ee.samples.catalogue.command;

import com.kumuluz.ee.samples.catalogue.BookStore;
import com.kumuluz.ee.samples.catalogue.entity.Book;
import com.kumuluz.ee.samples.catalogue.entity.Command;
import com.kumuluz.ee.samples.catalogue.producer.CommandProducer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * @author Matija Kljun
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/books")
@RequestScoped
public class CatalogueCommandResource {

    @Inject
    private CommandProducer commandProducer;

    @Inject
    private BookStore bookStore;

    @POST
    public Response createBook(Book b) {

        UUID commandId = UUID.randomUUID();
        Command command = new Command(commandId, "create-book", b);

        commandProducer.sendCommand(command);

        return Response.status(202).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") UUID id) {

        Book book = this.bookStore.getBook(id);

        if(book == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        Command command = new Command(UUID.randomUUID(), "delete-book", book);

        // send to kafka topic : commandsTopic
        commandProducer.sendCommand(command);

        return Response.status(202).build();
    }
}
