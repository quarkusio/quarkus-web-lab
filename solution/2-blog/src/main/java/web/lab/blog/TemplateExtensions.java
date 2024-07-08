package web.lab.blog;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import io.quarkus.qute.TemplateExtension;

/**
 * Add your custom Qute extension methods here.
 */
@TemplateExtension
public class TemplateExtensions {

    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    public static String mdToHtml(String string) {
        Node document = PARSER.parse(string);
        return RENDERER.render(document);
    }

    public static String toAbstract(BlogEntry entry) {
        Document doc = Jsoup.parse(mdToHtml(entry.content));
        String text = doc.body().text();
        return text.length() > 400 ? text.substring(0, 400) + "..." : text;
    }

    public static String monthStr(LocalDate date) {
        return date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }


}
