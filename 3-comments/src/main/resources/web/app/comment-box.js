import {LitElement, html, css} from 'lit';

class CommentBox extends LitElement {

    static styles = css``;

    static properties = {
        // TODO: add ref property
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
    }

    render() {
        return html`<span>Hello world</span>`;
    }
}
customElements.define('comment-box', CommentBox);