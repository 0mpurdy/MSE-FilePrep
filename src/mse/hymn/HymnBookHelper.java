package mse.hymn;

/**
 * Methods for generating filenames for hymn books
 *
 * @author MichaelPurdy
 */
public class HymnBookHelper {

    public static String[] HYMN_BOOK_FILE_NAMES = {"hymns1973", "hymns1962", "hymns1951", "hymns1932", "hymns1903"};

    public static String getFileName(HymnBook hymnBook) {
        return "hymns" + hymnBook.getYear();
    }

    public static String getSourceFilename(HymnBook hymnBook) {
        return getSourceFilename(getFileName(hymnBook));
    }

    public static String getSourceFilename(String name) {
        return name + ".txt";
    }

    public static String getTargetHtmlFilename(HymnBook hymnBook) {
        return getTargetHtmlFilename(getFileName(hymnBook));
    }

    public static String getTargetHtmlFilename(String name) {
        return name + ".html";
    }

    public static String getTargetJsonFilename(HymnBook hymnBook) {
        return getTargetJsonFilename(getFileName(hymnBook));
    }

    public static String getTargetJsonFilename(String name) {
        return name + ".json";
    }

    public static String getContentsName(HymnBook hymnBook) {
        return getContentsName(getFileName(hymnBook));
    }

    public static String getContentsName(String name) {
        return name + "-contents.html";
    }

    public static String getSerializedName(HymnBook hymnBook) {
        return getSerializedName(getFileName(hymnBook));
    }

    public static String getSerializedName(String name) {
        return name + ".ser";
    }
}
