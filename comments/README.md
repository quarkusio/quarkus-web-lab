## Comments

### Extensions

 - [Web Bundler](https://docs.quarkiverse.io/quarkus-web-bundler/dev/index.html)
 - [REST and Jackson](https://quarkus.io/guides/rest#json-serialisation)
 - [Hibernate and Panache](https://quarkus.io/guides/hibernate-orm-panache)
 - [Postgresql](https://quarkus.io/guides/datasource)

### Create initial app

```
mvn io.quarkus.platform:quarkus-maven-plugin:3.10.0:create -DprojectGroupId=web.workshop -DprojectArtifactId=comments -Dextensions='io.quarkiverse.web-bundler:quarkus-web-bundler, io.quarkus:quarkus-rest-jackson, io.quarkus:quarkus-hibernate-orm-panache, io.quarkus:quarkus-jdbc-postgresql'
```

Start on 7070

```
quarkus.http.port=7070
```
