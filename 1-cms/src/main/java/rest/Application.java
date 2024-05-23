package rest;

import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.Path;

public class Application extends Controller {
	// redirect / to /cms
	@Path("/")
	public void redirectToCms() {
		redirect(Cms.class).index();
	}
}