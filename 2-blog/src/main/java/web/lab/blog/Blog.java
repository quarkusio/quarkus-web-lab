package web.lab.blog;

import io.quarkiverse.roq.generator.runtime.RoqSelection;
import io.quarkiverse.roq.generator.runtime.SelectedPath;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Optional;

@Path("/")
@Blocking
public class Blog {

    // TODO: here define a type-safe template (@CheckedTemplate)

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

    @Produces
    @Singleton
    @Transactional
    RoqSelection produceRoqSelection() {
        return new RoqSelection(BlogEntry.<BlogEntry>listAll().stream().map(e -> SelectedPath.builder().html("/blog/" + e.slug + "/").build()).toList());
    }

}
