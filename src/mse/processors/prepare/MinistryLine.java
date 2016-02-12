package mse.processors.prepare;

/**
 * Created by michaelpurdy on 08/02/2016.
 */
public class MinistryLine {

    private String line;
    private StringBuilder htmlLine;
    private String cssClass;

    public MinistryLine(String line) {
        this.line = line;
        this.htmlLine = new StringBuilder(line);
        this.cssClass = "";
    }

    public StringBuilder getHtmlLine() {
        return htmlLine;
    }

    public void setHtmlLine(StringBuilder htmlLine) {
        this.htmlLine = htmlLine;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}
