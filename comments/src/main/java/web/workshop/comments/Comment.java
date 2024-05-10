package web.workshop.comments;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Comment extends PanacheEntity {
    @NotBlank(message="Ref may not be blank")
    public String ref;
    public LocalDateTime time;
    @NotBlank(message="Name may not be blank")
    public String name;
    @NotBlank(message="Comment may not be blank")
    public String comment;
    
    public static List<Comment> findRefComments(String ref){
        return list("ref", Sort.by("time", Sort.Direction.Descending),ref);
    }
}
