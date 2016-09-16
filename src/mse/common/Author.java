package mse.common;

import mse.data.FileConstants;
import mse.data.PreparePlatform;

import java.io.File;

public enum Author {

    // region authors

    BIBLE(0, "Bible", "Bible", "bible", 66, false, true, true),
    HYMNS(1, "Hymns", "Hymns", "hymns", 5, false, true, true),
    TUNES(2, "Tunes", "Hymn Tunes", "tunes", 100, false, false, true),
    JND(3, "JND", "J.N.Darby", "jnd", 52, true, true, true),
    JBS(4, "JBS", "J.B.Stoney", "jbs", 17, true, true, false),
    CHM(5, "CHM", "C.H.Mackintosh", "chm", 18, true, false, false),
    FER(6, "FER", "F.E.Raven", "fer", 21, true, true, false),
    CAC(7, "CAC", "C.A.Coates", "cac", 37, true, true, false),
    JT(8, "JT", "J.Taylor Snr", "jt", 103, true, false, false),
    GRC(9, "GRC", "G.R.Cowell", "grc", 88, true, false, false),
    AJG(10, "AJG", "A.J.Gardiner", "ajg", 11, true, false, false),
    SMC(11, "SMC", "S.McCallum", "smc", 10, true, false, false),
    WJH(12, "WJH", "W.J.House", "wjh", 23, true, false, false),
    Misc(13, "Misc", "Various Authors", "misc", 26, true, false, false);

    // endregion

    private final int id;
    private final String code;
    private final String name;
    private final int numVols;
    private final boolean isMinistry;
    private final boolean searchable;
    private final boolean asset;

    Author(int id, String code, String name, String folder, int numVols, boolean isMinistry, boolean searchable, boolean asset) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.numVols = numVols;
        this.isMinistry = isMinistry;
        this.searchable = searchable;
        this.asset = asset;
    }

    // region folder

    public String getPath() {
        return code.toLowerCase() + File.separator;
    }

    // endregion

    // region prepare

    /**
     * Get the path to the source text file
     *
     * @param platform The platform that the files are prepared for eg: PC or Android
     * @return
     */
    public String getPreparePath(PreparePlatform platform) {
        return platform.getSourcePath() + getCode().toLowerCase() + File.separator;
    }

    /**
     * Get the path to the source text for the bible index
     *
     * @param platform The platform that the files are prepared for eg: PC or Android
     * @return The path to the source text for the bible index
     */
    public String getIndexPreparePath(PreparePlatform platform) {
        if (this.equals(BIBLE)) return platform.getTargetPath() + File.separator + FileConstants.BIBLE_TEXT_OUTPUT_FOLDER + File.separator;
        return platform.getSourcePath() + getCode().toLowerCase() + File.separator;
    }

    public String getPrepareSourceName(int volNumber) {
        return getCode().toLowerCase() + volNumber + FileConstants.SOURCE_FILE_ENDING;
    }

    // endregion

    // region filenames

    public String getContentsName() {
        return code.toLowerCase() + "-contents.html";
    }

    public String getIndexFileName() {
        return "index-" + getCode().toLowerCase() + ".idx";
    }

    public String getVolumeName(int volumeNumber) {
        return getCode().toLowerCase() + volumeNumber + ".html";
    }

    // endregion

    // region getters

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isMinistry() {
        return isMinistry;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public boolean isAsset() {
        return asset;
    }

    public int getNumVols() {
        return numVols;
    }

    public int getID() {
        return id;
    }

    // endregion

    // region reader

    public static Author getFromString(String authorString) {

        authorString = authorString.toLowerCase();

        // go through each author and check if the name or the code matches
        for (Author nextAuthor : values()) {
            if (authorString.equals(nextAuthor.getCode().toLowerCase()) ||
                    authorString.equals(nextAuthor.getName().toLowerCase()))
                return nextAuthor;
        }

        // potentially include switch statement for other string matches here

        // if no authors matched return null
        return null;

    }

    // endregion
}