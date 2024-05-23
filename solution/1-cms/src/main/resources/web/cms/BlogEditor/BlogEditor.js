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
