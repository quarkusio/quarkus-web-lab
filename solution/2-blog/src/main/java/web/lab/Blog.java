package web.lab;

import java.util.Optional;

import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

@Path("/")
public class Blog {

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance blogPost(BlogEntry entry);

    }

    @Path("/blog/{slug}")
    @GET
    public TemplateInstance blogPost(String slug) {
        final Optional<BlogEntry> blogEntry = BlogEntry.getBySlug(slug);
        if (blogEntry.isEmpty()) {
            throw new WebApplicationException(RestResponse.StatusCode.NOT_FOUND);
        }
        return Templates.blogPost(blogEntry.get());
    }

}
