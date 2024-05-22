package util;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import model.BlogEntry;

@ApplicationScoped
public class Startup {
    /**
     * This method is executed at the start of your application
     */
	@Transactional
    public void start(@Observes StartupEvent evt) {
        // in DEV mode we seed some data
        if(LaunchMode.current() == LaunchMode.DEVELOPMENT) {
        	BlogEntry blogEntry = new BlogEntry("How to make cheese", "The process is easy, here's how to set it up in 12 steps. The last one will surprise you.");
        	blogEntry.persist();

        	blogEntry = new BlogEntry("How to eat cheese", "The process is easy, here's how to set it up in 12 steps. The last one will surprise you.");
        	blogEntry.persist();
        }
    }
}