## The CMS

For now we need to use Quarkus 3.11.0.CR1

### Extensions

 - [Renarde](https://docs.quarkiverse.io/quarkus-renarde/dev/index.html)
 - [Web Bundler](https://docs.quarkiverse.io/quarkus-web-bundler/dev/index.html)
 - [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
 - [JDBC driver for H2](https://quarkus.io/guides/datasource)

### Create initial app

Use the Quarkus Maven plugin to create an application with the required extensions

```
mvn io.quarkus.platform:quarkus-maven-plugin:3.11.0.CR1:create -DplatformVersion=3.11.0.CR1 -DprojectGroupId=web.workshop -DprojectArtifactId=cms \
 -Dextensions='io.quarkiverse.renarde:quarkus-renarde, io.quarkiverse.web-bundler:quarkus-web-bundler:1.5.0.CR2, hibernate-orm-panache, jdbc-h2'
```

### Configure some things

Open the `src/main/resources/application.properties` file and set these settings, allowing
us to run on a custom port, connect to a shared H2 database, and re-create our data every time.

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

The first thing to do is to represent our persistent model, that will be our Java representation of the
blog entry database table.

Open the `src/main/java/model/Todo.java` file and rename it to `BlogEntry.java`. This
contains our main entity for the lab, representing a blog entry. It will have the following attributes:

- Title (must be unique)
- Slug (derived from the title)
- Content (the blog contents, in Markdown)
- Published (the publication date)

This is a Panache entity, so it extends `PanacheEntity` to get a lot of useful methods, and its fields
are public. We will need a default constructor and a constructor with `title`, `content`, and optionally
`publishedDate`. We also define a query method for getting all entries sorted by publishing date:

```java
package model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
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

    public LocalDate published;

    public BlogEntry() {
        super();
    }

    public BlogEntry(String title, String content) {
        this(title, content, LocalDate.now());
    }
    
    public BlogEntry(String title, String content, LocalDate published) {
        this.title = title;
        this.content = content;
        this.slug = Slug.toSlug(title);
        this.published = published;
    }

    public static List<BlogEntry> listAllSortedByPublished() {
        return BlogEntry.listAll(Sort.by("published").descending());
    }
}
```

Add the `src/main/java/util/Slug.java` class. This is a utility method used to derive
a Slug from a blog entry title:

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

Now that we have our database model, let's work on our controller, which is responsible for
mapping HTTP URIs to Java actions, and defining our list of views in a type-safe manner, so
we know from the Java side what parameters they require.

We do this by declaring a `rest/Cms` class which extends `Controller`. It will serve endpoints
under the `/cms` path, and because its endpoints use a database, they are `@Blocking`.

Here, we start by defining our index endpoint, which lists all blog entries, and pass them to
the `Cms/index.html` view, which takes a list of such blog entries. This is done via the
`@CheckedTemplate` annotation on a nested `Templates` class with `static native` methods,
one for each view we want to define.

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
        return Templates.index(BlogEntry.listAllSortedByPublished());
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

Traditionally, startup actions belong in a `util/Startup` class, and we can use that to create and
save test database values. To that end, we mark the method as `@Transactional`, and only create
the test data in DEV mode.

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
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
            BlogEntry blogEntry = new BlogEntry("How to make cheese",
                    "The process is easy, here's how to set it up in 12 steps. The last one will surprise you.");
            blogEntry.persist();

            blogEntry = new BlogEntry("How to eat cheese",
                    "The process is easy, here's how to set it up in 12 steps. The last one will surprise you.");
            blogEntry.persist();

            blogEntry = new BlogEntry("Getting Started with Quarkus", """
                    Quarkus is a revolutionary framework designed to optimize Java for Kubernetes and cloud-native environments. Combining a fast startup time, low memory footprint, and a developer-friendly approach, Quarkus is rapidly gaining popularity. In this post, we will explore what makes Quarkus stand out and how to get started with it.
                    
                    ## What is Quarkus?
                    
                    Quarkus is a Kubernetes-native Java framework tailored for GraalVM and HotSpot. It focuses on making Java a leading platform in Kubernetes environments by offering:
                    
                    - **Supersonic Subatomic Java**: Quarkus boasts fast startup times and low memory usage, which are critical for cloud-native applications.
                    - **Developer Joy**: Features like live coding and unified configuration aim to enhance the developer experience.
                    - **Reactive and Imperative**: Quarkus supports both reactive and imperative programming models, giving developers flexibility in their approach.
                    
                    ## Key Features
                    
                    ### 1. Fast Startup and Low Memory Footprint
                    
                    Quarkus applications start up quickly and consume minimal memory, making them ideal for containerized environments. This efficiency is achieved through techniques like ahead-of-time (AOT) compilation with GraalVM.
                    
                    ### 2. Live Coding
                    
                    Quarkus live coding feature allows developers to see code changes in real-time without restarting the application. This significantly speeds up the development process and reduces downtime.
                    
                    ### 3. Unified Configuration
                    
                    Quarkus simplifies configuration management by providing a single configuration file for all environments. This consistency helps avoid configuration drift and ensures smoother deployments.
                    
                    ### 4. Extensible Framework
                    
                    Quarkus offers a wide range of extensions for various technologies, including Hibernate, RESTEasy, and Kafka. These extensions integrate seamlessly with Quarkus, allowing developers to build complex applications with ease.
                    
                    ## Getting Started
                    
                    To get started with Quarkus, follow these simple steps:
                    
                    ### Step 1: Install Quarkus CLI
                    
                    First, install the Quarkus command-line interface (CLI):
                    
                    ```bash
                    curl -Ls https://sh.quarkus.io/install.sh | bash
                    ```
                    
                    ### Step 2: Create a New Project
                    
                    Use the Quarkus CLI to create a new project:
                    
                    ```bash
                    quarkus create app org.acme:my-first-quarkus-app
                    ```
                    
                    ### Step 3: Run the Application
                    
                    Navigate to the project directory and run the application in development mode:
                    
                    ```bash
                    cd my-first-quarkus-app
                    ./mvnw compile quarkus:dev
                    ```
                    
                    Your Quarkus application is now running, and you can start coding!
                    
                    
                    ## Conclusion
                    
                    Quarkus is an exciting framework that brings Java into the cloud-native era with impressive performance and developer-friendly features. Whether you are building microservices or serverless applications, Quarkus offers a robust and efficient platform to help you achieve your goals. Give Quarkus a try and experience the future of Java development!
                    
                    By exploring Quarkus, you will join a growing community of developers who are redefining what Java can do in the cloud. Happy coding!""");
            blogEntry.persist();

            blogEntry = new BlogEntry("Web Components with Lit", """
                    ![Lit Logo](https://logowik.com/content/uploads/images/lit1871.logowik.com.webp#google_vignette)
                    
                    ## Introduction
                    
                    In the ever-evolving world of web development, staying ahead of the curve is essential. One library that has been making waves in the realm of Web Components is **Lit**. Designed to simplify the creation of fast, lightweight, and reusable web components, Lit has quickly become a favorite among developers. In this blog post, we will explore what makes Lit stand out, how it works, and why you should consider it for your next project.
                    
                    ## What is Lit?
                    
                    Lit is a modern, efficient, and highly flexible library for building Web Components. It builds on the standard Web Components APIs, making it easier to create and manage custom elements. Lit`s core philosophy revolves around leveraging the web platform`s native capabilities, providing just enough abstraction to make development smoother and more intuitive.
                    
                    ## Key Features
                    
                    ### Lightweight and Fast
                    
                    Lit is designed to be lightweight, ensuring that your components are performant and quick to load. The library itself has a small footprint, which means it will not bloat your application or slow down your website.
                    
                    ### Declarative Templates
                    
                    Lit uses a declarative templating system that allows you to write HTML directly within your JavaScript or TypeScript files. This makes it easy to visualize the structure of your components and manage dynamic content.
                    
                    ```javascript
                    import { html, css, LitElement } from \"lit\";
                    
                    class MyElement extends LitElement {
                      static styles = css`
                        :host {
                          display: block;
                          padding: 16px;
                          color: var(--my-element-text-color, black);
                        }
                      `;
                    
                      static properties = {
                        name: { type: String }
                      };
                    
                      constructor() {
                        super();
                        this.name = \"World\";
                      }
                    
                      render() {
                        return html`<p>Hello, ${this.name}!</p>`;
                      }
                    }
                    
                    customElements.define(\"my-element\", MyElement);
                    ```
                    
                    ### Reactive Properties
                    
                    Lit introduces a reactive properties system that simplifies state management within components. By defining properties and their corresponding types, Lit ensures that your components update efficiently when data changes.
                    
                    ### Extensible and Composable
                    
                    Lit is designed to be highly extensible. You can easily create base classes and mixins to share functionality across multiple components. This composability allows for greater code reuse and cleaner architecture.
                    
                    ### Community and Ecosystem
                    
                    The Lit community is vibrant and growing, with a wealth of resources, tutorials, and plugins available. Whether you are just getting started or looking to dive deep into advanced features, there is plenty of support to help you along the way.
                    
                    ## Getting Started with Lit
                    
                    To get started with Lit, you will need to install the library via npm:
                    
                    ```bash
                    npm install lit
                    ```
                    
                    Once installed, you can start creating your first Lit component by extending the `LitElement` class and defining your templates and styles.
                    
                    ### Example: Creating a Simple Counter
                    
                    Here is a simple example of a counter component built with Lit:
                    
                    ```javascript
                    import { html, css, LitElement } from \"lit\";
                    
                    class CounterElement extends LitElement {
                      static styles = css`
                        :host {
                          display: block;
                          padding: 16px;
                        }
                        button {
                          font-size: 1.2em;
                        }
                      `;
                    
                      static properties = {
                        count: { type: Number }
                      };
                    
                      constructor() {
                        super();
                        this.count = 0;
                      }
                    
                      increment() {
                        this.count += 1;
                      }
                    
                      render() {
                        return html`
                          <div>Count: ${this.count}</div>
                          <button @click=\"${this.increment}\">Increment</button>
                        `;
                      }
                    }
                    
                    customElements.define(\"counter-element\", CounterElement);
                    ```
                    
                    ## Why Choose Lit?
                    
                    ### Simplicity and Elegance
                    
                    Lit provides a clean and simple API that leverages modern JavaScript features, making it easy to learn and use. Its declarative approach to templates and reactive properties simplifies component development.
                    
                    ### Performance
                    
                    With its minimal overhead and efficient rendering system, Lit ensures that your components are fast and responsive, providing a smooth user experience.
                    
                    ### Compatibility
                    
                    Lit builds on the standard Web Components APIs, ensuring compatibility with other frameworks and libraries. This makes it easy to integrate Lit components into existing projects.
                    
                    ## Conclusion
                    
                    Lit is a powerful tool for modern web development, offering a perfect blend of simplicity, performance, and flexibility. 
                    Whether you are building a small widget or a complex application, Lit provides the tools you need to create high-quality, reusable web components. 
                    Give Lit a try and see how it can revolutionize your development workflow.""");
            blogEntry.persist();

            blogEntry = new BlogEntry("Exploring HTMX", """
                    ### Introduction
                    In the ever-evolving landscape of web development, HTMX has emerged as a powerful tool for creating dynamic and interactive web applications. By extending HTML with modern AJAX capabilities, HTMX simplifies the process of building sophisticated web interfaces without the need for extensive JavaScript. In this blog post, we will explore what HTMX is, its core features, and how it can revolutionize your web development workflow.
                    
                    ### What is HTMX?
                    HTMX is a JavaScript library that allows you to use extended HTML attributes to perform AJAX requests, WebSockets, and Server-Sent Events (SSE) directly from HTML. It was created to enhance the capabilities of HTML and enable developers to build modern web applications with less JavaScript code.
                    
                    ### Core Features of HTMX
                    1. **Seamless AJAX Integration**: With HTMX, you can perform AJAX requests using HTML attributes such as `hx-get`, `hx-post`, and `hx-swap`. This allows you to update parts of your web page dynamically without a full page reload.
                    2. **WebSocket Support**: HTMX supports WebSockets, enabling real-time communication between the client and server. This is particularly useful for applications that require live updates, such as chat applications or real-time dashboards.
                    3. **Server-Sent Events (SSE)**: HTMX also supports SSE, allowing the server to push updates to the client. This is ideal for applications that need to provide live data feeds.
                    4. **Declarative Approach**: HTMX promotes a declarative approach to web development. By using HTML attributes, you can describe the desired behavior directly in your HTML, making your code more readable and maintainable.
                    
                    ### Getting Started with HTMX
                    To get started with HTMX, you need to include the HTMX library in your project. You can do this by adding the following script tag to your HTML file:
                    
                    ```html
                    <script src=\"https://unpkg.com/htmx.org\"></script>
                    ```
                    
                    Once you have included the library, you can start using HTMX attributes in your HTML. Here’s a simple example:
                    
                    ```html
                    <!DOCTYPE html>
                    <html lang=\"en\">
                    <head>
                        <meta charset=\"UTF-8\">
                        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
                        <title>HTMX Example</title>
                    </head>
                    <body>
                        <button hx-get=\"/hello\" hx-target=\"#response\">Say Hello</button>
                        <div id=\"response\"></div>
                    
                        <!-- HTMX Script -->
                        <script src=\"https://unpkg.com/htmx.org\"></script>
                    </body>
                    </html>
                    ```
                    
                    In this example, clicking the \"Say Hello\" button will send a GET request to the `/hello` endpoint and update the content of the `#response` div with the response from the server.
                    
                    ### Benefits of Using HTMX
                    1. **Reduced JavaScript Code**: By leveraging HTML attributes, you can reduce the amount of JavaScript code needed to achieve dynamic functionality.
                    2. **Enhanced Readability**: HTMX promotes a clean and declarative approach, making your code easier to read and understand.
                    3. **Improved Performance**: HTMX allows you to update specific parts of your page without a full page reload, leading to faster and more efficient web applications.
                    
                    ### Conclusion
                    HTMX is a powerful tool that extends the capabilities of HTML and simplifies the process of building dynamic web applications. By using HTMX, you can create interactive and real-time web experiences with minimal JavaScript code. Whether you are building a simple website or a complex web application, HTMX is a valuable addition to your web development toolkit.""");
            blogEntry.persist();
        }
    }
}
```

## Now display the blogs

In Qute, all views live in `templates/<Controller>/<method>.html`, so for `Cms.index` we need to
create a `templates/Cms/index.html`. In order to make sure all your web pages have the same style and
structure, we recommend using template composition, so every endpoint template extends a main template called
`main.html` by convention:

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

Now let's make our `Cms.index` template so that we can display our list of blog entries:

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
                {blogEntry.published}: {blogEntry.title}
            </li>
            {/for}
        </ul>
    </div>
</div>
```

### Give it a spin!

Now start Quarkus in DEV mode, run this in your terminal in your application's folder:

```shell
$ ./mvnw quarkus:dev
```

Press `w` and observe your web page.

### Style it up

// At 5 minutes

Let's add a logo for our blog.

Download https://quarkus.io/assets/images/brand/quarkus_icon_reverse.svg and save it at `src/main/resources/web/static/assets/images/logo.svg`.

Now let's use mvnpm to download NPM modules packaged as Maven modules, so that we have Stimulus and Bootstrap in our application.

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

Thanks to Web Bundler, we can write our style in SCSS, and it will be compiled on-the-fly to CSS.

In our case, we will create a `cms` bundle, so our styles and javascript must go under `web/cms` resources.

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

In order for the Web Bundler to know what JavaScript and CSS dependencies must be packaged,
you must import them from your bundle's JavaScript.

Edit the `app.js`:

```javascript
import "bootstrap/scss/bootstrap.scss";
import "bootstrap-icons/font/bootstrap-icons.scss";
import { Application } from "@hotwired/stimulus";

const StimulusApp = Application.start();
export default StimulusApp;
```

Now we must configure the Web Bundler to know about our `cms` bundle.

Add this to `src/main/resources/application.properties`:

```properties
# Bundle the CMS things
quarkus.web-bundler.bundle.cms=true
quarkus.web-bundler.bundle.cms.qute-tags=true
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

Now reload your main page and observe!

### Displaying blog entries

// At 8:30

Now, we want to click on a blog entry to show its contents,
so let's add the ability to show a blog content per id on the index page.
We do this by adding a `Cms.editBlogEntry` method which has a `id` path parameter,
representing the blog entry we want to show. Notice that it can use the same template
as the `index` method, so they both share the same view. The main difference is that
one will have a `currentBlogEntry` set to `null` and the other to the blog entry
we want to display.

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
        return Templates.index(BlogEntry.listAllSortedByPublished(), null);
    }

    public TemplateInstance editBlogEntry(@RestPath Long id) {
        BlogEntry blogEntry = BlogEntry.findById(id);
        notFoundIfNull(blogEntry);
        return Templates.index(BlogEntry.listAllSortedByPublished(), blogEntry);
    }
}
```

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
