## The Static server

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

TODO: Add rest of the instructions. CORS will be needed for commenting