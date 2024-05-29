package web.lab.blog;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.TemplateData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@TemplateData(namespace = "BlogEntry")
@Entity
public class BlogEntry extends PanacheEntity {
    
    public String title;

    public String slug;

    public String picture;

    @Column(columnDefinition="text")
    public String content;
    
    public LocalDate published = LocalDate.now();
    
    public static List<BlogEntry> listAllSortedByPublished() {
        return BlogEntry.listAll(Sort.by("published").descending());
    }

    public static Optional<BlogEntry> getBySlug(String slug) {
        if (slug.isBlank()) {
            return Optional.empty();
        }
        return BlogEntry.find("LOWER(slug) = LOWER(?1)", slug).firstResultOptional();
    }


}