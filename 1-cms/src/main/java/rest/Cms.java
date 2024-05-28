package rest;

import java.time.LocalDate;
import java.util.List;

import io.quarkiverse.renarde.Controller;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkiverse.renarde.htmx.HxController;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import model.BlogEntry;
import util.Slug;

// Define a new controller, blocking (DB operations), at /cms
@Blocking
@Path("/cms")
public class Cms extends Controller {
    
    /**
     * This defines templates available in src/main/resources/templates/Classname/method.html by convention
     */
    @CheckedTemplate
    public static class Templates {
        /**
         * This specifies that the Cms/index.html template takes a list of blogEntries as parameter
         */
        public static native TemplateInstance index(List<BlogEntry> blogEntries, BlogEntry currentBlogEntry);

    }

    // set up our blog index page at /cms
    @Path("")
    public TemplateInstance index() {
        return Templates.index(BlogEntry.listAllSortedByPublished(), null);
    }

    public TemplateInstance newBlogEntry() {
    	return Templates.index(BlogEntry.listAllSortedByPublished(), new BlogEntry());
    }

    public TemplateInstance editBlogEntry(@RestPath Long id) {
        // TODO: find the blog entry, return an error if null
        BlogEntry blogEntry = new BlogEntry();
        return Templates.index(BlogEntry.listAllSortedByPublished(), blogEntry);
    }

    @POST
    public void saveBlogEntry(@RestPath Long id, 
    		@RestForm @NotBlank String title,
            @RestForm @NotBlank String picture,
    		@RestForm String content,
            @RestForm LocalDate published) {
        if (validationFailed()) {
            editBlogEntry(id);
        }
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        if (BlogEntry.getByTitle(title).filter(other -> other.id != id).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
        }
        if (validationFailed()) {
            // TODO: redirect to edition
        }
        // TODO: set the todo values and redirect to it
        // save is automatic for managed entities
    }
    
    @POST
    public void saveNewBlogEntry( 
    		@RestForm @NotBlank String title,
            @RestForm @NotBlank String picture,
            @RestForm String content,
            @RestForm LocalDate published) {
        if (validationFailed()) {
            newBlogEntry();
        }
        if (BlogEntry.getByTitle(title).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
        }
        if (validationFailed()) {
            newBlogEntry();
        }
        BlogEntry blogEntry = new BlogEntry(title, picture, content, published);
        // TODO: make it persistent and redirect to edition
    }

    @POST
    public void deleteBlogEntry(@RestPath("id") Long id) {
        // TODO: find the entry, return an error if null, delete it
    }
}