package web.lab;

import java.util.Optional;

import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;

@Path("/")
@Blocking
public class Site {

    @CheckedTemplate
    static class Templates {

        static native TemplateInstance blogPost(BlogEntry entry);

    }

    @Path("/blog/{slug}")
    @GET
    public TemplateInstance blogPost(@PathParam("slug") @NotBlank String slug) {
        final Optional<BlogEntry> bySlug = BlogEntry.getBySlug(slug);
        if (bySlug.isEmpty()) {
            throw new WebApplicationException(RestResponse.notFound().toResponse());
        }
        return Templates.blogPost(bySlug.get());
    }

}
