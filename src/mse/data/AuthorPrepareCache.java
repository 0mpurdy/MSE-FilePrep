package mse.data;

import mse.common.Author;
import mse.processors.prepare.MinistryPage;

/**
 * Created by mj_pu_000 on 11/11/2015.
 */
public class AuthorPrepareCache {

    // public variables are bad but they're faster than a method call

    public Author author;
    public MinistryPage mPage;

    public int volNum;
    public int pageNum;

    public int prevPageNumber;

    public boolean finishedVolumes = false;

    public int lineCount;

    public String line;

    public int section;

    public int unresolvedFootnotes;
    public int resolvedFootnotes;
    public String unresolvedFootnoteIdentifier = "";
    public String resolvedFootnoteIdentifier = "";

    // css class
    public String cssClass = "";

    public boolean startedItalics = false;

    public String messages = "";

    public AuthorPrepareCache(Author author) {
        this.author = author;
        lineCount = 0;
        volNum = 1;
        section = 1;
        unresolvedFootnoteIdentifier = "";
        resolvedFootnoteIdentifier = "";
        unresolvedFootnotes = 0;
        resolvedFootnotes = 0;
        cssClass = "";
        finishedVolumes = false;
        startedItalics = false;
        messages = "";
        mPage = new MinistryPage();
    }

    public void clearPageValues() {
        unresolvedFootnotes = 0;
        resolvedFootnotes = 0;
        unresolvedFootnoteIdentifier = "";
        resolvedFootnoteIdentifier = "";

        section = 1;
    }

    public void clearVolumeValues() {
        pageNum = 0;
        prevPageNumber = 0;
        clearPageValues();
    }

    public void addMessage(String message) {
        messages += "\n\t" + message;
    }

}
