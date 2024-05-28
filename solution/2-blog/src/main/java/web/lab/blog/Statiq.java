package web.lab.blog;

import io.quarkiverse.statiq.runtime.StatiqPage;
import io.quarkiverse.statiq.runtime.StatiqPages;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Produces;

@Singleton
public class Statiq {

    @Produces
    @Singleton
    @Transactional
    StatiqPages produce() {
        return new StatiqPages(BlogEntry.<BlogEntry>listAll().stream().map(e -> new StatiqPage("/blog/"+ e.slug + "/")).toList());
    }

}
