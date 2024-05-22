## The CMS

For now we need to use Quarkus 3.11.0.CR1

### Extensions

 - [Renarde](https://docs.quarkiverse.io/quarkus-renarde/dev/index.html)
 - [Web Bundler](https://docs.quarkiverse.io/quarkus-web-bundler/dev/index.html)

### Create initial app

```
mvn io.quarkus.platform:quarkus-maven-plugin:3.11.0.CR1:create -DplatformVersion=3.11.0.CR1 -DprojectGroupId=web.workshop -DprojectArtifactId=cms \
 -Dextensions='io.quarkiverse.renarde:quarkus-renarde, io.quarkiverse.web-bundler:quarkus-web-bundler:1.5.0.CR2, hibernate-orm-panache, jdbc-h2'
```

### Configure some things

Open the `src/main/resources/application.properties` file and set these:

```
# Start serving on port 9090
quarkus.http.port=9090
# Make sure we can send large blog entries
quarkus.http.limits.max-form-attribute-size=10K
# Configure H2 so it can be shared with the other parts of the lab
quarkus.datasource.jdbc.url=jdbc:h2:../db;AUTO_SERVER=TRUE
# Start with fresh data every time
quarkus.hibernate-orm.database.generation=drop-and-create
```

### Set up your model

Open the `src/main/java/model/Todo.java` file and rename it to `BlogEntry.java`:

```java
package model;

import java.util.Date;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import util.Slug;

@Entity
public class BlogEntry extends PanacheEntity {

    @Column(unique = true)
    public String title;

    public String slug;

    @Column(columnDefinition = "text")
    public String content;

    public Date created = new Date();

    public Date updated = new Date();

    public BlogEntry() {
        super();
    }

    public BlogEntry(String title, String content) {
        this.title = title;
        this.slug = Slug.toSlug(title);
        this.content = content;
    }
}
```

Add the `src/main/java/util/Slug.java` file:

```java
package util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Slug {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");

    public static String toSlug(String input) {
        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
```

Remove the unwanted `src/main/java/web/` package and its contents.

### Set up your controller

Open the `src/main/java/rest/Todos.java` file and rename it to `Cms.java`:

```java
package rest;

import java.util.List;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.Path;
import model.BlogEntry;

// Define a new controller, blocking (DB operations), at /cms
@Blocking
@Path("/cms")
public class Cms extends Controller {
    
    /**
     * This defines templates available in src/main/resources/templates/Classname/method.html by convention
     */
    @CheckedTemplate
    public static class Templates {
        /**
         * This specifies that the Cms/index.html template takes a list of blogEntries as parameter
         */
        public static native TemplateInstance index(List<BlogEntry> blogEntries);
    }

    // set up our blog index page at /cms
    @Path("")
    public TemplateInstance index() {
        return Templates.index(BlogEntry.listAll());
    }
}
```

Make sure we redirect from `/` to `/cms`, by adding the `src/main/java/rest/Application.java` class:

```java
package rest;

import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.Path;

public class Application extends Controller {
	// redirect / to /cms
	@Path("/")
	public void redirectToCms() {
		redirect(Cms.class).index();
	}
}
```

## Set up initial data

Open the `src/main/java/util/Startup.java` file and edit it:

```java
package util;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import model.BlogEntry;

@ApplicationScoped
public class Startup {
    /**
     * This method is executed at the start of your application
     */
	@Transactional
    public void start(@Observes StartupEvent evt) {
        // in DEV mode we seed some data
        if(LaunchMode.current() == LaunchMode.DEVELOPMENT) {
        	BlogEntry blogEntry = new BlogEntry("How to make cheese", "The process is easy, here's how to set it up in 12 steps. The last one will surprise you.");
        	blogEntry.persist();

        	blogEntry = new BlogEntry("How to eat cheese", "The process is easy, here's how to set it up in 12 steps. The last one will surprise you.");
        	blogEntry.persist();
        }
    }
}
```

## Now display the blogs

- Remove the `src/main/resources/templates/Todos/todo.html`
- Rename the `src/main/resources/templates/Todos/` folder to `src/main/resources/templates/Cms/`

Edit the `src/main/resources/templates/main.html` file to:

```html
<!DOCTYPE html>
<html>
    <head>
        <title>{#insert title/}</title>
    </head>
    <body>
        <nav class="navbar sticky-top bg-dark border-bottom border-body" data-bs-theme="dark">
            <div class="container-fluid">
              <a class="navbar-brand" href="#">
                <img src="/static/assets/images/logo.svg" alt="Logo" width="30" height="24" class="d-inline-block align-text-top">
                {#insert title/}
              </a>
            </div>
        </nav>
        {#insert /}
    </body>
</html>
```

Edit the `src/main/resources/templates/Cms/index.html` file to:

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
            <li class="list-group-item blogEntry d-flex justify-content-between align-items-center" >
                {blogEntry.title}
            </li>
            {/for}
        </ul>
    </div>
</div>
```

### Give it a spin!

Now start Quarkus:

```shell
$ ./mvnw quarkus:dev
```

Press `w` and observe your web page.

### Style it up

// At 5 minutes

Download https://quarkus.io/assets/images/brand/quarkus_icon_reverse.svg and save it at `src/main/resources/web/static/assets/images/logo.svg`.

Add the bootstrap dependencies to the `pom.xml`:

```xml
        <dependency>
            <groupId>org.mvnpm.at.hotwired</groupId>
            <artifactId>stimulus</artifactId>
            <version>3.2.2</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>bootstrap</artifactId>
            <version>5.3.3</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>bootstrap-icons</artifactId>
            <version>1.11.3</version>
            <scope>provided</scope>
        </dependency>
```

Rename the `src/main/resources/web/app` to `src/main/resources/web/cms`.

Edit the `app.scss`:

```css
body {
    background-color: white;
}

.blogEntries {
    min-width: 400px;
}
.blogEntry {
    padding: 0;
    width: 400px;
}
.blogEntry a, .blogEntry button, .blogEntry > i {
    padding: .5rem 1rem;
}
.blogEntry a {
    cursor: pointer;
    flex-grow: 1;
    display: block;
    color: #333;
    text-decoration: none;
}
.list-group-item.active a {
    color: white;
}
.blogEntry button.blogEntry-delete  { color: #ccc; visibility: hidden;  }
.blogEntry button.blogEntry-delete:hover { color: red; }
.blogEntry:hover button.blogEntry-delete { visibility: visible; }

.blogEntry.htmx-swapping a, .blogEntry.htmx-swapping button {
    opacity: 0;
    transition: opacity 1s ease-out;
}

.blogEntry-form .editor-wrapper {
    min-height: 450px;
}

.EasyMDEContainer {
    padding-top: 16px;
}
```

Edit the `app.js`:


```javascript
import "bootstrap/scss/bootstrap.scss";
import "bootstrap-icons/font/bootstrap-icons.scss";
import { Application } from "@hotwired/stimulus";

const StimulusApp = Application.start();
export default StimulusApp;
```

Add this to `src/main/resources/application.properties`:

```properties
# Bundle the CMS things
quarkus.web-bundler.bundle.cms=true
quarkus.web-bundler.bundle.cms.qute-tags=true
```

Add this to the start of your `src/main/resources/templates/main.html`:

```html
<!DOCTYPE html>
<html>
    <head>
        <title>{#insert title/}</title>
        {#bundle key="cms"/}
    </head>
```

Now reload your main page and observe!

### Displaying blog entries

// At 8:30

Now, we want to click on a blog entry to show its contents,
so let's add the ability to show a blog content per id on the index page.

Edit the `src/main/java/rest/Cms.java` class:

```java
package rest;

import java.util.List;

import org.jboss.resteasy.reactive.RestPath;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.Path;
import model.BlogEntry;

// Define a new controller, blocking (DB operations), at /cms
@Blocking
@Path("/cms")
public class Cms extends Controller {
    
    /**
     * This defines templates available in src/main/resources/templates/Classname/method.html by convention
     */
    @CheckedTemplate
    public static class Templates {
        /**
         * This specifies that the Cms/index.html template takes a list of blogEntries as parameter
         */
        public static native TemplateInstance index(List<BlogEntry> blogEntries, BlogEntry currentBlogEntry);
    }

    // set up our blog index page at /cms
    @Path("")
    public TemplateInstance index() {
        return Templates.index(BlogEntry.listAll(), null);
    }

    public TemplateInstance editBlogEntry(@RestPath Long id) {
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        return Templates.index(BlogEntry.listAll(), blogEntry);
    }
}
```

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
                {blogEntry.title}
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

Add the `getByTitle` method to `src/main/java/model/BlogEntry.java`:

```java
    public static Optional<BlogEntry> getByTitle(String title) {
        return BlogEntry.find("LOWER(title) = LOWER(?1)", title).firstResultOptional();
    }
```

Add the `saveBlogEntry` method to `src/main/java/rest/Cms.java`:

```java
    @POST
    public void saveBlogEntry(@RestPath Long id, 
    		@RestForm @NotBlank String title, 
    		@RestForm String content) {
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
        blogEntry.updated = new Date();
        blogEntry.slug = Slug.toSlug(title);
        // save is automatic for managed entities
        editBlogEntry(id);
    }
```

Now, plug the `form` in `src/main/resources/templates/Cms/index.html`:


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

Add the `newBlogEntry` and `saveNewBlogEntry` methods to `src/main/java/rest/Cms.java`:

```java
    public TemplateInstance newBlogEntry() {
    	return Templates.index(BlogEntry.listAll(), new BlogEntry());
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

Now replace `Top menu` with a link to create a new blog page in `src/main/resources/templates/Cms/index.html`:

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

And in the same file, plug in the `saveNewBlogEntry` method for our `form`:

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

Add this method to your controller in `src/main/java/rest/Cms.java`:

```java
    @POST
    public void deleteBlogEntry(@RestPath("id") Long id) {
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
                {blogEntry.title}
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

Add this dependency to your `pom.xml`:

```xml
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>easymde</artifactId>
            <version>2.18.0</version>
            <scope>provided</scope>
        </dependency>
```

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

Add this dependency to your `pom.xml`:

```xml
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>htmx.org</artifactId>
            <version>1.9.11</version>
            <scope>provided</scope>
        </dependency>
```

Add this import to your `app.js`:

```javascript
import "htmx.org"
```

Replace the `body` tag in `src/main/resources/templates/main.html`:

```html
    <body hx-ext="morphdom-swap" hx-headers='{"{inject:csrf.headerName}":"{inject:csrf.token}"}'>
```

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
                {blogEntry.title}
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

And to your controller at `src/main/java/rest/Cms.java`:

```java
package rest;

import java.util.Date;
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
            return Templates.index$blogEntries(BlogEntry.listAll(), null);
        }
        return Templates.index(BlogEntry.listAll(), null);
    }

    public TemplateInstance newBlogEntry() {
        if (isHxRequest()) {
            this.hx(HxResponseHeader.TRIGGER, "refreshEntries");
            return concatTemplates(Templates.index$blogEntries(BlogEntry.listAll(), null),
                    Templates.index$blogEntryForm(new BlogEntry()));
        }
    	return Templates.index(BlogEntry.listAll(), new BlogEntry());
    }

    public TemplateInstance editBlogEntry(@RestPath Long id) {
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        if (isHxRequest()) {
            this.hx(HxResponseHeader.TRIGGER, "refreshEntries");
            return concatTemplates(Templates.index$blogEntries(BlogEntry.listAll(), blogEntry),
                    Templates.index$blogEntryForm(blogEntry));
        }
        return Templates.index(BlogEntry.listAll(), blogEntry);
    }

    @POST
    public void saveBlogEntry(@RestPath Long id, 
    		@RestForm @NotBlank String title, 
    		@RestForm String content) {
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
        blogEntry.updated = new Date();
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
