package web.lab.blog;

import java.util.Optional;

import io.quarkiverse.roq.generator.runtime.RoqSelection;
import io.quarkiverse.roq.generator.runtime.SelectedPath;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Produces;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

@Path("/")
public class Blog {

    // You can also use template records with JDK 14+
    // record blogPost(BlogEntry entry) implements TemplateInstance {}

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
        // You can also use template records with JDK 14+
        // return new blogPost(blogEntry.get());
    }

    @Produces
    @Singleton
    @Transactional
    RoqSelection produceRoqSelection() {
        return new RoqSelection(BlogEntry.<BlogEntry>listAll().stream().map(e -> SelectedPath.builder().html("/blog/" + e.slug + "/").build()).toList());
    }


}
