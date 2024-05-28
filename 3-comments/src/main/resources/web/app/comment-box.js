import {LitElement, html, css} from 'lit';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';

class CommentBox extends LitElement {

    static styles = css``;

    static properties = {
        // TODO: add properties definition
    };

    constructor() {
        super();
        this.serverUrl = SERVER_URL;  // This is defined through web-bundler envs in application.properties
        // TODO: add properties initialization
    }

    connectedCallback() {
        super.connectedCallback();
        // TODO: fetch the comments from the backend
    }

    render() {
        return html`<span>Hello world</span>`;
    }
}
customElements.define('comment-box', CommentBox);