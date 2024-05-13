## The Static server

The static server simulate a static HTML server (like GitHub Page). On our case we will just serve all static content from a certain location. 
Our CMS will publish static content to this location.

### Extensions

- [VertX-HTTP](https://quarkus.io/guides/http-reference)

### Create initial app

```
mvn io.quarkus.platform:quarkus-maven-plugin:3.10.0:create -DprojectGroupId=web.workshop -DprojectArtifactId=static-server -Dextensions='vertx-http'
```

Add one class to serve files statically

```
package web.workshop.staticserver;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.enterprise.event.Observes;

public class StaticResources {
    void installRoute(@Observes StartupEvent startupEvent, Router router) {
        router.route()
                .path("/*")
                .handler(StaticHandler.create("published"));
    }
}
```

This mean all files in `static-server/published` will be served.