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

```
mvn quarkus:dev
```

The app runs on port 8080 so that it does not conflict with other parts of the Lab. 
You can see the Dev UI on http://localhost:8080/q/dev-ui

### Base template

First, we'll need a _base_ template.
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

This file is located in `src/main/resource/templates/base.html`.

### Index page

For the index page we'll use the `quarkus-qute-web` extension that exposes the Qute templates located in the `src/main/resource/templates/pub` directory automatically.

Let's take a look how the initial `index.html` template looks like.

```html
{#include base.html}
{#title}My Quarkus Blog{/title}
<main class="articles">
  <div>
  <!-- TODO: iterate over all blog entries sorted by the published date in desc order -->
  <!-- start of the loop -->
  <article>
      <header>
        <img src="{entry.title.randomThumb}" loading="lazy">
        {#entryDate entry=entry/}
      </header>
      <h3><a href="/blog/{entry.slug}">{entry.title}</a></h3>
      <p>{entry.toAbstract}</p>
      <a href="/blog/{entry.slug}">Read more</a>
    </article>
   <!-- end of the loop -->
  </div>
</main>
```

The index page includes the `base.html` and defines the content for the title and body.

Note that `BlogEntry` does not define the `toAbstract` property but we're still able to use it in type-safe manner.
The `toAbstract` property is actually resolved by a [_template extension method_](https://quarkus.io/guides/qute-reference#template_extension_methods) declared on `web.lab.TemplateExtensions`.
Template extension methods can be used to extend the data classes with new functionality.
For example, it is possible to add computed properties and virtual methods.

**››› CODING TIME**

Let's implement the loop to iterate over all blog entries.
We will use the `BlogEntry#listAllSortedByPublished()` method to obtain the blog entries in the template.

<details>
<summary>See hint</summary>

```html
{#include base.html}
{#title}My Quarkus Blog{/title}
<main class="articles">
  <div>
  {#for entry in BlogEntry:listAllSortedByPublished}
    <article>
      <header>
        <img src="{entry.title.randomThumb}" loading="lazy">
        {#entryDate entry=entry/}
      </header>
      <h3><a href="/blog/{entry.slug}">{entry.title}</a></h3>
      <p>{entry.toAbstract}</p>
      <a href="/blog/{entry.slug}">Read more</a>
    </article>
  {/for}
  </div>
</main>
```

The `BlogEntry:listAllSortedByCreated` is an expression that calls the static method `web.lab.BlogEntry#listAllSortedByCreated()`.
That's why we need to annotate the `BlogEntry` with `@TemplateData(namespace = "BlogEntry")`.

</details>

### Blog entry page

For the blog entry page we'll need a simple JAX-RS resource.
This controller will use a [_type-safe template_](https://quarkus.io/guides/qute-reference#typesafe_templates).
Parameters of type-safe templates are used to bind type-safe expressions which are then validated at build time.
In our case, the parameter has name `entry` and type `web.lab.BlogEntry`.
Therefore, any top-level expression that starts with `entry` will be validated against the `web.lab.BlogEntry` type (and additional template extension methods).

```java
@Path("/")
public class Blog {

    // TODO: define a type-safe template

    @Path("/blog/{slug}")
    @GET
    public TemplateInstance blogPost(String slug) {
        final Optional<BlogEntry> blogEntry = BlogEntry.getBySlug(slug);
        if (blogEntry.isEmpty()) {
            throw new WebApplicationException(RestResponse.StatusCode.NOT_FOUND);
        }
        // TODO: use the type-safe template to render the blog post
        return null;
    }

}
```

**››› CODING TIME**

Let's define a type-safe template.
Either with [`@CheckedTemplate` and static method](https://quarkus.io/guides/qute-reference#nested-type-safe-templates) or by means of [template records (JDK 14+)](https://quarkus.io/guides/qute-reference#template-records).

<details>
<summary>See hint</summary>

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
<summary>See hint</summary>

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
The initial version of the template can be found in `src/main/resource/templates/Blog/blogPost.html`.
Note that the template path is [defaulted](https://quarkus.io/guides/qute-reference#customized-template-path).
The blog entry page again includes the `base.html` and defines the content for the title and body.

```html
{#include base.html}
{#title}My Quarkus Blog - {entry.title}{/title}

<main class="blog-post">
  <article>
    <header>
      {#entryDate entry=entry/}
    </header>
    <img loading="lazy" src="{entry.title.randomImg}">
    <h1>{entry.title}</h1>
    <!-- TODO: use the TemplateExtensions#mdToHtml(String string) to display the md content of a blog entry as HTML -->
  </article>
</main>
```

**››› CODING TIME**

Let's use the `TemplateExtensions#mdToHtml(String string)` to display the content of the blog post.

<details>
<summary>See hint</summary>

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

### User-defined tags

You may have noticed that we always display `UNKNOWN` month for each blog entry.
Both `index.html` and `Blog/blogPost.html` call a [user-defined tag](https://quarkus.io/guides/qute-reference#user_tags) to display the date of publishing.
This tag is located in `src/main/resource/templates/tags/entryDate.html`.
However, the "month" part is missing.

```html
{@web.lab.BlogEntry entry}
<div class="date">
  <div class="number">{entry.published.getDayOfMonth}</div>
  <div>
    <!-- TODO: use the computed property of LocalDate to display the month value -->
    UNKNOWN
  </div>
</div>
```

**››› CODING TIME**

We will create a template extension method in the `web.lab.TemplateExtensions` class and use it the tag to display the month value in the template.

<details>
<summary>See hint</summary>

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
