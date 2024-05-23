package rest;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

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
public class Cms extends HxController {
    
    /**
     * This defines templates available in src/main/resources/templates/Classname/method.html by convention
     */
    @CheckedTemplate
    public static class Templates {
        /**
         * This specifies that the Cms/index.html template takes a list of blogEntries as parameter
         */
        public static native TemplateInstance index(List<BlogEntry> blogEntries, BlogEntry currentBlogEntry);

        public static native TemplateInstance index$blogEntries(List<BlogEntry> blogEntries, BlogEntry currentBlogEntry);

        public static native TemplateInstance index$blogEntryForm(BlogEntry currentBlogEntry);
    }

    // set up our blog index page at /cms
    @Path("")
    public TemplateInstance index() {
        if (isHxRequest()) {
            return Templates.index$blogEntries(BlogEntry.listAllSortedByPublished(), null);
        }
        return Templates.index(BlogEntry.listAllSortedByPublished(), null);
    }

    public TemplateInstance newBlogEntry() {
        if (isHxRequest()) {
            this.hx(HxResponseHeader.TRIGGER, "refreshEntries");
            return concatTemplates(Templates.index$blogEntries(BlogEntry.listAllSortedByPublisheds(), null),
                    Templates.index$blogEntryForm(new BlogEntry()));
        }
    	return Templates.index(BlogEntry.listAllSortedByPublished(), new BlogEntry());
    }

    public TemplateInstance editBlogEntry(@RestPath Long id) {
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        if (isHxRequest()) {
            this.hx(HxResponseHeader.TRIGGER, "refreshEntries");
            return concatTemplates(Templates.index$blogEntries(BlogEntry.listAllSortedByPublished(), blogEntry),
                    Templates.index$blogEntryForm(blogEntry));
        }
        return Templates.index(BlogEntry.listAllSortedByPublished(), blogEntry);
    }

    @POST
    public void saveBlogEntry(@RestPath Long id, 
    		@RestForm @NotBlank String title, 
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
            editBlogEntry(id);
        }
        blogEntry.title = title;
        blogEntry.content = content;
        blogEntry.published = published;
        blogEntry.slug = Slug.toSlug(title);
        // save is automatic for managed entities
        editBlogEntry(id);
    }
    
    @POST
    public void saveNewBlogEntry( 
    		@RestForm @NotBlank String title, 
    		@RestForm String content) {
        if (validationFailed()) {
            newBlogEntry();
        }
        if (BlogEntry.getByTitle(title).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
        }
        if (validationFailed()) {
            newBlogEntry();
        }
        BlogEntry blogEntry = new BlogEntry(title, content);
        // make it persistent
        blogEntry.persist();
        editBlogEntry(blogEntry.id);
    }

    @DELETE
    public String deleteBlogEntry(@RestPath("id") Long id) {
        onlyHxRequest();
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        blogEntry.delete();
        // HTMX is not a fan of 204 No Content for swapping https://github.com/bigskysoftware/htmx/issues/1130
        return "";
    }
}