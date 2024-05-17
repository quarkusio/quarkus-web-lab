import { Controller } from "@hotwired/stimulus";
import StimulusApp from "../StimulusApp";

StimulusApp.register("search", class extends Controller {
    static targets = [ "open", "close", "input" ];

    open() {
        this.inputTarget.style.display = 'block';
        this.openTarget.style.display = 'none';
        this.closeTarget.style.display = 'inline';
        this.inputTarget.focus();
    }
    close() {
        this.inputTarget.style.display = 'none';
        this.openTarget.style.display = 'inline';
        this.closeTarget.style.display = 'none';
    }

    clear() {
        this.close();
        this.inputTarget.value = '';
    }
});