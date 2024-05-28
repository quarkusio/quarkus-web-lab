## The Blog

In this part, we are going to prepare:
- the index page with all the blog posts
- the detail page to show the content of a blog post

Then we are going to use the Statiq extension to generate a static website which could be deployed on any static server (like GitHub Pages). 

In our case, we will just serve all static pages using a jbang script. 

### Extensions

 - [Qute Web](https://docs.quarkiverse.io/quarkus-qute-web/dev/index.html)
 - [Quarkus REST](https://quarkus.io/guides/rest)
 - [Web Bundler](https://docs.quarkiverse.io/quarkus-web-bundler/dev/index.html)
 - [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
 - [JDBC driver for H2](https://quarkus.io/guides/datasource)
 - [Statiq](https://github.com/quarkiverse/quarkus-statiq)

#### The initial app

We don't start from scratch.
The directory which contains this README also contains the _initial version_ of the app.
There are some comments starting with `TODO:` in the code.
It's up to you to remove these comments with appropriate code!

#### Start the app in the [dev mode](https://quarkus.io/guides/dev-mode-differences)

The app runs on port 8080 so that it does not conflict with other parts of the Lab.
It's time to start Quarkus dev:

```
./mvnw quarkus:dev
```

🚀 Press `w` and observe your web page, press `d` and see the Quarkus Dev UI.

You should see a `TemplateException`, it's part of the plan :)

### Base template

First, we'll need a _base_ template.
All other templates will extend this template and provide the content for the `title` and `body` insert sections.
This feature is called _template inheritance_ and makes it possible to reuse template layouts.

👀 This file is located in `src/main/resource/templates/base.html`.

`{#insert /}` defines no name and so the main block of the relevant `{#include}` section is used.

### Index page

For the index page we'll use the `quarkus-qute-web` extension that exposes the Qute templates located in the `src/main/resource/templates/pub` directory automatically.

👀 Let's take a look how the initial `src/main/resource/templates/pub/index.html` template looks like.

The index page includes the `base.html` and defines the content for the title and body.

Note that `BlogEntry` does not define the `toAbstract` property but we're still able to use it in type-safe manner.
The `toAbstract` property is actually resolved by a [_template extension method_](https://quarkus.io/guides/qute-reference#template_extension_methods) declared on `web.lab.TemplateExtensions`.
Template extension methods can be used to extend the data classes with new functionality.
For example, it is possible to add computed properties and virtual methods.

**››› CODING TIME**

Let's implement the loop to iterate over all blog entries (and fix the template exception).

<details>
<summary>See hint</summary>

We will use the `BlogEntry#listAllSortedByPublished()` method to obtain the blog entries in the template.

</details>

<details>
<summary>See solution</summary>

```html
  {#for entry in BlogEntry:listAllSortedByPublished}
    <article>
      ...
    </article>
  {/for}
  </div>
</main>
```

The `BlogEntry:listAllSortedByCreated` is an expression that calls the static method `web.lab.BlogEntry#listAllSortedByCreated()`.
That's why we need to annotate the `BlogEntry` with `@TemplateData(namespace = "BlogEntry")`.

</details>

🚀 You should see the list of blog post in the browser!

### Blog entry page

For the blog entry page we'll need a simple JAX-RS resource.
This controller will use a [_type-safe template_](https://quarkus.io/guides/qute-reference#typesafe_templates).
Parameters of type-safe templates are used to bind type-safe expressions which are then validated at build time.
In our case, the parameter has name `entry` and type `web.lab.BlogEntry`.
Therefore, any top-level expression that starts with `entry` will be validated against the `web.lab.BlogEntry` type (and additional template extension methods).

👀 Have a look at `src/main/java/web/lab/Blog.java`.

**››› CODING TIME**

Let's define a type-safe template.
Either with [`@CheckedTemplate` and static method](https://quarkus.io/guides/qute-reference#nested-type-safe-templates) or by means of [template records (JDK 14+)](https://quarkus.io/guides/qute-reference#template-records).

<details>
<summary>See solutions</summary>

#### Solution #1:

```java
public class Blog {

    @CheckedTemplate
    static class Templates {
        static native TemplateInstance blogPost(BlogEntry entry);
    }
}
```

#### Solution #2:

```java
public class Blog {

    record blogPost(BlogEntry entry) implements TemplateInstance {}
}
```
</details>

Then we'll use the type-safe template in the resource method.

<details>
<summary>See solutions</summary>

#### Solution #1:

```java
    @Path("/blog/{slug}")
    @GET
    public TemplateInstance blogPost(String slug) {
        final Optional<BlogEntry> blogEntry = BlogEntry.getBySlug(slug);
        if (blogEntry.isEmpty()) {
            throw new WebApplicationException(RestResponse.StatusCode.NOT_FOUND);
        }
        return Templates.blogPost(blogEntry.get());
    }
```

#### Solution #2:

```java
    @Path("/blog/{slug}")
    @GET
    public TemplateInstance blogPost(String slug) {
        final Optional<BlogEntry> blogEntry = BlogEntry.getBySlug(slug);
        if (blogEntry.isEmpty()) {
            throw new WebApplicationException(RestResponse.StatusCode.NOT_FOUND);
        }
        return new blogPost(blogEntry.get());
    }
```
</details>

Then we will need the template file.
👀 The initial version of the template can be found in `src/main/resource/templates/Blog/blogPost.html`.
Note that the template path is [defaulted](https://quarkus.io/guides/qute-reference#customized-template-path).
The blog entry page again includes the `base.html` and defines the content for the title and body.

**››› CODING TIME**

Let's use the `TemplateExtensions#mdToHtml(String string)` to display the content of the blog post.

<details>
<summary>See solution</summary>

```html
<article>
    <header>
      {#entryDate entry=entry/}
    </header>
    <img loading="lazy" src="{entry.title.randomImg}">
    <h1>{entry.title}</h1>
    {entry.content.mdToHtml.raw}
  </article>
```

The `{entry.content.mdToHtml.raw}` expression is quite interesting.
Let's take a look how it's resolved.
The `entry` maps to the `web.lab.BlogEntry` class so during the build Qute validates that a `content` property exist.
It does exist and its type is `java.lang.String`.
Next Qute attempts to validate `mdToHtml`.
There's no such property declared on `java.lang.String` but there is another [template extension method](https://quarkus.io/guides/qute-reference#template_extension_methods): `web.lab.TemplateExtensions#mdToHtml(String)`.
Therefore the validation was successful.
Finally, the `raw` property is used to render an [unescaped value:](https://quarkus.io/guides/qute-reference#character-escapes).
By default, for HTML and XML templates the `'`, `"`, `<`, `>`, `&` characters are escaped.

</details>

🚀 You now have the content of your blog post!

### User-defined tags

You may have noticed that we always display `UNKNOWN` month for each blog entry.
Both `index.html` and `Blog/blogPost.html` call a [user-defined tag](https://quarkus.io/guides/qute-reference#user_tags) to display the date of publishing.
👀 This tag is located in `src/main/resource/templates/tags/entryDate.html`.
However, the "month" part is missing.

**››› CODING TIME**

We will create a template extension method in the `web.lab.TemplateExtensions` class and use it the tag to display the month value in the template.

<details>
<summary>See solution</summary>

```java
public static String monthStr(LocalDate date) {
   return date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
}
```

```html
{@web.lab.BlogEntry entry}
<div class="date">
  <div class="number">{entry.published.getDayOfMonth}</div>
  <div>{entry.published.monthStr}</div>
</div>
```

</details>

🚀 And the month appears!

### Generate a static website (optional)

Once edited, there is no point of reloading all the data from the db and rendering it, we could just export the content and use it from a static server (like GitHub Pages).

👀 Have a look to `src/main/java/web/lab/Statiq` and `src/main/resources/application.properties`, you will find the statiq configuration (what needs to be exported).

Statiq will generate files in `target/statiq`.

Restart Quarkus Dev without live reload:
```shell
./mvnw quarkus:dev -Dquarkus.web-bundler.browser-live-reload=false
```

Now in the Dev UI (press `d` in your terminal):
- click on `Statiq files` in the `statiq` extension box.
- have a look to the list of file to generate.
- Click on Generate

🚀 Now try to serve them locally with a local static server:
```shell
$ jbang app install --verbose --fresh --force statiq@quarkiverse/quarkus-statiq
$ statiq                                                                                                                         decks->!+(ia3andy/decks)
Serving: target/statiq/
Server started on port http://localhost:8181
```
