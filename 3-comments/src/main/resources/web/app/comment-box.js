import {LitElement, html, css} from 'lit';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';

class CommentBox extends LitElement {

    static styles = css``;

    static properties = {
        // TODO: add properties definition
    };

    constructor() {
        super();
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