## The WebSite

In this part, we are going to prepare:
- the blog index page with all the posts
- the post page to show the content

Then we are going to use the statiq extension to generate a static website which could be deployed on any static server (like GitHub Page). 

On our case we will just serve all static using a jbang script. 


### Create initial app

```
quarkus create app web.workshop:website -x quarkus-rest-qute -x quarkus-web-bundler -x quarkus-statiq -x jdbc-h2 -x hibernate-orm-panache --no-code
```

Also add Playwright as a test scoped dependency

```
<dependency>
    <groupId>io.quarkiverse.playwright</groupId>
    <artifactId>quarkus-playwright</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
</dependency>
```

Hit this url to generate the static website: http://localhost:8080/q/statiq/generate

Statiq will generate files in `target/statiq`.

To serve them locally:
```shell
$ jbang app install --verbose --fresh --force statiq@quarkiverse/quarkus-statiq
$ statiq                                                                                                                         decks->!+(ia3andy/decks)
Serving: target/statiq/
Server started on port http://localhost:8181
```