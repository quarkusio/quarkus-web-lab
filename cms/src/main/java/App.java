import io.quarkiverse.renarde.Controller;
import rest.Blog;

import jakarta.ws.rs.Path;

public class App extends Controller {

    @Path("/")
    public void home() {
        redirect(Blog.class).index(null, null);
    }
}