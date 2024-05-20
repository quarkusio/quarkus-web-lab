package model;

import static util.Slug.toSlug;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.util.StringUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class BlogEntry extends PanacheEntity {

    @Column(unique = true)
    public String title;

    public String slug;

    @Column(columnDefinition = "text")
    public String content;

    public Date created = new Date();

    public Date updated = new Date();

    public BlogEntry() {
        super();
    }

    public BlogEntry(String title, String content) {
        this.title = title;
        this.slug = toSlug(title);
        this.content = content;
    }

    public static List<BlogEntry> listAllSortedByLastUpdated() {
        return BlogEntry.listAll(Sort.by("updated").descending());
    }

    public static List<BlogEntry> listAllSortedByCreated() {
        return BlogEntry.listAll(Sort.by("created").descending());
    }

    public static Optional<BlogEntry> getByTitle(String title) {
        return BlogEntry.find("LOWER(title) = LOWER(?1)", title).firstResultOptional();
    }
    
    public static Optional<BlogEntry> getBySlug(String slug) {
        return BlogEntry.find("LOWER(slug) = LOWER(?1)", slug).firstResultOptional();
    }

    public static List<BlogEntry> search(String search) {
        if (StringUtil.isNullOrEmpty(search)) {
            return listAllSortedByLastUpdated();
        }
        return BlogEntry.find("LOWER(title) like LOWER(:search) OR LOWER(content) like LOWER(:search)",
                Sort.by("updated").descending(), Parameters.with("search", '%' + search + '%')).list();
    }
}