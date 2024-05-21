package web.lab;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.TemplateData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@TemplateData(namespace = "BlogEntry")
@Entity
public class BlogEntry extends PanacheEntity {
    
    public String title;

    public String slug;

    @Column(columnDefinition="text")
    public String content;
    
    public Date created = new Date();
    
    public Date updated = new Date();

    public static List<BlogEntry> listAllSortedByCreated() {
        return BlogEntry.listAll(Sort.by("created").descending());
    }

    public static Optional<BlogEntry> getBySlug(String slug) {
        if (slug.isBlank()) {
            return Optional.empty();
        }
        return BlogEntry.find("LOWER(slug) = LOWER(?1)", slug).firstResultOptional();
    }


}