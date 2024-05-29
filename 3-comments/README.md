## 3- Comments (~30m)

If you haven't, complete the [CMS part](../1-cms) and [Blog part](../2-blog) before this.

The comments microservice will allow users to comment on blog entries. We will build a Microfrontend component to be added to the 
bottom of a blog entry. This component will allow users to comment and also display all existing comments. We will allow writing a comment
using Markdown. 

![screenshot](screenshot.png)

### Get the initial app

We don't start from scratch.
The directory which contains this README also contains the _initial version_ of the app.

Open a new tab in your terminal in the project root (and keep the CMS and Blog running):

```shell
cd 3-comments
```

### Start the app in the [dev mode](https://quarkus.io/guides/dev-mode-differences)

```
./mvnw quarkus:dev
```

🚀 Press `w` and observe your web page you should see `TODO`.

The app runs on port 7070 so that it does not conflict with other parts of the Lab.

### Backend

The backend is an existing Quarkus REST App that store and retrieve comments from a database. Now that you have coded the CMS and the blog, we provide it in the initial app to let you focus on the web-component part.

👀 See the REST resource: `src/main/java/workshop/comments/CommentResource.java`

and

👀 The database entity: `src/main/java/workshop/comments/Comment.java`

#### Extensions used for the backend

- [REST and Jackson](https://quarkus.io/guides/rest#json-serialisation)
- [Bean validation](https://quarkus.io/guides/validation)
- [Hibernate and Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [H2](https://quarkus.io/guides/datasource)


This gives us two REST Api's to use:

**››› COMMAND TIME**



1) Adding a comment to a blog entry, for example blog entry with reference 123:

```
curl -X 'POST' \
  'http://localhost:7070/comment' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "ref": "123",
  "name": "Foo Bar",
  "comment": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
}'
```

2) Getting all the comments for a certain blog entry, for example blog entry with reference 123:

```
curl -X 'GET' \
  'http://localhost:7070/comment/123' \
  -H 'accept: application/json'
```

### The UI (Workshop really starts here)

#### Lit 

We are going to use Lit[https://lit.dev/] to build a web component. You can navigate to mvnpm[https://mvnpm.org] to find any UI library:

👀 Have a look to the `pom.xml`, we declared Lit and some ready-to-use web-components to help us.

> **_NOTE:_**
> The scope can be provided, as the bundler will bundle the needed js into your bundle, and there is not need to have the whole lib during runtime.

👀 Now we can start with a basic component. In `src/main/resources/web/app`, see a file called `comment-box.js`;

This is the basic structure of a Lit component:

 - extends LitElement (that extends HTMLElement)
 - styles css: here we can add CSS
 - properties: here we can add element attributes and state properties
 - constructor: here we set the initial values for properties
 - connectedCallback: this gets called when the element is added to the DOM. Here we can make calls to the server to fetch initial values for state properties
 - render: this gets called to render the element (or re-render in case a state property change)

#### Test

👀 This component will be used on the static site, but to do some manual testing, we created a page that use this in `src/main/resources/web/index.html`.

🚀 Throughout the workshop, you can check the http://localhost:7070/ page to see the progress

#### Allow the component to take a ref param

**››› CODING TIME**

We need to link each comment section to a blog entry. Add an element attribute that the user of the component can set
to indicate the reference to the blog entry

<details>
<summary>See solution</summary>

```js
static properties = {
    ref: { type: String }
};

constructor() {
    super();
    this.serverUrl = SERVER_URL;  // This is defined through web-bundler envs in application.properties
    this.ref = null;
    
}

```
</details>

This will allow users of this component to use it in the `index.html` page :

<details>
<summary>See hint</summary>

You may now use your component like a normal html tag `<comment-box .../>`, use `test` as example ref.
</details>

<details>
<summary>See solution</summary>

```html
<comment-box ref="test"></comment-box>
```
</details>

🚀 Go observe the test page, you should see the "Hello World"

#### Fetch the initial list of comments

**››› CODING TIME**

Add a new string state property called `_comments` that will contain the comments.

<details>
<summary>See solution</summary>

```js
static properties = {
    ref: { type: String },
    _comments: {state: true}
};

constructor() {
    super();
    this.serverUrl = SERVER_URL;  // This is defined through web-bundler envs in application.properties
    this.ref = null;
    this._comments = [];
}
```
</details>



Add a method that call the REST endpoint to fetch all comments, the set the `_comments` state property to the response. Changing the state will automatically trigger a new `render()`.

<details>
<summary>See solution</summary>

```js
_fetchAllComments(){
    fetch(`${this.serverUrl}/comment/${this.ref}`)
        .then(response => response.json())
        .then(response => this._comments = response);
}
```
</details>


Now call that method in the `connectedCallback`:

<details>
<summary>See solution</summary>

```js
connectedCallback() {
    super.connectedCallback();
    this._fetchAllComments();
}
```
</details>

Now we have the comments for a certain blog reference when the component is added to the DOM. Let's render it. We will loop through all comments and render them. Each time the `comments` state property change, this will re-render.


<details>
<summary>See solution</summary>

```js
render(){
    if(this._comments){
        return html`${this._comments.map((comment) =>
            html`<div class="existingcomment">
                    <div class="comment">${comment.comment}</div>
                    <div class="right">
                        <div class="commentByAndTime">
                            by ${comment.name} on ${comment.time}
                        </div>
                    </div>
                </div>`
        )}
        `;
    }
}
```
</details>


🚀 Go observe the test page, you should see the list of comments.

#### Allow adding a comment

Change the current `render` method to rather call 2 methods. One to render the "add comment", and another with the "existing comments".



<details>
<summary>See hint</summary>

Move the current `render` to `_renderExistingComments` and add a new `_renderNewComment` with this html content:
```html
<div class="comment-box">
    <input id="name" class="input" type="text" placeholder="Your name">
    <textarea id="comment" class="input" placeholder="Your comment. You can use Markdown for formatting" rows="5"></textarea>
    <button class="button" @click="${this._postComment}"><fas-icon icon="comment-dots" color="white"></fas-icon> Post Comment</button>
</div>
```
</details>

<details>
<summary>See solution</summary>

```js
render() {
    return html`${this._renderNewComment()}
                ${this._renderExistingComments()}
      `;
}


_renderNewComment() {
    return html`
      <div class="comment-box">
        <input id="name" class="input" type="text" placeholder="Your name">
        <textarea id="comment" class="input" placeholder="Your comment. You can use Markdown for formatting" rows="5"></textarea>
        <button class="button" @click="${this._postComment}"><fas-icon icon="comment-dots" color="white"></fas-icon> Post Comment</button>
      </div>
    `;
}

_renderExistingComments(){
    if(this._comments){
        return html`${this._comments.map((comment) =>
            html`<div class="existingcomment">
                    <div class="comment">${comment.comment}</div>
                    <div class="right">
                        <div class="commentByAndTime">
                            by ${comment.name} on ${comment.time}
                        </div>
                    </div>
                </div>`
        )}
        `;
    }
}
```
</details>

Add a method to do a POST when the button is clicked (we already added a `@click` handler to the button)

```js
_postComment() {
    let comment = new Object();
    comment.ref = this.ref;
    comment.name = this.shadowRoot.getElementById('name').value;
    comment.comment = this.shadowRoot.getElementById('comment').value;

    fetch(`${this.serverUrl}/comment`, {
        method: 'POST',
        headers: {
        'Content-Type': 'application/json',
        },
        body: JSON.stringify(comment),
    })
    .then(response => response.json())
    .then(response => {
            this._comments = response;
            this._clear();
        }
    );
}

_clear(){
    // Let's make sure to reset the fields after adding a comment
    this.shadowRoot.getElementById('name').value = "";
    this.shadowRoot.getElementById('comment').value = "";
}
```

Here we post a new Comment Object to the `/comment` endpoint in Json format.

The POST method return the list of (new) responses, so we can set the `_comments` state property that will result in a re-render with the updated info.

> **_NOTE:_** 
> Technically, the more correct REST way to do this is to not return data on a POST, but add a header that contains where the newly added comment can be found. You can then make another call to fetch the latest comment and update the `_comments` array.

🚀 Go observe the test page, you should be able to post comments.

#### CSS

Add some CSS to make the layout better:

```css
static styles = css`
    :host {
        display: flex;
        flex-direction: column;
        overflow: hidden;
        padding: 5px;
        gap: 10px;
        font-family: -apple-system, BlinkMacSystemFont, 'Roboto', 'Segoe UI', Helvetica, Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol';
    }
    .comment-box {
        display: flex;
        flex-direction: column;
        gap:2px;
    }
    .existingcomment {
        border: 1px solid #C0C2CD;
        border-radius: 12px;
        padding: 10px;
    }

    .right {
        display: flex;
        flex-direction: row-reverse;
    }

    .commentByAndTime {
        display: flex;
        flex-direction: column;
    }

    .time {
        color: gray;
        font-size: small;
    }

    .input {
        width: 100%;
        padding: 12px 20px;
        margin: 8px 0;
        box-sizing: border-box;
        resize: none;
        border: 2px solid #ccc;
    }

    .button {
        background-color: #04AA6D; /* Green */
        border: none;
        color: white;
        padding: 15px 32px;
        text-align: center;
        text-decoration: none;
        display: inline-block;
        font-size: 16px;
        cursor: pointer;
    }

    markdown-toolbar {
        display: flex;
        gap: 10px;
    }

    fas-icon {
        color: gray;
        cursor: pointer;
    }
    fas-icon:hover {
        color: #212f80;
    }
`;    
```

#### CORS



👀 check the `application.properties` provided that static server runs on 8080, it makes sure this can be used from the static server application.

Now when add this ⬇️ to the blog page 'src/main/resources/templates/Blog/blogPost.html' (just after the `</main>).

```html
<script crossorigin src="http://localhost:7070/static/bundle/main.js" type="module"></script>

<comment-box ref="{entry.slug}"></comment-box>
```

🚀 Open the blog in dev or static mode, you now have comments for the blog posts !!!

🥳🎉 You just finished the lab!

---

#### (Optional) Add markdown support

We want users to be able to comment using markdown and then render the markup.

To render the markup we will use [Markdown-it](https://github.com/markdown-it/markdown-it), it is already in the pom.xml

We can import it:

```js
import MarkdownIt from 'markdown-it';
```

and create an instance in the constructor:

```js
 constructor() {
    [...]
    this.md = new MarkdownIt({breaks: true});
 }
```

> **_NOTE:_**
> It doesn't have to be specified as a state because it is not altering the view depending on the state of its data.

Create a new method that will render an existing comment using Markup:

```js
_renderMarkdownComment(comment){
    const htmlContent = this.md.render(comment.comment);
    return html`${unsafeHTML(htmlContent)}`;
}
```

Change the `_renderExistingComments` method to use above:

```js
render(){
    if(this._comments){
        return html`${this._comments.map((comment) =>
            html`<div class="existingcomment">
                    <div class="comment">${this._renderMarkdownComment(comment)}</div>
                    <div class="right">
                        <div class="commentByAndTime">
                            by ${comment.name} on ${comment.time}
                        </div>
                    </div>
                </div>`
          )}
        `;
    }
}
```

Also add a toolbar to make it easier for users to create Markdown. We will use [markdown-toolbar element](https://github.com/github/markdown-toolbar-element):

```xml
<dependency>
    <groupId>org.mvnpm.at.github</groupId>
    <artifactId>markdown-toolbar-element</artifactId>
    <version>2.2.3</version>
    <scope>provided</scope>
</dependency>
```

Import the new element to use:

```js
import '@github/markdown-toolbar-element';
```

And add it under the comment textarea:

```js
_renderNewComment() {
    return html`
      <div class="comment-box">
        <input id="name" class="input" type="text" placeholder="Your name">
        <textarea id="comment" class="input" placeholder="Your comment. You can use Markdown for formatting" rows="5"></textarea>
        <markdown-toolbar for="comment">
            <md-bold>B</md-bold>
            <md-header>H</md-header>
            <md-italic>I</md-italic>
            <md-quote>quote</md-quote>
            <md-code>code</md-code>
            <md-link>link</md-link>
            <md-image>img</md-image>
            <md-unordered-list>ul</md-unordered-list>
            <md-ordered-list>ol</md-ordered-list>
            <md-task-list>tasks</md-task-list>
        </markdown-toolbar>
        <button class="button" @click="${this._postComment}"><fas-icon icon="comment-dots" color="white"></fas-icon> Post Comment</button>
      </div>
    `;
}
```

Make sure the `for` attribute is set to the id of the comment textarea.

We can replace the buttons with nice Fontawesome icons:

```xml
<dependency>
    <groupId>org.mvnpm.at.qomponent</groupId>
    <artifactId>qui-icons</artifactId>
    <version>1.0.1</version>
    <scope>provided</scope>
</dependency>
```

Import the icon library:

```js
import '@qomponent/qui-icons';
```

Add them to the markdown toolbar:

```js
<markdown-toolbar for="comment">
    <md-bold><fas-icon icon="bold" size="small" title="bold"></fas-icon></md-bold>
    <md-header><fas-icon icon="heading" size="small" title="heading"></fas-icon></md-header>
    <md-italic><fas-icon icon="italic" size="small" title="italic"></fas-icon></md-italic>
    <md-quote><fas-icon icon="quote-right" size="small" title="quote"></fas-icon></md-quote>
    <md-code><fas-icon icon="code" size="small" title="code"></fas-icon></md-code>
    <md-link><fas-icon icon="link" size="small" title="link"></fas-icon></md-link>
    <md-image><fas-icon icon="image" size="small" title="image"></fas-icon></md-image>
    <md-unordered-list><fas-icon icon="list-ul" size="small" title="unordered list"></fas-icon></md-unordered-list>
    <md-ordered-list><fas-icon icon="list-ol" size="small" title="ordered list"></fas-icon></md-ordered-list>
    <md-task-list><fas-icon icon="list-check" size="small" title="task list"></fas-icon></md-task-list>
</markdown-toolbar>
```

#### Render the comment time in a `X mins ago` format

To render the timestamp in a nicer format, that will show `now` when a comment is made, and then update itself to show time since the comment
we can use [relative-time element](https://github.com/github/relative-time-element):

```xml
<dependency>
    <groupId>org.mvnpm.at.github</groupId>
    <artifactId>relative-time-element</artifactId>
    <version>4.4.0</version>
    <scope>provided</scope>
</dependency>
```

Import:

```js
import '@github/relative-time-element';
```

Update the `_renderExistingComments()` method:

```js
_renderExistingComments(){
    if(this._comments){
        return html`${this._comments.map((comment) =>
            html`<div class="existingcomment">
                    <div class="comment">${this._renderMarkdownComment(comment)}</div>
                    <div class="right">
                        <div class="commentByAndTime">
                            by ${comment.name}
                            <relative-time datetime="${comment.time}" class="time">
                                ${comment.time}
                            </relative-time>
                        </div>
                    </div>
                </div>`
            )}
        `;
    }
}
```
