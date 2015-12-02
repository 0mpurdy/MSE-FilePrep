package mse;

import java.io.PrintWriter;

/**
 * Created by Michael on 02/12/2015.
 */
public class HtmlHelper {

    public static void writeHtmlHeader(PrintWriter pw, String title, String mseStyleLocation) {
        pw.println("<html>");
        pw.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + mseStyleLocation + "\">\n");
        pw.println("<head>\n\t<title>" + title + "</title>\n</head>\n\n<body>");
    }
}
