package model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import util.Slug;

@Entity
public class BlogEntry extends PanacheEntity {

    @Column(unique = true)
    public String title;

    public String slug;

    @Column(columnDefinition = "text")
    public String content;

    public LocalDate published;

    public BlogEntry() {
        super();
    }

    public BlogEntry(String title, String content) {
        this(title, content, LocalDate.now());
    }
    
    public BlogEntry(String title, String content, LocalDate published) {
        this.title = title;
        this.content = content;
        this.slug = Slug.toSlug(title);
        this.published = published;
    }

    public static List<BlogEntry> listAllSortedByPublished() {
        return BlogEntry.listAll(Sort.by("published").descending());
    }

    public static Optional<BlogEntry> getByTitle(String title) {
        return BlogEntry.find("LOWER(title) = LOWER(?1)", title).firstResultOptional();
    }
}