package web.lab;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Entity
public class Entry extends PanacheEntity {
    public String title;

    public String slug;

    @Column(columnDefinition="text")
    public String content;
    public Date created = new Date();
    public Date updated = new Date();

    public Entry() {
        super();
    }

    public static List<Entry> listAllSortedByCreated() {
        return Entry.listAll(Sort.by("created").descending());
    }

    public static Optional<Entry> getBySlug(String slug) {
        return Entry.find("LOWER(slug) = LOWER(?1)", slug).firstResultOptional();
    }


}