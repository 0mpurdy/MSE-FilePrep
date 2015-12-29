package mse.helpers;

import mse.data.AuthorPrepareCache;
import mse.data.BibleBook;
import mse.data.BiblePrepareCache;

import java.io.PrintWriter;


/**
 * Created by Michael Purdy on 02/12/2015.
 * <p>
 * Helps with creation of HTML files
 */
public class HtmlHelper {

    private static String bootstrapCssLocation = "../../bootstrap/css/bootstrap.min.css";
    private static String bootstrapJsLocation = "../../bootstrap/js/bootstrap.min.js";

    // region genericStart

    public static void writeHtmlHeader(PrintWriter pw, String title, String mseStyleLocation) {
        pw.println(getHtmlHeader(title, mseStyleLocation));
    }

    public static String getHtmlHeader(String title, String mseStyleLocation) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n\n" +
                "<head>\n" +
                "\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                "\t<title>" + title + "</title>\n" +
                "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + bootstrapCssLocation + "\">\n" +
                "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + mseStyleLocation + "\">\n" +
                "</head>";
    }

    public static void writeStart(PrintWriter pw) {
        pw.println(getStart());
    }

    public static void writeStartAndContainer(PrintWriter pw) {
        pw.println(getStart() + "\n" + getStartContainer());
    }

    public static String getStart() {
        return "\n<body>";
    }

    public static String getStartContainer() {
        return "\t<div class=\"container\">";
    }

    // endregion

    // region biblePrepare

    public static void writeBibleStart(PrintWriter pwBible, PrintWriter pwBibleText, BibleBook book) {
        pwBible.println("\n<body>\n\t<strong>Chapters</strong> ");
        pwBibleText.println("{#" + book.getName() + "}");
    }

    public static String getBibleChapterHeader(BiblePrepareCache bpc) {
        return String.format("\n\t</table>" +
                        "\n\t<table class=\"bible\">" +
                        "\n\t\t<tr>" +
                        "\n\t\t\t<td colspan=\"3\" class=\"chapterTitle\"><a name=%s>%s %s</a></td>" +
                        "\n\t\t</tr>" +
                        "\n\t\t<tr>" +
                        "\n\t\t\t<td></td>" +
                        "\n\t\t\t<td><strong>Darby Translation (1889)</strong> %s</td>" +
                        "\n\t\t\t<td><strong>Authorised (King James) Version (1796)</strong></td>" +
                        "\n\t\t</tr>"
                , bpc.chapter, bpc.book.getName(), bpc.chapter, bpc.synopsisLink);
    }

    // endregion

    // region ministryPrepare

    public static void writePanelStart(PrintWriter pw) {
        pw.println("<div class=\"panel-group\" id=\"accordion\">");
    }

    public static void writeContentsTitle(PrintWriter pw, String title) {
        pw.println("\t<div class=\"container\">\n" +
                "\t\t<p id=\"0\" class=\"contents-title\">" + title + "</p>\n" +
                "\t</div>\n" +
                "\t<div class=\"container\">");
    }

    public static void writePanelGroupOpen(PrintWriter pw) {
        pw.println("\t\t<div class=\"panel panel-default\">");
    }

    public static void writePanelHeading(PrintWriter pw, int volNum, String heading) {
        pw.println("\t\t\t<div class=\"panel-heading\">\n" +
                "\t\t\t\t<div class=\"panel-title\">\n" +
                "\t\t\t\t\t<h4 class=\"panel-title\">\n" +
                "\t\t\t\t\t\t<a data-toggle=\"collapse\" data-parent=\"#accordion\" href=\"#collapse" + volNum + "\">Volume " + volNum + " - " + heading + "</a>\n" +
                "\t\t\t\t\t</h4>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>");
    }

    public static void writePanelBodyOpen(PrintWriter pw, int volNum) {
        pw.println("\t\t\t<div id=\"collapse" + volNum + "\" class=\"panel-collapse collapse\">\n" +
                "\t\t\t\t<div class=\"panel-body\">");
    }

    public static void writeSinglePanelBodyClose(PrintWriter pw) {
        pw.println("\t\t\t\t\t</div>\n" +
                "\t\t\t\t</div>");
    }

    public static void writeContentsClose(PrintWriter pw) {
        pw.println("\t\t\t\t</div>\n" +
                "\t\t\t</div>\n" +
                "\t\t</div>\n" +
                "\t</div>\n" +
                "</body>\n" +
                "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>\n" +
                "<script src=\"" + bootstrapJsLocation + "\"></script>\n" +
                "\n" +
                "</html>");
    }

    public static StringBuilder wrapContent(AuthorPrepareCache apc, StringBuilder content) {
        // add formatting to the line
        if (apc.cssClass.equals("")) {
            apc.cssClass = "paragraph";
        }
        content.insert(0, "\t\t<div class=\"" + apc.cssClass + "\">\n\t\t\t");
        content.append("\n\t\t</div>");
        return content;
    }

    // endregion

    // region htmlManipulation

    public static String removeHtml(String line) {
        return removeHtml(new StringBuilder(line)).toString();
    }

    public static StringBuilder removeHtml(StringBuilder line) {
        int charPos = 0;

        while (++charPos < line.length()) {
            if (line.charAt(charPos) == '<') {
                int tempCharIndex = charPos + 1;
                while (tempCharIndex < line.length() - 1 && line.charAt(tempCharIndex) != '>') tempCharIndex++;
                tempCharIndex++;
                line.replace(charPos, tempCharIndex, "");
            }
        }

        return line;
    }

    // endregion
}
