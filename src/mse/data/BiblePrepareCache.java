package mse.data;

import mse.common.Author;
import mse.common.Config;
import mse.helpers.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by michaelpurdy on 28/12/2015.
 */
public class BiblePrepareCache {

    public Config cfg;
    public String jndBiblePath;
    public String kjvBiblePath;
    public String bibleDestinationPath;
    public String bibleTxtDestinationPath;

    public HashMap<String, String> synopsisPages;

    public int bookNumber = 0;

    public String jndLine; //line read from JND file
    public String kjvLine; //line read from KJV file
    public String chapter = "1";
    public String synopsisLink;
    public String verseNum;
    public String jndVerse;
    public String kjvVerse;
    public String bufferString;
    public String bufferTxt;
    public boolean startedItalic = false;
    public BibleBook book;

    public BiblePrepareCache(Config cfg) throws IOException {
        this.cfg = cfg;
         jndBiblePath = FileHelper.checkSourceFolder(cfg, "bible");
         kjvBiblePath = FileHelper.checkSourceFolder(cfg, "kjv");
         bibleDestinationPath = FileHelper.checkTargetFolder(cfg, Author.BIBLE.getTargetPath());
         bibleTxtDestinationPath = FileHelper.checkTargetFolder(cfg, "target" + File.separator + "bibleText");
    }

    public String getSynopsisSource() {
        return jndBiblePath + "pages.txt";
    }

    public String getJndSource() {
        return jndBiblePath + "bible" + bookNumber + ".txt";
    }

    public String getKjvSource() {
        return kjvBiblePath + "kjv" + bookNumber + ".txt";
    }

    public String getBibleOutput() {
        return bibleDestinationPath + book.getBookFileName();
    }

    public void nextBook(BibleBook nextBook) {
        this.book = nextBook;
        bookNumber++;
    }

    public String getBibleTextOutput() {
        return bibleTxtDestinationPath + Author.BIBLE.getCode() + bookNumber + ".txt";
    }
}
