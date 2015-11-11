package mse;

/**
 * Created by mj_pu_000 on 11/11/2015.
 */
public class AuthorPrepareCache {

    // public variables are bad but they're faster than a method call

    public int volNum;
    public int pageNum;

    public int keepPageNumber;

    public boolean finishedVolumes = false;

    public int lineCount;

    public String line;

    public int section;

    public String footnotes;
    public String actualFootnotes;
    public int footnotesNumber;
    public int actualFootnotesNumber;
    public int maxFootnotesNumber;

    // css class
    public String cssClass = "";

    public boolean startedItalics = false;

    String messages = "";

    public AuthorPrepareCache() {
        lineCount = 0;
        volNum = 1;
        section = 1;
        footnotes = "";
        actualFootnotes = "";
        footnotesNumber = 0;
        actualFootnotesNumber = 0;
        maxFootnotesNumber = 0;
        cssClass = "";
        finishedVolumes = false;
        startedItalics = false;
        messages = "";
    }

}
