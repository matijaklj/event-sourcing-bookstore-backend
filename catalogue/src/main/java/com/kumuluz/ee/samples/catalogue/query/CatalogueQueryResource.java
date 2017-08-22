package com.kumuluz.ee.samples.catalogue.query;

import com.kumuluz.ee.samples.catalogue.entity.Book;
import com.kumuluz.ee.samples.catalogue.BookStore;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Matija Kljun
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/books")
@RequestScoped
public class CatalogueQueryResource {

    //@Resource
    @Inject
    private BookStore bookStore;

    @GET
    public Response getBooks() {

        List<Book> books = bookStore.getBooks();

        return Response.ok().entity(books).build();
    }
}
