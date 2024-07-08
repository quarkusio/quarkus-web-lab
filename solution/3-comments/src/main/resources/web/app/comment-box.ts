import { LitElement, html, css} from 'lit';
import { customElement, state, property } from 'lit/decorators.js';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import MarkdownIt from 'markdown-it';
import '@github/markdown-toolbar-element';
import '@qomponent/qui-icons';
import '@github/relative-time-element';

declare const SERVER_URL: string; // This is defined through web-bundler envs in application.properties

interface Comment {
    ref: string;
    name: string;
    comment: string;
    time?: string;
}

@customElement('comment-box')
class CommentBox extends LitElement {

    @property()
    ref: string;

    @property()
    serverUrl: string = SERVER_URL;

    @state()
    private comments: Comment[] = [];

    @state()
    private name: string = "";

    @state()
    private comment: string = "";

    private md = new MarkdownIt({breaks: true});

    connectedCallback() {
        super.connectedCallback();
        this.fetchAllComments();
    }

    render() {
        return html`${this.renderNewComment()}
                    ${this.renderExistingComments()}`;
    }

    private renderNewComment() {
        return html`
          <div class="comment-box">
            <input id="name" class="input" type="text" placeholder="Your name" .value=${this.name} @input="${this.nameChanged}" >
            <textarea id="comment" class="input" placeholder="Your comment. You can use Markdown for formatting" rows="5" .value=${this.comment} @input="${this.commentChanged}" ></textarea>
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
            <button class="button" @click="${this.postComment}"><fas-icon icon="comment-dots" color="white"></fas-icon> Post Comment</button>
          </div>
        `;
    }

    private nameChanged(e: any) {
        this.name = e.target.value;
    }

    private commentChanged(e: any) {
        this.comment = e.target.value;
    }

    private renderExistingComments(){
        if(this.comments){
            return html`${this.comments.map((comment) =>
                html`<div class="existingcomment">
                        <div class="comment">${this.renderMarkdownComment(comment)}</div>
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

    private renderMarkdownComment(comment){
        const htmlContent = this.md.render(comment.comment);
        return html`${unsafeHTML(htmlContent)}`;
    }

    private fetchAllComments(){
        fetch(`${this.serverUrl}/comment/${this.ref}`)
            .then(response => response.json())
            .then(response => this.comments = response as Comment[]);
    }

    private postComment() {
        let comment = {
            ref: this.ref,
            name: this.name,
            comment: this.comment
        } as Comment;
        fetch(`${this.serverUrl}/comment`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(comment),
        })
        .then(response => response.json())
        .then(response => {
                this.comments = response as Comment[];
                this.clear();
            }
        );
    }

    private clear(){
        this.name = "";
        this.comment = "";
    }

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

}