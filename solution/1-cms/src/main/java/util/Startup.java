package util;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import model.BlogEntry;

import java.time.LocalDate;

@ApplicationScoped
public class Startup {
    /**
     * This method is executed at the start of your application
     */
    @Transactional
    public void start(@Observes StartupEvent evt) {
        // in DEV mode we seed some data
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT && BlogEntry.count() == 0) {
            BlogEntry blogEntry = new BlogEntry("How to make cheese?", "make-cheese.jpg", """
                    ## The Process is Easy, Here's How to Set it Up in 12 Steps. The Last One Will Surprise You.
                                        
                    1. **Gather Your Ingredients and Tools:** \s
                       To start, you'll need fresh milk, rennet, and a starter culture. Make sure you have a cheesecloth, a large pot, a thermometer, and a colander ready as well.
                                        
                    2. **Heat the Milk:** \s
                       Pour the milk into the large pot and slowly heat it to around 85°F (29°C). Stir occasionally to ensure even heating.
                                        
                    3. **Add the Starter Culture:** \s
                       Once the milk reaches the desired temperature, add the starter culture. This will help acidify the milk and start the cheese-making process. Stir well to distribute the culture evenly.
                                        
                    4. **Let the Milk Ripen:** \s
                       Cover the pot and let the milk sit undisturbed for about an hour. This allows the starter culture to ripen the milk, creating the perfect environment for the rennet to work.
                                        
                    5. **Add the Rennet:** \s
                       Dilute the rennet in a small amount of cool, non-chlorinated water. Add this mixture to the milk and stir gently for a few minutes. This will start the coagulation process.
                                        
                    6. **Wait for the Curds to Form:** \s
                       Cover the pot again and let it sit for about 30-60 minutes. You'll know it's ready when you can insert a knife and see a clean break in the curd.
                                        
                    7. **Cut the Curds:** \s
                       Using a long knife, cut the curds into small cubes. This helps release the whey and start the process of firming up the curds.
                                        
                    8. **Cook the Curds:** \s
                       Slowly heat the curds to around 105°F (40°C), stirring gently to prevent them from clumping together. This helps the curds firm up and expel more whey.
                                        
                    9. **Drain the Whey:** \s
                       Once the curds are firm enough, pour them into a colander lined with cheesecloth. Let the whey drain off, leaving just the curds behind.
                                        
                    10. **Press the Cheese:** \s
                       Transfer the curds into a cheese mold and press them with a weight. This helps to expel any remaining whey and shape the cheese. Press for several hours or overnight, depending on the type of cheese you're making.
                                        
                    11. **Salt and Age the Cheese:** \s
                       After pressing, remove the cheese from the mold and salt it on all sides. Salt acts as a preservative and enhances the flavor. Place the cheese in a cool, humid place to age. The aging time can vary from a few days to several months, depending on the desired flavor and texture.
                                        
                    12. **Enjoy Your Homemade Cheese:** \s
                       Here's the surprising part - cheese making is a form of art. The flavor and texture can vary each time you make it, influenced by factors like the type of milk, the ripening time, and even the environment. Each batch is unique, and the joy of tasting your own homemade cheese is incomparable.
                                        
                    So there you have it! Making cheese at home is a fun and rewarding process. With these 12 steps, you can enjoy fresh, homemade cheese tailored to your taste. Happy cheese making!
                                        
                    """, LocalDate.of(2024, 5, 22));
            blogEntry.persist();

            blogEntry = new BlogEntry("How to eat cheese? The Ultimate Guide to Enjoying Cheese", "eat-cheese.jpg", """
                    Cheese is a versatile and delicious food that can be enjoyed in many different ways. Whether you’re a cheese connoisseur or just starting to explore the world of cheese, here are some tips on how to eat and enjoy cheese to the fullest.
                                        
                    ### 1. Choose the Right Cheese
                                        
                    Start by selecting a cheese that appeals to you. There are many varieties to choose from, including:
                                        
                    - **Soft Cheeses:** Brie, Camembert, Goat Cheese
                    - **Semi-Soft Cheeses:** Havarti, Fontina, Munster
                    - **Hard Cheeses:** Cheddar, Gouda, Parmesan
                    - **Blue Cheeses:** Roquefort, Gorgonzola, Stilton
                                        
                    ### 2. Pair Cheese with the Right Accompaniments
                                        
                    Cheese can be enjoyed on its own, but pairing it with the right foods can enhance its flavors. Consider these pairings:
                                        
                    - **Fruits:** Apples, pears, grapes, figs
                    - **Nuts:** Walnuts, almonds, hazelnuts
                    - **Bread and Crackers:** Baguette slices, whole grain crackers, breadsticks
                    - **Spreads:** Honey, fig jam, quince paste
                                        
                    ### 3. Serve at the Right Temperature
                                        
                    Cheese tastes best at room temperature. Remove the cheese from the refrigerator about 30 minutes to an hour before serving to allow it to warm up and develop its full flavor and texture.
                                        
                    ### 4. Use the Right Tools
                                        
                    Having the right tools can make a big difference in your cheese-eating experience:
                                        
                    - **Cheese Knife:** For cutting and serving
                    - **Cheese Board:** For presentation
                    - **Cheese Grater:** For hard cheeses like Parmesan
                    - **Cheese Slicer:** For semi-soft and hard cheeses
                                        
                    ### 5. Taste Cheese Properly
                                        
                    When tasting cheese, follow these steps to fully appreciate its flavors:
                                        
                    1. **Look:** Observe the cheese’s appearance. Note its color, texture, and any mold or rind.
                    2. **Smell:** Take a moment to smell the cheese. This can give you clues about its flavor.
                    3. **Taste:** Take a small piece and let it sit on your tongue. Notice the texture and how the flavor develops.
                                        
                    ### 6. Experiment with Cooking
                                        
                    Cheese is not just for snacking—it can be a key ingredient in many dishes. Try these ideas:
                                        
                    - **Melted Cheese:** Grilled cheese sandwiches, cheese fondue, quesadillas
                    - **Baked Cheese:** Macaroni and cheese, lasagna, baked brie
                    - **Cheese Toppings:** Pizza, salads, soups
                                        
                    ### 7. Enjoy Cheese with Beverages
                                        
                    Pairing cheese with the right beverage can elevate your tasting experience. Consider these combinations:
                                        
                    - **Wine:** Red wines like Merlot or Cabernet Sauvignon for hard cheeses, white wines like Sauvignon Blanc for soft cheeses
                    - **Beer:** Ales, stouts, or lagers depending on the cheese type
                    - **Non-Alcoholic Options:** Sparkling water, apple cider, or grape juice
                                        
                    ### 8. Store Cheese Properly
                                        
                    To keep cheese fresh, wrap it in wax paper or parchment paper, then place it in a loose plastic bag or an airtight container. Store it in the refrigerator’s vegetable drawer or a cheese drawer if available.
                                        
                    ### 9. Explore Cheese Varieties
                                        
                    Don’t be afraid to try new and different cheeses. Visit a local cheese shop or a farmer’s market, and ask for recommendations. Exploring new varieties can be a fun and delicious adventure.
                                        
                    ### 10. Share with Friends
                                        
                    Cheese is best enjoyed with others. Host a cheese tasting party, or simply share your favorite cheese with family and friends.
                                        
                    By following these tips, you can enjoy cheese in all its glory. Whether you’re savoring a simple slice or indulging in a gourmet cheese board, cheese is a delightful treat that can be enjoyed in countless ways. Happy cheese eating!
                                        
                    """, LocalDate.of(2024, 2, 12));
            blogEntry.persist();

            blogEntry = new BlogEntry("Getting Started with Quarkus", "start-quarkus.jpg", """

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

                    By exploring Quarkus, you will join a growing community of developers who are redefining what Java can do in the cloud. Happy coding!""", LocalDate.of(2024, 4, 8));
            blogEntry.persist();

            blogEntry = new BlogEntry("Web Components with Lit", "lit.jpg", """
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
                    import { html, css, LitElement } from "lit";

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
                        this.name = "World";
                      }

                      render() {
                        return html`<p>Hello, ${this.name}!</p>`;
                      }
                    }

                    customElements.define("my-element", MyElement);
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
                    import { html, css, LitElement } from "lit";

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
                          <button @click="${this.increment}">Increment</button>
                        `;
                      }
                    }

                    customElements.define("counter-element", CounterElement);
                    ```

                    ## Why Choose Lit?

                    ### Simplicity and Elegance

                    Lit provides a clean and simple API that leverages modern JavaScript features, making it easy to learn and use. Its declarative approach to templates and reactive properties simplifies component development.

                    ### Performance

                    With its minimal overhead and efficient rendering system, Lit ensures that your components are fast and responsive, providing a smooth user experience.

                    ### Compatibility

                    Lit builds on the standard Web Components APIs, ensuring compatibility with other frameworks and libraries. This makes it easy to integrate Lit components into existing projects.

                    ## Conclusion

                    Lit is a powerful tool for modern web development, offering a perfect blend of simplicity, performance, and flexibility.\s
                    Whether you are building a small widget or a complex application, Lit provides the tools you need to create high-quality, reusable web components.\s
                    Give Lit a try and see how it can revolutionize your development workflow.""", LocalDate.of(2024, 3, 14));
            blogEntry.persist();

            blogEntry = new BlogEntry("Exploring HTMX", "htmx.jpg", """

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
                    <script src="https://unpkg.com/htmx.org"></script>
                    ```

                    Once you have included the library, you can start using HTMX attributes in your HTML. Here’s a simple example:

                    ```html
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>HTMX Example</title>
                    </head>
                    <body>
                        <button hx-get="/hello" hx-target="#response">Say Hello</button>
                        <div id="response"></div>g

                        <!-- HTMX Script -->
                        <script src="https://unpkg.com/htmx.org"></script>
                    </body>
                    </html>
                    ```

                    In this example, clicking the "Say Hello" button will send a GET request to the `/hello` endpoint and update the content of the `#response` div with the response from the server.

                    ### Benefits of Using HTMX
                    1. **Reduced JavaScript Code**: By leveraging HTML attributes, you can reduce the amount of JavaScript code needed to achieve dynamic functionality.
                    2. **Enhanced Readability**: HTMX promotes a clean and declarative approach, making your code easier to read and understand.
                    3. **Improved Performance**: HTMX allows you to update specific parts of your page without a full page reload, leading to faster and more efficient web applications.

                    ### Conclusion
                    HTMX is a powerful tool that extends the capabilities of HTML and simplifies the process of building dynamic web applications. By using HTMX, you can create interactive and real-time web experiences with minimal JavaScript code. Whether you are building a simple website or a complex web application, HTMX is a valuable addition to your web development toolkit.
                    """, LocalDate.of(2024, 1, 2));
            blogEntry.persist();
        }
    }
}