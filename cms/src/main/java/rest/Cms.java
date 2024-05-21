package rest;

import io.quarkiverse.renarde.htmx.HxController;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import model.BlogEntry;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.Date;
import java.util.List;

import static util.Slug.toSlug;

@Blocking
@Path("/cms")
public class Cms extends HxController {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(List<BlogEntry> blogEntries, Long currentBlogEntryId, BlogEntry currentBlogEntry,
                String search);

        public static native TemplateInstance index$blogEntries(List<BlogEntry> blogEntries, Long currentBlogEntryId);

        public static native TemplateInstance index$blogEntryForm(BlogEntry currentBlogEntry);
    }

    @Path("")
    public TemplateInstance index(@RestQuery("id") Long id, @RestQuery("search") String search) {
        final List<BlogEntry> entries = BlogEntry.search(search);
        if (isHxRequest()) {
            return Templates.index$blogEntries(entries, id);
        }
        BlogEntry entry = id != null ? BlogEntry.findById(id) : null;
        return Templates.index(entries, id, entry, search);
    }

    public TemplateInstance newBlogEntry() {
        // not really used
        BlogEntry entry = new BlogEntry();
        if (isHxRequest()) {
            this.hx(HxResponseHeader.TRIGGER, "refreshEntries");
            return concatTemplates(Templates.index$blogEntries(BlogEntry.listAllSortedByLastUpdated(), null),
                    Templates.index$blogEntryForm(entry));
        }
        return Templates.index(BlogEntry.listAllSortedByLastUpdated(), null, entry, null);
    }

    public TemplateInstance editBlogEntry(@RestPath("id") Long id) {
        final BlogEntry entry = BlogEntry.findById(id);
        if (entry == null) {
            index(null, null);
            return null;
        }
        if (isHxRequest()) {
            this.hx(HxResponseHeader.TRIGGER, "refreshEntries");
            return concatTemplates(Templates.index$blogEntries(BlogEntry.listAllSortedByLastUpdated(), id),
                    Templates.index$blogEntryForm(entry));
        }
        return Templates.index(BlogEntry.listAllSortedByLastUpdated(), id, entry, null);
    }

    @DELETE
    public String deleteBlogEntry(@RestPath("id") Long id) {
        onlyHxRequest();
        BlogEntry entry = BlogEntry.findById(id);
        notFoundIfNull(entry);
        entry.delete();
        // HTMX is not a fan of 204 No Content for swapping https://github.com/bigskysoftware/htmx/issues/1130
        return "";
    }

    @POST
    public void saveBlogEntry(@RestPath("id") Long id, @RestForm @NotBlank String title, @RestForm String content) {
        if (validationFailed()) {
            editBlogEntry(id);
            return;
        }
        BlogEntry entry = BlogEntry.findById(id);
        notFoundIfNull(entry);
        if (!entry.title.equals(title) && BlogEntry.getByTitle(title).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
            prepareForErrorRedirect();
            editBlogEntry(id);
            return;
        }
        entry.title = title;
        entry.content = content;
        entry.updated = new Date();
        entry.slug = toSlug(title);
        entry.persist();
        editBlogEntry(id);
    }

    @POST
    public void saveNewBlogEntry(@RestForm @NotBlank String title, @RestForm String content) {
        if (validationFailed()) {
            newBlogEntry();
            return;
        } else if (BlogEntry.getByTitle(title).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
            prepareForErrorRedirect();
            newBlogEntry();
            return;
        }
        BlogEntry entry = new BlogEntry();
        entry.title = title;
        entry.content = content;
        entry.slug = toSlug(title);
        entry.persist();
        editBlogEntry(entry.id);
    }
}