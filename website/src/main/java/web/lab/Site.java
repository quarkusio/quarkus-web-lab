package web.lab;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;
import java.util.Optional;

@Path("/")
@Blocking
public class Site {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(List<Entry> entries);

        public static native TemplateInstance post(Entry entry);

    }

    @Path("")
    @GET
    public TemplateInstance index() {
        return Templates.index(Entry.listAllSortedByCreated());
    }

    @Path("/post/{slug}")
    @GET
    public TemplateInstance post(@PathParam("slug") @NotBlank String slug) {
        final Optional<Entry> bySlug = Entry.getBySlug(slug);
        if (bySlug.isEmpty()) {
            throw new WebApplicationException(RestResponse.notFound().toResponse());
        }
        return Templates.post(bySlug.get());
    }

}
