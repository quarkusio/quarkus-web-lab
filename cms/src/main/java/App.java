import io.quarkiverse.renarde.Controller;
import rest.Cms;

import jakarta.ws.rs.Path;

public class App extends Controller {

    @Path("/")
    public void home() {
        redirect(Cms.class).index(null, null);
    }
}