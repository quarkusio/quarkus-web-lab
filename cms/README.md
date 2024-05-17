## The CMS

For now we need to use Quarkus 3.9.4

### Extensions

 - [Renarde](https://docs.quarkiverse.io/quarkus-renarde/dev/index.html)
 - [Web Bundler](https://docs.quarkiverse.io/quarkus-web-bundler/dev/index.html)
 - [Playwright](https://docs.quarkiverse.io/quarkus-playwright/dev/index.html)

### Create initial app

```
mvn io.quarkus.platform:quarkus-maven-plugin:3.10.0:create -DprojectGroupId=web.workshop -DprojectArtifactId=cms     -Dextensions='io.quarkiverse.renarde:quarkus-renarde, io.quarkiverse.web-bundler:quarkus-web-bundler'
```



Start on 9090

```
quarkus.http.port=9090
```

Start in dev mode

```
mvn quarkus:dev
```

