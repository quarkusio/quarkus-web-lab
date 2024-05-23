## The Blog

In this part, we are going to prepare:
- the blog index page with all the posts
- the post page to show the content

Then we are going to use the statiq extension to generate a static website which could be deployed on any static server (like GitHub Page). 

In our case, we will just serve all static pages using a jbang script. 

#### Get the initial app

TODO: Url to app with the backend

#### Start the app in the [dev mode](https://quarkus.io/guides/dev-mode-differences)

```
mvn quarkus:dev
```

The app runs on port 8080 so that it does not conflict with other parts of the Lab. 
You can see the Dev UI on http://localhost:8080/q/dev-ui

### Base template

First, we'll create a _base_ template.
All other templates will extend this template and provide the content for the `title` and `body` insert sections.
This feature is called _template inheritance_ and makes it possible to reuse template layouts.

```html
<!DOCTYPE html>
<html>
<head>
<title>{#insert title/}</title>
{#bundle /}
</head>
<body>
  {#insert /}
</body>
</html>
```

`{#insert /}` defines no name and so the main block of the relevant `{#include}` section is used.

We'll save this file in `src/main/resource/templates/base.html`.

### Index page

For the index page we'll use the `quarkus-qute-web` extension that exposes the Qute templates located in the `src/main/resource/templates/pub` directory automatically.

```html
{#include base.html}
{#title}My Blog{/title}
<h1>Welcome to my Blog</h1>
{#for entry in BlogEntry:listAllSortedByCreated}
<article>
  <h2><a href="/blog/{entry.slug}">{entry.title}</a></h2>
  <p>{entry.toAbstract}</p>
  <a href="/blog/{entry.slug}">Read more</a>
</article>
{/for}
```

The index page includes the `base.html` and defines the content for the title and body.
The `BlogEntry:listAllSortedByCreated` is an expression that calls the static method `web.lab.BlogEntry#listAllSortedByCreated()`.
That's why we need to annotate the `BlogEntry` with `@TemplateData(namespace = "BlogEntry")`.

Note that `BlogEntry` does not define the `toAbstract` property but we're still able to use it in type-safe manner.
The `toAbstract` property is actually resolved by a [_template extension method_](https://quarkus.io/guides/qute-reference#template_extension_methods) declared on `web.lab.TemplateExtensions`.
Template extension methods can be used to extend the data classes with new functionality.
For example, it is possible to add computed properties and virtual methods.

### Blog entry page

For the blog entry page we'll create a simple JAX-RS resource.
This resource defines a [_type-safe template_](https://quarkus.io/guides/qute-reference#typesafe_templates).
Parameters of type-safe templates are used to bind type-safe expressions which are then validated at build time.
In our case, the parameter has name `entry` and type `web.lab.BlogEntry`.
Therefore, any top-level expression that starts with `entry` will be validated against the `web.lab.BlogEntry` type (and additional template extension methods).

```java
@Path("/")
public class Blog {

    @CheckedTemplate
    static class Templates {
        static native TemplateInstance blogPost(BlogEntry entry);
    }

    @Path("/blog/{slug}")
    @GET
    public TemplateInstance blogPost(String slug) {
        final Optional<BlogEntry> blogEntry = BlogEntry.getBySlug(slug);
        if (blogEntry.isEmpty()) {
            throw new WebApplicationException(RestResponse.StatusCode.NOT_FOUND);
        }
        return Templates.blogPost(blogEntry.get());
    }
}
```

Then we will create the template file.
The blog entry page again includes the `base.html` and defines the content for the title and body.

```html
{#include base.html}
{#title}My Blog - {entry.title}{/title}

<article>
  <h1>{entry.title}</h1>
  {entry.content.mdToHtml.raw}
</article>
```

We'll save this file in `src/main/resource/templates/Blog/blogPost.html`.
The template path is [defaulted](https://quarkus.io/guides/qute-reference#customized-template-path).

The `{entry.content.mdToHtml.raw}` expression is a little bit more interesting.
Let's take a look how it's resolved.
The `entry` maps to the `web.lab.BlogEntry` class so during the build Qute validates that a `content` property exist.
It does exist and its type is `java.lang.String`.
Next Qute attempts to validate `mdToHtml`.
There's no such property declared on `java.lang.String` but there is another [template extension method](https://quarkus.io/guides/qute-reference#template_extension_methods): `web.lab.TemplateExtensions#mdToHtml(String)`.
Therefore the validation was successful.
Finally, the `raw` property is used to render an [unescaped value:](https://quarkus.io/guides/qute-reference#character-escapes).
By default, for HTML and XML templates the `'`, `"`, `<`, `>`, `&` characters are escaped.

### Generate a static website

Hit this url to generate the static website: http://localhost:8080/q/statiq/generate

Statiq will generate files in `target/statiq`.

To serve them locally:
```shell
$ jbang app install --verbose --fresh --force statiq@quarkiverse/quarkus-statiq
$ statiq                                                                                                                         decks->!+(ia3andy/decks)
Serving: target/statiq/
Server started on port http://localhost:8181
```
