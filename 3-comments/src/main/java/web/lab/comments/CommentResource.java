package web.lab.comments;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.time.LocalDateTime;
import java.util.List;

@Path("/comment")
public class CommentResource {
    
    @GET
    @Path("/{ref}")
    public List<Comment> comments(String ref) {
        return Comment.findRefComments(ref);
    }

    @POST
    @Transactional
    public List<Comment> addComment(Comment comment) {
        if(comment.time == null){
            comment.time = LocalDateTime.now();
        }
        comment.persist();
        
        return comments(comment.ref);
    }
}
