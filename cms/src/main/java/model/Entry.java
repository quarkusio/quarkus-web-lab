package model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.util.StringUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.Date;
import java.util.List;

@Entity
public class Entry extends PanacheEntity {
    public String title;

    @Column(columnDefinition="text")
    public String content;
    public Date created = new Date();
    public Date updated = new Date();

    public Entry() {
        super();
    }

    public Entry(String title, String content) {
        this();
        this.title = title;
        this.content = content;
    }

    public static List<Entry> listAllSortedByLastUpdated() {
        return Entry.listAll(Sort.by("updated").descending());
    }

    public static List<Entry> search(String search) {
        if(StringUtil.isNullOrEmpty(search)) {
            return listAllSortedByLastUpdated();
        }
        return Entry.find("LOWER(title) like LOWER(:search) OR LOWER(content) like LOWER(:search)", Sort.by("updated").descending(), Parameters.with("search", '%' + search + '%')).list();
    }
}