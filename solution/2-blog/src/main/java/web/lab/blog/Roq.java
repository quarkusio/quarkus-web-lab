package web.lab.blog;

import io.quarkiverse.roq.generator.runtime.StaticPage;
import io.quarkiverse.roq.generator.runtime.StaticPages;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Produces;

@Singleton
public class Roq {

    @Produces
    @Singleton
    @Transactional
    StaticPages produce() {
        return new StaticPages(BlogEntry.<BlogEntry>listAll().stream().map(e -> StaticPage.builder().html("/blog/" + e.slug + "/").build()).toList());
    }

}
