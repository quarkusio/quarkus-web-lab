import {LitElement, html, css} from 'lit';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import MarkdownIt from 'markdown-it';
import '@github/markdown-toolbar-element';
import '@qomponent/qui-icons';
import '@github/relative-time-element';

class CommentBox extends LitElement {

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
    
    static properties = {
        serverUrl: { type: String },
        ref: { type: String },
        _comments: {state: true}
    };

    constructor() {
        super();
        this.serverUrl = ''; // Default server URL
        this.ref = null;
        this._comments = [];
        this.md = new MarkdownIt();
    }

    connectedCallback() {
        super.connectedCallback();
        this._fetchAllComments();
    }

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
            <button class="button" @click="${this._postComment}"><fas-icon icon="comment-dots" color="white"></fas-icon> Post Comment</button>
          </div>
        `;
    }

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

    _renderMarkdownComment(comment){
        const htmlContent = this.md.render(comment.comment);
        return html`${unsafeHTML(htmlContent)}`;
    }

    _fetchAllComments(){
        fetch(`${this.serverUrl}/comment/${this.ref}`)
            .then(response => response.json())
            .then(response => this._comments = response);
    }

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
        this.shadowRoot.getElementById('name').value = "";
        this.shadowRoot.getElementById('comment').value = "";
    }

}
customElements.define('comment-box', CommentBox);