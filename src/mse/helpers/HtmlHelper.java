package mse.helpers;

import mse.common.Author;
import mse.data.BibleBook;
import mse.data.BiblePrepareCache;
import mse.data.HymnBook;

import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * Created by Michael on 02/12/2015.
 */
public class HtmlHelper {

    private static String bootstrapLocation = "../../bootstrap/css/bootstrap.css";

    public static void writeHtmlHeader(PrintWriter pw, String title, String mseStyleLocation) {
        pw.println(getHtmlHeader(title, mseStyleLocation));
    }

    public static String getHtmlHeader(String title, String mseStyleLocation) {
        return "<!DOCTYPE html>\n\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
                "\t<title>" + title + "</title>\n" +
                "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + bootstrapLocation + "\">\n" +
                "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + mseStyleLocation + "\">\n" +
                "</head>";
    }

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
                "\n\t\t</tr>"+
                "\n\t\t<tr>" +
                "\n\t\t\t<td></td>" +
                "\n\t\t\t<td><strong>Darby Translation (1889)</strong> %s</td>" +
                "\n\t\t\t<td><strong>Authorised (King James) Version (1796)</strong></td>" +
                "\n\t\t</tr>"
                , bpc.chapter, bpc.book.getName(), bpc.chapter, bpc.synopsisLink);
    }

    // endregion

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
}
