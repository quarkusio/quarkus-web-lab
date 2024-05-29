## 1- The CMS (~60m)

### Extensions

 - [Renarde](https://docs.quarkiverse.io/quarkus-renarde/dev/index.html)
 - [Web Bundler](https://docs.quarkiverse.io/quarkus-web-bundler/dev/index.html)
 - [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
 - [JDBC driver for H2](https://quarkus.io/guides/datasource)

### Get the initial app

We could start from scratch, but it is nicer to start with a few things ready. If you haven't already, [set up your environment and clone this repository](../README.md#getting-your-environment-ready).

The directory which contains this README also contains the _initial version_ of the app. Use GitHub or a markdown viewer to read this with the best experience.

```shell
cd 1-cms
```

There are some comments starting with `TODO:` in the code.
It's up to you to remove these comments with appropriate code!

It's time to start Quarkus dev:
```shell
./mvnw quarkus:dev
```

🚀 Press `w` and observe your web page, it should be poor for now.

### Let's have a look to the Quarkus config

👀  Open the `src/main/resources/application.properties` file, those setting are allowing
us to run on a custom port, connect to a shared H2 database, and update our schema when needed.

### The Model

The first thing to do is to represent our persistent model, that will be our Java representation of the
blog entry database table.

👀 Open the `src/main/java/model/BlogEntry.java`. This
contains our main entity for the lab, representing a blog entry. It will have the following attributes:

- Title (must be unique)
- Slug (derived from the title)
- Picture (picture file name located in `src/main/resources/web/static/assets/blog/pictures`, e.g. eat-cheese.jpg)
- Content (the blog contents, in Markdown)
- Published (the publication date)

This is a Panache entity, so it extends `PanacheEntity` to get a lot of useful methods, and its fields
are public. We will need a default constructor and a constructor with `title`, `content`, and optionally
`publishedDate`. 

### Time to show blog posts (The View)

👀 The app is set up with initial data. Traditionally, startup actions belong in a `util/Startup` class (`src/main/java/util/Startup.java`), and we can use that to create and
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

In `src/main/java/model/BlogEntry.java`:

```java
    public static List<BlogEntry> listAllSortedByPublished() {
        return BlogEntry.listAll(Sort.by("published").descending());
    }
```
</details>

🚀 On the browser, after manually refreshing, you should now have a list of "foo", let's show the actual posts.

In Renarde, by default, all views live in `templates/<Controller>/<method>.html`, so for `Cms.index` we need to
open `templates/Cms/index.html`. In order to make sure all your web pages have the same style and
structure, we recommend using template composition, so every endpoint template extends a main template called
`main.html` by convention (`src/main/resources/templates/main.html`):

For this:

- open `src/main/resources/templates/Cms/index.html`
- find the blog posts `{#for}` loop
- add the Qute expressions to print something like "2024-03-02: My blog post"  in the loop

<details>
<summary>See hint</summary>

Use `{blogEntry.field}` to print a field value.
</details>

<details>
<summary>See solution</summary>

In `src/main/resources/templates/Cms/index.html`, add this:

```html
{blogEntry.published}: {blogEntry.title}
```
</details>

🚀 You now see the blog post... Nice right? What about continuing with a touch of style?


### Style it up

Now let's use mvnpm to download NPM modules packaged as Maven modules, so that Bootstrap in our application.

**››› COPYING TIME 🫣**

We must reference the `cms` bundle from our main template
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

🚀🔑 Refresh your browser, nothing new? You just unlocked browser live-reload, no more manual refresh 😇

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

🚀 Now observe in the browser, better right 🎉?

Thanks to Web Bundler, we can write our style in SCSS, and it will be compiled on-the-fly to CSS. We also added a configuration to auto-import web dependencies scripts and style when it is possible. So nothing more to do.

In our case, we generate a `cms` bundle, so our styles and javascript must go under `web/cms` resources.

👀 Have a look to `src/main/resources/web/cms/app.scss`, there is already a few things there.

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
<summary>See hint</summary>

Use one of the `BlogEntry` static methods to find the right entry and `notFoundIfNull` from the parent Controller to return an error if null.
</details>

<details>
<summary>See solution</summary>

In `src/main/java/rest/Cms.java`, find the `editBlogEntry` and replace what's needed: 

```java
// Find the blog entry
BlogEntry blogEntry = BlogEntry.findById(id);
// Fail if null
notFoundIfNull(blogEntry);
// Show the index with the given entry in the editor
return Templates.index(BlogEntry.listAllSortedByPublished(), blogEntry);
```
</details>

Even if there is already a base `<form>` to get you started, nothing should change in the browser because we didn't add the link to access this.

Let's add it in `src/main/resources/templates/Cms/index.html` (where we added the date and name previously):
<details>
<summary>See hint</summary>

In Renarde, you can use `uri:Controller.method(parameters)`
in order to create a URI to an endpoint method, making it extra easy to create links.

`{uri:Cms.editBlogEntry(blogEntry.id)}` resolves to the link of the blog entry edition.
</details>

<details>
<summary>See solution</summary>

In `src/main/resources/templates/Cms/index.html, find, the blog post date and name in the loop and replace with a link:

```html
    <a href="{uri:Cms.editBlogEntry(blogEntry.id)}">{blogEntry.published}: {blogEntry.title}</a>
```
</details>

🚀 Hop over to your browser, click on one blog post, you should see the editor now (with the content printed for now).

_So far so good, you are doing great, keep up 👍!_


### The Markdown Editor (Qute Components)

Now let's display the blog entry's markdown editor.

> [!WARNING]  
> The `{#authenticityToken/}` is required in the `<form>` to make
sure all forms are not subject to CSRF attacks.

👀 If you have a look to `src/main/resources/web/cms/BlogEditor/`, you will find a style, html and js for what we call a qute-component. 
You can use it like any Qute tag: `{#BlogEditor id=... name=... value=... /}`

> [!NOTE] 
> This BlogEditor uses 'Easymde' web library to provide the Markdown editor.
> 'Stimulus' a light library to help binding the Markdown editor with dom lifecycle (in the js file), it requires some initialization done in app.js.
> Both of those libraries are in the pom.xml and already bundled through the Quarkus Web Bundler.

**››› CODING TIME**

Use the `{#BlogEditor /}` in the edition `<form>`.

<details>
<summary>See hint</summary>

- You can directly pass arguments to Qute tags `{#tag foo=myObj.foo /}`.
- `id` is not the blog post id, but the html element id (needed by Easymde).
- `value=currentBlogEntry.content` would have worked, but `value=inject:flash.get('content').or(currentBlogEntry.content)` will allow to keep the state in case of error (no worries, this is covered later).
</details>

<details>
<summary>See solution</summary>

In `src/main/resources/templates/Cms/index.html, find the content edition part of the form and replace with this:
 

```html
     {#BlogEditor id="blogEntry-content" name="content" value=inject:flash.get('content').or(currentBlogEntry.content) /}
```
</details>

🚀 There you go, check your browser and find a nice Markdown editor for your blog post!

### Now let's do the save action

In order to validate that we're not creating two entries with the same title, we must add a
query method to our model, we do this by declaring `static` methods on our model class, for
easier access and encapsulation.

**››› CODING TIME**

Implement the `getByTitle` method in `src/main/java/model/BlogEntry.java`:

<details>
<summary>See solution</summary>

```java
    public static Optional<BlogEntry> getByTitle(String title) {
        return BlogEntry.find("LOWER(title) = LOWER(?1)", title).firstResultOptional();
    }
```
</details>

Now we need to use our first mutating endpoint on our controller. Traditionally this is not done in `GET`
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

> [!NOTE] 
> `validationFailed()` will make sure all errors are pushed to the views which can render them using the `#ifError` and `#error`
> tags, and will also push all endpoint parameters to the "flash" scope. This "flash" scope has the particularity of surviving
> the next redirect. This makes sure that after you redirect to the page containing your `<form>`, you can then display every
> validation error, and re-fill your form with the previous data, because it's bad form to throw away user values. This is what
> we do in our views with `inject:flash.get('title') ?: currentBlogEntry.title`, which looks for a flashed value for `title`,
> and if not, displays the unmodified database value from `currentBlogEntry.title`.

**››› CODING TIME**

Edit the `saveBlogEntry` method in `src/main/java/rest/Cms.java`:

<details>
<summary>See hint</summary>

- You already implemented the `BlogEntry.getByTitle` so no need to touch the check.
- As explained above, to redirect, just call the instance endpoint method you wish to redirect to.
- 👀 The `src/main/java/util/Slug.java` class is a utility class used to derive
  a Slug (for the url) from a blog entry title.
- To save the data, just assign it to your `blogEntry`, it will be saved automatically.
</details>

<details>
<summary>See solution</summary>

Find `saveBlogEntry` in `src/main/java/rest/Cms.java`.

Redirect in case of validation error (errors will be stored in flash by `validationFailed`):

```java
    if (validationFailed()) {
        editBlogEntry(id);
        // Redirect to this blog entry (no need to return, this will stop here)
    }
```

Set the blog entry with the new values:

```java
    blogEntry.title = title;
    blogEntry.content = content;
    blogEntry.published = published;
    blogEntry.slug = Slug.toSlug(title);
    // save is automatic for managed entities

    // Now that it's saved, redirect to the same editor with updated data
    editBlogEntry(id);
}
```
</details>

Now, make sure we tell our `<form>` where to find its action `{uri:Cms.saveBlogEntry(currentBlogEntry.id)}` in `src/main/resources/templates/Cms/index.html`. As you can see it's already done, and it also deals with new post creation (the next part).

🚀 Go observe the page, save!

### Now let's do the action to create new post

We want to add a button to add new blog entries, so we also need a controller endpoint to show a blank
blog entry in the editor `newBlogEntry`, as well as a POST action for when want to save this new blog entry: `saveNewBlogEntry`.

**››› CODING TIME**

Now replace `Top menu` icon with a link to create a new blog page in `src/main/resources/templates/Cms/index.html`:

<details>
<summary>See hint</summary>

Use a `<a>` with `class="btn btn-outline-dark"`, the link should point to `Cms.newBlogEntry`.
</details>

<details>
<summary>See solution</summary>

```html
    <a class="btn btn-outline-dark" href="{uri:Cms.newBlogEntry()}"><i class="bi bi-plus"></i> Post</a>
```
</details>

🚀 In the page, click on the new button and find a blank editor for your new post. 
It's not done yet though, we need to wire the save.

This action is very similar to `saveBlogEntry` except it does not require an `id` (this is a new entry), and
we create a new `BlogEntry` instance and make it persistent before redirecting to its view.

Add the `newBlogEntry` and `saveNewBlogEntry` methods to `src/main/java/rest/Cms.java`:

<details>
<summary>See hint</summary>

Calling `persist()` on your hibernate entity will make it persisted
</details>

<details>
<summary>See solution</summary>

```java 
    BlogEntry blogEntry = new BlogEntry(title, picture, content, published);
    // make it persistent
    blogEntry.persist();
    
    //  it now exists, let's keep the editor open
    editBlogEntry(blogEntry.id);
```
</details>

🚀 In the browser, now, add some content (use `well-done.jpg` as picture) and save your first new post 🍿

### Add a way to delete entries

For deletion, since it's being called by HTML, and we want to put the action in a `<form>`, it
must be a `POST` method (we cannot call anything else than `GET` and `POST` in plain HTML).

We will define a new `deleteBlogEntry` endpoint, also parameterised by `id`, and call `delete()`
on the blog entry, before redirecting to the index page.

**››› CODING TIME**

Implement the `deleteBlogEntry` method to your controller in `src/main/java/rest/Cms.java`:

<details>
<summary>See hint</summary>

Call `delete()` on your hibernate entity to delete it and redirect to index.
</details>

<details>
<summary>See solution</summary>

```java
    @POST
    public void deleteBlogEntry(@RestPath Long id) {
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        blogEntry.delete();
        index();
    }
```
</details>

Now add the action to your view at `src/main/resources/templates/Cms/index.html`:

<details>
<summary>See solution</summary>

Just after the link `</a>` the edit the blog post, add this:

```html
<form action="{uri:Cms.deleteBlogEntry(blogEntry.id)}" method="post" onsubmit="return confirm('Do you really want to delete this blog post?');">
  {#authenticityToken/}
  <button class="btn blogEntry-delete"
  />
  <i class="bi bi-trash"></i>
  </button>
</form>
```
</details>

🚀 Go play with your homemade CMS!

You achieved the CMS part 👍, time to show the content in a blog website [Part 2 - The Blog](../2-blog).

---

### (Optional) Make it HTMX

_If you feel like it, we added an extra section to add HTMX power to your CMS. This part is not as detailed, this is more something to play with at home._

At this point, we have our CMS ready, but it's all regular old-style HTML. We can turn it into a dynamic AJAX application by
using HTMX, without writing any JavaScript! Since HTMX is an NPM package, let's import it.

Add this dependency to your `pom.xml` to make it part of your bundle:

```xml
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>htmx.org</artifactId>
            <version>1.9.11</version>
            <scope>provided</scope>
        </dependency>
```

The way HTMX works is that it defines a number of HTML custom tags that add dynamic behaviours to your HTML elements. 

For example, the `hx-ext` attribute allows us to define a transition for when content is swapped.

> [!NOTE] 
> Due to the CSRF mitigation explained before, we have to protect our AJAX/HTMX calls, and because `{#authenticityToken/}`
> only works with regular `form` elements, we have to set up CSRF mitigation using headers, via the `hx-headers` attribute.

Replace the `body` tag in `src/main/resources/templates/main.html`:

```html
    <body hx-headers='{"{inject:csrf.headerName}":"{inject:csrf.token}"}'>
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

See solution in: [../solution/1-cms/src/main/resources/templates/Cms/index.html](../solution/1-cms/src/main/resources/templates/Cms/index.html)

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

See solution in: [../solution/1-cms/src/main/java/rest/Cms.java](../solution/1-cms/src/main/java/rest/Cms.java)

Now observe the page!

