package web.lab;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import io.quarkus.qute.TemplateExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Add your custom Qute extension methods here.
 */
@TemplateExtension
public class TemplateExtensions {


    public static String mdToHtml(String string) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(string);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    public static String toAbstract(String string) {
        Document doc = Jsoup.parse(string);
        String text = doc.body().text();
        return text.length() > 150 ? text.substring(0, 150) + "..." : text;
    }

}
