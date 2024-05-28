package web.lab.blog;

import java.util.Optional;

import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

@Path("/")
public class Blog {

    // TODO: define a type-safe template

    @Path("/blog/{slug}")
    @GET
    public TemplateInstance blogPost(String slug) {
        final Optional<BlogEntry> blogEntry = BlogEntry.getBySlug(slug);
        if (blogEntry.isEmpty()) {
            throw new WebApplicationException(RestResponse.StatusCode.NOT_FOUND);
        }
        // TODO: use the type-safe template to render the blog post
        return null;
    }

}
