## The CMS

For now we need to use Quarkus 3.11.0.CR1

### Extensions

 - [Renarde](https://docs.quarkiverse.io/quarkus-renarde/dev/index.html)
 - [Web Bundler](https://docs.quarkiverse.io/quarkus-web-bundler/dev/index.html)
 - [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
 - [JDBC driver for H2](https://quarkus.io/guides/datasource)

### Get the initial app

We could start from scratch, but it is nicer to start with a few things ready.

TODO: Url to app with the backend

It's time to start Quarkus dev:
```shell
mvn quarkus:dev
```

Press `w` and observe your web page.

### Let's have a look to the Quarkus config

Open the `src/main/resources/application.properties` file, those setting are allowing
us to run on a custom port, connect to a shared H2 database, and update our schema when needed.

### The Model

The first thing to do is to represent our persistent model, that will be our Java representation of the
blog entry database table.

Open the `src/main/java/model/BlogEntry.java`. This
contains our main entity for the lab, representing a blog entry. It will have the following attributes:

- Title (must be unique)
- Slug (derived from the title)
- Content (the blog contents, in Markdown)
- Published (the publication date)

This is a Panache entity, so it extends `PanacheEntity` to get a lot of useful methods, and its fields
are public. We will need a default constructor and a constructor with `title`, `content`, and optionally
`publishedDate`. 

### Time to show blog posts (The View)

The app is set up with initial data. Traditionally, startup actions belong in a `util/Startup` class (`src/main/java/util/Startup.java`), and we can use that to create and
save test database values. To that end, we mark the method as `@Transactional`, and only create
the test data in DEV mode.

**››› CODING TIME**

Let's implement the query method for getting all entries sorted by publishing date (descending) `listAllSortedByPublished`:

<details>
    <summary>See hint</summary>

    Use `BlogEntry.listAll` combined with `Sort.by("published").descending()`
</details>

<details>
    <summary>See solution</summary>

```java
    public static List<BlogEntry> listAllSortedByPublished() {
        return BlogEntry.listAll(Sort.by("published").descending());
    }
```
</details>

On the browser you should now have a list of "foo", let's show the actual posts.

In Renarde, by default, all views live in `templates/<Controller>/<method>.html`, so for `Cms.index` we need to
open `templates/Cms/index.html`. In order to make sure all your web pages have the same style and
structure, we recommend using template composition, so every endpoint template extends a main template called
`main.html` by convention (`src/main/resources/templates/main.html`):

For this:

- open `src/main/resources/templates/Cms/index.html`
- find the blog post nav
- add a link to edit the post, we want the "published: title" as text

<details>
    <summary>See solution</summary>

```html
{blogEntry.published}: {blogEntry.title}
```
</details>

You now see the blog post... Nice right? What about continuing with a touch of style?


### Style it up

Now let's use mvnpm to download NPM modules packaged as Maven modules, so that Bootstrap in our application.

**››› COPYING TIME 🫣**

Add the bootstrap dependencies to the `pom.xml`:

```xml
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>bootstrap</artifactId>
            <version>5.2.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>bootstrap-icons</artifactId>
            <version>1.11.3</version>
            <scope>provided</scope>
        </dependency>
```

The last bit missing is that we must reference the `cms` bundle from our main template
in order to get it injected in every page's `<head>` element.

Add this to the start of your `src/main/resources/templates/main.html`:

```html
<!DOCTYPE html>
<html>
    <head>
        <title>{#insert title/}</title>
        {#bundle key="cms"/}
    </head>
```

Now reload your main page and observe, better right 🎉?


Thanks to Web Bundler, we can write our style in SCSS, and it will be compiled on-the-fly to CSS. We also added a configuration to auto-import web dependencies scripts and style when it is possible. So nothing more to do.

In our case, we generate a `cms` bundle, so our styles and javascript must go under `web/cms` resources.

Have a look to `src/main/resources/web/cms/app.scss`, there is already a few things there.

### Editing blog entries (The Controller)

Now that we have our database model, let's work on our controller, which is responsible for
mapping HTTP URIs to Java actions, and defining our list of views in a type-safe manner, so
we know from the Java side what parameters they require.

We do this by declaring a `rest/Cms` class which extends `Controller`. It will serve endpoints
under the `/cms` path, and because its endpoints use a database, they are `@Blocking`.

Here, we see our index endpoint, which lists all blog entries, and pass them to
the `Cms/index.html` view (that we already edited), which takes a list of such blog entries. This is done via the
`@CheckedTemplate` annotation on a nested `Templates` class with `static native` methods,
one for each view we want to define.

Now, we want to click on a blog entry to edit its contents,
so let's add the ability to show a blog content per id on the index page.
We do this with a `Cms.editBlogEntry` method which has a `id` path parameter,
representing the blog entry we want to show. Notice that it can use the same template
as the `index` method, so they both share the same view. The main difference is that
one will have a `currentBlogEntry` set to `null` and the other to the blog entry
we want to display.


**››› CODING TIME**

Open the `src/main/java/rest/Cms.java` and look at `editBlogEntry`.. nothing wrong?

<details>
    <summary>See solution</summary>

```java
    public TemplateInstance editBlogEntry(@RestPath Long id) {
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        return Templates.index(BlogEntry.listAllSortedByPublished(), blogEntry);
    }
```
</details>

Nothing should change in the browser because we didn't add the link to access this.

Let's add it in `src/main/resources/templates/Cms/index.html` (where we added the title previously):
<details>
    <summary>See hint</summary>
    
    {uri:Cms.editBlogEntry(blogEntry.id)} will automatically resolve to the link of the blog entry edition
</details>

<details>
    <summary>See solution</summary>

```html
    <a href="{uri:Cms.editBlogEntry(blogEntry.id)}">{blogEntry.published}: {blogEntry.title}</a>
```
</details>


----------- NEXT PART IS NOT DONE YET


Now let's edit the view to display the blog entry's contents, in a `<textarea>` (not editable for now),
and add a link for each blog title, pointing to the blog entry page. In Renarde, you can use `uri:Controller.method(parameters)`
in order to create a URI to an endpoint method, making it extra easy to create links.

Note: here we start creating the `<form>` for later, but don't worry about it. The `{#authenticityToken/}` is required to make
sure all forms are not subject to CSRF attacks.

Now edit the view at `src/main/resources/templates/Cms/index.html`:

```html
{#include main.html}
{#title}Blogger CMS{/title}
<div class="left-bar d-flex justify-content-between">
    <div class="p-2">
        <div class="mb-3 d-flex">
			Top menu
        </div>

        <ul
                class="blogEntries list-group"
        >
            {#for blogEntry in blogEntries}
            <li class="{#if blogEntry.id == currentBlogEntry.id??}active {/if}list-group-item blogEntry d-flex justify-content-between align-items-center" >
                <a
                        href="{uri:Cms.editBlogEntry(blogEntry.id)}"
                >
                {blogEntry.published}: {blogEntry.title}
                </a>
            </li>
            {/for}
        </ul>
    </div>
    <div id="blog-editor" class="flex-grow-1 p-2">
        {#if currentBlogEntry}
                <form
                class="blogEntry-form" 
                method="post" 
                enctype='multipart/form-data'>
                    {#authenticityToken/}
                    <div class="editor-wrapper">
                        <div>
                            <input name="title" class="form-control {#ifError 'title'}is-invalid{/}" value="{inject:flash.get('title') ?: currentBlogEntry.title}" placeholder="Enter new title"/>
                            {#ifError 'title'}
                            <div class="invalid-feedback">
                                Error: {#error 'title'/}
                            </div>
                            {/ifError}
                        </div>
                        <div class="mb-3">
                            <input name="published" type="date" class="form-control {#ifError 'published'}is-invalid{/}" value="{inject:flash.get('published') ?: currentBlogEntry.published}" />
                            {#ifError 'published'}
                            <div class="invalid-feedback">
                                Error: {#error 'title'/}
                            </div>
                            {/ifError}
                        </div>
                        <div class="mb-3">
                            <textarea cols="50" rows="20" name="content" class="form-control {#ifError 'content'}is-invalid{/}" placeholder="Enter new content">{inject:flash.get('content') ?: currentBlogEntry.content}</textarea>
                            {#ifError 'content'}
                            <div class="invalid-feedback">
                                Error: {#error 'content'/}
                            </div>
                            {/ifError}
                        </div>
                    </div>
                    <button class="btn btn-primary">Save</button>
                </form>
        {/if}
    </div>
</div>
```

Go observe the page!

### Now let's do the save action

// At 9:30

In order to validate that we're not creating two entries with the same title, we must add a
query method to our model, we do this by declaring `static` methods on our model class, for
easier access and encapsulation.

Add the `getByTitle` method to `src/main/java/model/BlogEntry.java`:

```java
    public static Optional<BlogEntry> getByTitle(String title) {
        return BlogEntry.find("LOWER(title) = LOWER(?1)", title).firstResultOptional();
    }
```

Now we have to add our first mutating endpoint on our controller. Traditionally this is not done in `GET`
methods, so we have to add the `@POST` annotation. This method takes an `id` as path parameter, and
`title` and `content` form parameters. We use the `@NotBlank` annotation to add a validation constraint,
but keep in mind that you have to check for validation failure by calling the `validationFailed()` method,
and then trigger a redirect to the view containing the `<form>` in case of error.

You can trigger redirects from endpoints by just calling the endpoint you want to redirect to. Don't worry,
this never returns, so your endpoint is done the minute you call another endpoint method.

Often, as is the case here, we need to compose a mix of validation that can be described as annotations (with `@NotBlank`),
and code (with `BlogEntry.getByTitle`), so we proceed step by step, making sure we call `validationFailed()` until
we're ready to do our mutation, by modifying our entity.

Finally, we will redirect to the edited blog's view. It is always recommended to redirect to a `GET` method from a `POST`
method, to make sure page reloads don't trigger actions multiple times.

NOTE: `validationFailed()` will make sure all errors are pushed to the views which can render them using the `#ifError` and `#error`
tags, and will also push all endpoint parameters to the "flash" scope. This "flash" scope has the particularity of surviving
the next redirect. This makes sure that after you redirect to the page containing your `<form>`, you can then display every
validation error, and re-fill your form with the previous data, because it's bad form to throw away user values. This is what
we do in our views with `inject:flash.get('title') ?: currentBlogEntry.title`, which looks for a flashed value for `title`,
and if not, displays the unmodified database value from `currentBlogEntry.title`.

Add the `saveBlogEntry` method to `src/main/java/rest/Cms.java`:

```java
    @POST
    public void saveBlogEntry(@RestPath Long id, 
    		@RestForm @NotBlank String title, 
    		@RestForm String content,
    		@RestForm LocalDate published) {
        if (validationFailed()) {
            editBlogEntry(id);
        }
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        if (BlogEntry.getByTitle(title).filter(other -> other.id != id).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
        }
        if (validationFailed()) {
            editBlogEntry(id);
        }
        blogEntry.title = title;
        blogEntry.content = content;
        blogEntry.published = published;
        blogEntry.slug = Slug.toSlug(title);
        // save is automatic for managed entities
        editBlogEntry(id);
    }
```

> [!NOTE]
> The `src/main/java/util/Slug.java` class is a utility method used to derive
a Slug (for the url) from a blog entry title:

Now, make sure we tell our `form` where to find its action `Cms.saveBlogEntry` in `src/main/resources/templates/Cms/index.html`:


```html
                <form
                action="{uri:Cms.saveBlogEntry(currentBlogEntry.id)}"
                class="blogEntry-form" 
                method="post" 
                enctype='multipart/form-data'>
```

Go observe the page, try validation!

### Now let's do the new action

// At 11:00m

We want to add a button to add new blog entries, so we also need a controller endpoint to show a blank
blog entry in the `textarea`, as well as an action for when want to save this new blog entry: `saveNewBlogEntry`.

This action is very similar to `saveBlogEntry` except it does not require an `id` (this is a new entry), and
we create a new `BlogEntry` instance and make it persistent before redirecting to its view.

Add the `newBlogEntry` and `saveNewBlogEntry` methods to `src/main/java/rest/Cms.java`:

```java
    public TemplateInstance newBlogEntry() {
    	return Templates.index(BlogEntry.listAllSortedByPublished(), new BlogEntry());
    }
    
    @POST
    public void saveNewBlogEntry( 
    		@RestForm @NotBlank String title, 
    		@RestForm String content) {
        if (validationFailed()) {
            newBlogEntry();
        }
        if (BlogEntry.getByTitle(title).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
        }
        if (validationFailed()) {
            newBlogEntry();
        }
        BlogEntry blogEntry = new BlogEntry(title, content);
        // make it persistent
        blogEntry.persist();
        editBlogEntry(blogEntry.id);
    } 
```

Now replace `Top menu` with a button to create a new blog page in `src/main/resources/templates/Cms/index.html`:

```html
{#include main.html}
{#title}Blogger CMS{/title}
<div class="left-bar d-flex justify-content-between">
    <div class="p-2">
        <div class="mb-3 d-flex">
            <a
                    class="btn btn-outline-dark"
                    href="{uri:Cms.newBlogEntry()}"
            ><i class="bi bi-plus"></i> Post</a>
        </div>
```

And in the same file, make sure the `form` knows to call our `saveNewBlogEntry` action
in the case of new blog entries:

```html
                <form
                {#if currentBlogEntry.id}
                action="{uri:Cms.saveBlogEntry(currentBlogEntry.id)}"
                {#else}
                action="{uri:Cms.saveNewBlogEntry()}"
                {/if}
                class="blogEntry-form" 
                method="post" 
                enctype='multipart/form-data'>
```

Now observe the page!

### Add a way to delete entries

// At 12:20m

For deletion, since it's being called by HTML, and we want to put the action in a `<form>`, it
must be a `POST` method (we cannot call anything else than `GET` and `POST` in plain HTML).

We will define a new `deleteBlogEntry` endpoint, also parameterised by `id`, and call `delete()`
on the blog entry, before redirecting to the index page.

Add this method to your controller in `src/main/java/rest/Cms.java`:

```java
    @POST
    public void deleteBlogEntry(@RestPath Long id) {
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        blogEntry.delete();
        index();
    }
```

Now add a link to the action to your view at `src/main/resources/templates/Cms/index.html`:

```html
{#include main.html}
{#title}Blogger CMS{/title}
<div class="left-bar d-flex justify-content-between">
    <div class="p-2">
        <div class="mb-3 d-flex">
            <a
                    class="btn btn-outline-dark"
                    href="{uri:Cms.newBlogEntry()}"
            ><i class="bi bi-plus"></i> Post</a>
        </div>

        <ul
                class="blogEntries list-group"
        >
            {#for blogEntry in blogEntries}
            <li class="{#if blogEntry.id == currentBlogEntry.id??}active {/if}list-group-item blogEntry d-flex justify-content-between align-items-center" >
                <a
                        href="{uri:Cms.editBlogEntry(blogEntry.id)}"
                >
                {blogEntry.published}: {blogEntry.title}
                </a>
                <form action="{uri:Cms.deleteBlogEntry(blogEntry.id)}" method="post">
					{#authenticityToken/}
                    <button class="btn blogEntry-delete"
                    />
                        <i class="bi bi-trash"></i>
                    </button>
				</form>
            </li>
            {/for}
        </ul>
    </div>
…
```

Go observe the page!

### Add a Qute Component for Markdown

// At 13:40m

We would like to improve our `textarea` with an interactive Markdown editor. For that we will re-use and extend EasyMDE,
which is an NPM module.

Add this dependency to your `pom.xml`:

```xml
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>easymde</artifactId>
            <version>2.18.0</version>
            <scope>provided</scope>
        </dependency>
```

Thanks to Qute and Web Bundler, we can create Qute components that are defined as a combination of CSS, JavaScript and HTML.
To do that, we have to create three files in a folder with the following name convention `src/main/resources/web/<bundle-name>/<component-name>/`.
We will name our component `BlogEditor`.

Create a folder `src/main/resources/web/cms/BlogEditor` and add `src/main/resources/web/cms/BlogEditor/BlogEditor.css`:

```css
.editor-toolbar {
    border-top-left-radius: 0;
    border-top-right-radius: 0;
    padding: 5px 10px !important;
    background: lightgray;
}

.blog-editor {
    display: none;
}
```

Now add `src/main/resources/web/cms/BlogEditor/BlogEditor.html`:

```html
<textarea id="{id}" name="{name}" data-controller="blog-editor" class="form-control blog-editor {#ifError name}is-invalid{/}">{value}</textarea>
```

And add `src/main/resources/web/cms/BlogEditor/BlogEditor.js`:

```javascript
import EasyMDE from "easymde";
import "easymde/dist/easymde.min.css"
import { Controller } from "@hotwired/stimulus";
import StimulusApp from "../app";

StimulusApp.register("blog-editor", class extends Controller {
    connect() {
        console.log("init new editor")
        this.editor = new EasyMDE({ element: this.element, forceSync: true, spellChecker: false });
    }

    disconnect() {
        this.editor.toTextArea();
        this.editor.cleanup();
        console.log("cleanup");
    }
})
```

Now replace the `textarea` in `src/main/resources/templates/Cms/index.html` with:

```html
                            {#BlogEditor id="blogEntry-content" name="content" value=inject:flash.get('content').or(currentBlogEntry.content) /}
```

Go observe the page!

### Make it HTMX

// At 16:00m

At this point, we have our CMS ready, but it's all regular old-style HTML. We can turn it into a dynamic AJAX application by
using HTMX, without writing any JavaScript! Since HTMX is an NPM package, let's import it.

Add this dependency to your `pom.xml`:

```xml
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>htmx.org</artifactId>
            <version>1.9.11</version>
            <scope>provided</scope>
        </dependency>
```

To make sure your bundle uses it, add this import to your `app.js`:

```javascript
import "htmx.org"
```

The way HTMX works is that it defines a number of HTML custom tags that add dynamic behaviours to your HTML elements. 

For example, the `hx-ext` attribute allows us to define a transition for when content is swapped.

NOTE: Due to the CSRF mitigation explained before, we have to protect our AJAX/HTMX calls, and because `{#authenticityToken/}`
only works with regular `form` elements, we have to set up CSRF mitigation using headers, via the `hx-headers` attribute.

Replace the `body` tag in `src/main/resources/templates/main.html`:

```html
    <body hx-ext="morphdom-swap" hx-headers='{"{inject:csrf.headerName}":"{inject:csrf.token}"}'>
```

Now let's turn our blog page into a dynamic page.

The `hx-get` attribute will cause a click on your element to trigger an AJAX `GET` method to be called (instead of a regular `GET`
which would refresh the entire page). The same is true for other methods such as `hx-delete` and `hx-post`.

The `hx-target` attribute is a selector which defines what we do with the response of the AJAX
`GET`. For example, we could replace the current element, or another element. This allows for partial page updates.

The `hx-swap` attribute allows us to define how to replace the target, between replacing it entirely, or its contents, or its parent,
siblings, or deleting it.

The `hx-push-url` attribute makes sure that clicking on a dynamic action will update the current browser URL so that we have a history
to get back to. This is useful if you refresh your page. What is important to understand is that your page changes state on dynamic
actions, but if the user reloads the entire page, it should get in the same state with a full page rendering. This is easy when
the controller for a given URI has two outcomes: one for regular page loads, and one for HTMX requests (partial updates).

The last very important feature to understand is Qute fragments. Those are defined with `#fragment` and allow us to delineate sections
of our views that we want to be able to render dynamically. This is used for partial rendering. We specify the sections in the view, and
give them an `id` attribute. Then later in the controller we will show how to render them. During full page loads, these sections are
always rendered as normal.

Now add htmx to your `src/main/resources/templates/Cms/index.html` file:

```html
{#include main.html}
{#title}Blogger CMS{/title}
<div class="left-bar d-flex justify-content-between">
    <div class="p-2">
        <div class="mb-3 d-flex">
            <a
                    class="btn btn-outline-dark"
                    href="{uri:Cms.newBlogEntry()}"
                    hx-get="{uri:Cms.newBlogEntry()}"
                    hx-push-url="true"
                    hx-target="#blog-editor"
            ><i class="bi bi-plus"></i> Post</a>
        </div>

        {#fragment id="blogEntries"}
        <ul
                id="blogEntries"
                hx-swap-oob="true"
                class="blogEntries list-group"
        >
            {#for blogEntry in blogEntries}
            <li class="{#if blogEntry.id == currentBlogEntry.id??}active {/if}list-group-item blogEntry d-flex justify-content-between align-items-center" >
                <a
                        href="{uri:Cms.editBlogEntry(blogEntry.id)}"
                        hx-get="{uri:Cms.editBlogEntry(blogEntry.id)}"
                        hx-push-url="true"
                        hx-target="#blog-editor"
                        hx-swap="innerHTML"
                >
                {blogEntry.published}: {blogEntry.title}
                </a>
                {#if blogEntry.id != currentBlogEntry.id??}
                <button class="btn blogEntry-delete"
                        hx-delete="{uri:Cms.deleteBlogEntry(blogEntry.id)}"
                        hx-confirm="Are you sure?"
                        hx-target="closest .blogEntry"
                        hx-swap="outerHTML swap:0.5s"
                />
                    <i class="bi bi-trash"></i>
                </button>
                {/if}
            </li>
            {/for}
        </ul>
        {/fragment}
    </div>
    <div id="blog-editor" class="flex-grow-1 p-2">
        {#if currentBlogEntry}
            {#fragment id="blogEntryForm"}
                <form
                {#if currentBlogEntry.id}
                hx-post="{uri:Cms.saveBlogEntry(currentBlogEntry.id)}"
                {#else}
                hx-post="{uri:Cms.saveNewBlogEntry()}"
                {/if}
                class="blogEntry-form" 
                hx-encoding='multipart/form-data'
                hx-target="this"
                hx-push-url="true"
                >
                    <div class="editor-wrapper">
                        <div>
                            <input name="title" class="form-control {#ifError 'title'}is-invalid{/}" value="{inject:flash.get('title') ?: currentBlogEntry.title}" placeholder="Enter new title"/>
                            {#ifError 'title'}
                            <div class="invalid-feedback">
                                Error: {#error 'title'/}
                            </div>
                            {/ifError}
                        </div>
                        <div class="mb-3">
                            <input name="published" type="date" class="form-control {#ifError 'published'}is-invalid{/}" value="{inject:flash.get('published') ?: currentBlogEntry.published}" />
                            {#ifError 'published'}
                            <div class="invalid-feedback">
                                Error: {#error 'title'/}
                            </div>
                            {/ifError}
                        </div>
                        <div class="mb-3">
                            {#BlogEditor id="blogEntry-content" name="content" value=inject:flash.get('content').or(currentBlogEntry.content) /}
                            {#ifError 'content'}
                            <div class="invalid-feedback">
                                Error: {#error 'content'/}
                            </div>
                            {/ifError}
                        </div>
                    </div>
                    <button class="btn btn-primary">Save</button>
                </form>
            {/fragment}
        {/if}
    </div>
</div>
```

Our controller now has to be updated for HTMX. For convenience, we make it extend `HxController`, which has a number
of useful methods to help.

We start by defining signatures for our template fragments. The convention is that they will be named `<view>$<fragment-id>`,
so for example we have `index$blogEntries`, and again we define which parameters they take.

Then, we have to alter some of our endpoints so they can do partial rendering. You can use the `isHxRequest()` method to
check if we're doing a full page load, or a partial update. For HTMX requests, you can render fragments. You can also use
`concatTemplates` to render more than one fragment. HTMX will then place one fragment where specified by `hx-target`,
and the other will find its place using `hx-swap-oob`: this technique is called "out-of-bounds" replacement, and is very
handy to update several parts of the page at once.

The last thing we can do is turn our `deleteBlogEntry` action from a `POST` method to a `DELETE` method since we can
invoke those using HTMX/AJAX (unlike HTML `form`).

And to your controller at `src/main/java/rest/Cms.java`:

```java
package rest;

import java.time.LocalDate;
import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkiverse.renarde.htmx.HxController;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import model.BlogEntry;
import util.Slug;

// Define a new controller, blocking (DB operations), at /cms
@Blocking
@Path("/cms")
public class Cms extends HxController {
    
    /**
     * This defines templates available in src/main/resources/templates/Classname/method.html by convention
     */
    @CheckedTemplate
    public static class Templates {
        /**
         * This specifies that the Cms/index.html template takes a list of blogEntries as parameter
         */
        public static native TemplateInstance index(List<BlogEntry> blogEntries, BlogEntry currentBlogEntry);

        public static native TemplateInstance index$blogEntries(List<BlogEntry> blogEntries, BlogEntry currentBlogEntry);

        public static native TemplateInstance index$blogEntryForm(BlogEntry currentBlogEntry);
    }

    // set up our blog index page at /cms
    @Path("")
    public TemplateInstance index() {
        if (isHxRequest()) {
            return Templates.index$blogEntries(BlogEntry.listAllSortedByPublished(), null);
        }
        return Templates.index(BlogEntry.listAllSortedByPublished(), null);
    }

    public TemplateInstance newBlogEntry() {
        if (isHxRequest()) {
            return concatTemplates(Templates.index$blogEntries(BlogEntry.listAllSortedByPublished(), null),
                    Templates.index$blogEntryForm(new BlogEntry()));
        }
    	return Templates.index(BlogEntry.listAllSortedByPublished(), new BlogEntry());
    }

    public TemplateInstance editBlogEntry(@RestPath Long id) {
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        if (isHxRequest()) {
            return concatTemplates(Templates.index$blogEntries(BlogEntry.listAllSortedByPublished(), blogEntry),
                    Templates.index$blogEntryForm(blogEntry));
        }
        return Templates.index(BlogEntry.listAllSortedByPublished(), blogEntry);
    }

    @POST
    public void saveBlogEntry(@RestPath Long id, 
    		@RestForm @NotBlank String title, 
    		@RestForm String content,
            @RestForm LocalDate published) {
        if (validationFailed()) {
            editBlogEntry(id);
        }
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        if (BlogEntry.getByTitle(title).filter(other -> other.id != id).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
        }
        if (validationFailed()) {
            editBlogEntry(id);
        }
        blogEntry.title = title;
        blogEntry.content = content;
        blogEntry.published = published;
        blogEntry.slug = Slug.toSlug(title);
        // save is automatic for managed entities
        editBlogEntry(id);
    }
    
    @POST
    public void saveNewBlogEntry( 
    		@RestForm @NotBlank String title, 
    		@RestForm String content) {
        if (validationFailed()) {
            newBlogEntry();
        }
        if (BlogEntry.getByTitle(title).isPresent()) {
            validation.addError("title", String.format("A blog entry with the title [%s] already exists", title));
        }
        if (validationFailed()) {
            newBlogEntry();
        }
        BlogEntry blogEntry = new BlogEntry(title, content);
        // make it persistent
        blogEntry.persist();
        editBlogEntry(blogEntry.id);
    }

    @DELETE
    public String deleteBlogEntry(@RestPath("id") Long id) {
        onlyHxRequest();
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        blogEntry.delete();
        // HTMX is not a fan of 204 No Content for swapping https://github.com/bigskysoftware/htmx/issues/1130
        return "";
    }
}
```

Now observe the page!

// At 19:00m
