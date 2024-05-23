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

    @Column(unique = true)
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
        // TODO 1: return all entries sorted by published date descending
        return List.of();
    }

    public static Optional<BlogEntry> getByTitle(String title) {
        // TODO 2: return the entry with the given title
        return Optional.empty();
    }
}