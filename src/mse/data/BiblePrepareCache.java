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

    public BiblePrepareCache(Config cfg, PreparePlatform platform) throws IOException {
        this.cfg = cfg;
        jndBiblePath = FileHelper.checkSourceFolder(platform, FileConstants.JND_BIBLE_FOLDER);
        kjvBiblePath = FileHelper.checkSourceFolder(platform, FileConstants.KJV_BIBLE_FOLDER);
        bibleDestinationPath = FileHelper.checkTargetFolder(FileHelper.getTargetPath(Author.BIBLE, platform));
        bibleTxtDestinationPath = FileHelper.checkTargetFolder(platform.getTargetPath() + File.separator + FileConstants.BIBLE_TEXT_OUTPUT_FOLDER);
    }

    public String getSynopsisSource() {
        return jndBiblePath + File.separator + FileConstants.JND_SYNOPSIS_SOURCE_NAME;
    }

    public String getJndSource() {
        return jndBiblePath + File.separator + FileConstants.JND_BIBLE_FOLDER + bookNumber + FileConstants.SOURCE_FILE_ENDING;
    }

    public String getKjvSource() {
        return kjvBiblePath + File.separator + FileConstants.KJV_BIBLE_FOLDER + bookNumber + FileConstants.SOURCE_FILE_ENDING;
    }

    public String getBibleOutput() {
        return bibleDestinationPath + File.separator + book.getTargetFilename();
    }

    public void nextBook(BibleBook nextBook) {
        this.book = nextBook;
        bookNumber++;
    }

    public String getBibleTextOutput() {
        return bibleTxtDestinationPath + File.separator + Author.BIBLE.getCode().toLowerCase() + bookNumber + FileConstants.SOURCE_FILE_ENDING;
    }
}
