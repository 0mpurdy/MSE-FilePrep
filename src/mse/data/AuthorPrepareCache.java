package mse.data;

import mse.common.Author;

/**
 * Created by mj_pu_000 on 11/11/2015.
 */
public class AuthorPrepareCache {

    // public variables are bad but they're faster than a method call

    public Author author;

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

    public String messages = "";

    public AuthorPrepareCache(Author author) {
        this.author = author;
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

    public void clearPageValues() {
        footnotes = "";
        actualFootnotes = "";
        footnotesNumber = 0;
        maxFootnotesNumber = 0;
        actualFootnotesNumber = 0;
    }

    public void clearVolumeValues() {
        pageNum = 0;
        keepPageNumber = 0;
        clearPageValues();
    }

}
