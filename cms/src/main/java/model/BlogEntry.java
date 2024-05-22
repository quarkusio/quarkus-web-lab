package model;

import java.util.Date;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
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

    public Date created = new Date();

    public Date updated = new Date();

    public BlogEntry() {
        super();
    }

    public BlogEntry(String title, String content) {
        this.title = title;
        this.slug = Slug.toSlug(title);
        this.content = content;
    }

    public static Optional<BlogEntry> getByTitle(String title) {
        return BlogEntry.find("LOWER(title) = LOWER(?1)", title).firstResultOptional();
    }
}