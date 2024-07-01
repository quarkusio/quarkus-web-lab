import { LitElement, html, css} from 'lit';
import { customElement, state, property } from 'lit/decorators.js';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';

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
    serverUrl: string = SERVER_URL;

    // TODO: add properties and states here

    connectedCallback() {
        super.connectedCallback();
        // TODO: fetch the comments from the backend
    }

    render() {
        return html`<span>Hello world</span>`;
    }

    static styles = css``;

}