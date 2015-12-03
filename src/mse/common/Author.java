package mse.common;

import java.io.File;

//public enum Author {
//
//    BIBLE(0, "Bible", "Bible", "bible", "books", true),
//    HYMNS(1, "Hymns", "Hymns", "hymns", "hymns", true),
//    TUNES(2, "Tunes", "Hymn Tunes", "tunes", "tunes", false),
//    JND(3, "JND", "J.N.Darby", "jnd", "jnd", true),
//    JBS(4, "JBS", "J.B.Stoney", "jbs", "jbs", true),
//    CHM(5, "CHM", "C.H.Mackintosh", "chm", "chm", true),
//    FER(6, "FER", "F.E.Raven", "fer", "fer", true),
//    CAC(7, "CAC", "C.A.Coates", "cac", "cac", true),
//    JT(8, "JT", "J.Taylor Snr", "jt", "jt", true),
//    GRC(9, "GRC", "G.R.Cowell", "grc", "grc", true),
//    AJG(10, "AJG", "A.J.Gardiner", "ajg", "ajg", true),
//    SMC(11, "SMC", "S.McCallum", "smc", "smc", true),
//    WJH(12, "WJH", "W.J.House", "wjh", "wjh", true),
//    Misc(13, "Misc", "Various Authors", "misc", "misc", true);
//
//    private final int index;
//    private final String code;
//    private final String name;
//    private final String folder;
//    private final String contentsName;
//    private final boolean searchable;
//    private String targetFolder;
//
//    Author(int index, String code, String name, String folder, String contentsName, boolean searchable) {
//        this.index = index;
//        this.code = code;
//        this.name = name;
//        this.folder = folder;
//        this.contentsName = contentsName;
//        this.searchable = searchable;
//        this.targetFolder = "target";
//    }
//
//    public void setTargetFolder(String targetFolder) {
//        this.targetFolder = targetFolder;
//    }
//
//    public String getCode() {
//        return code;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getPreparePath() {
//        return "source" + File.separator + folder + File.separator;
//    }
//
//    public String getPreparePath(String filename) {
//        return "source" + File.separator + folder + File.separator + filename;
//    }
//
//    public String getTargetPath() {
//        return targetFolder + File.separator + folder + File.separator;
//    }
//
//    public String getTargetPath(String filename) {
//        return targetFolder + File.separator + folder + File.separator + filename;
//    }
//
//    public String getContentsName() {
//        return contentsName;
//    }
//
//    public String getIndexFilePath() {
//        return getTargetPath("index-" + getCode() + ".idx");
//    }
//
//    public boolean isSearchable() {
//        return searchable;
//    }
//
//    public int getIndex() {
//        return index;
//    }
//}

public enum Author {

    BIBLE(0, "Bible", "Bible", "bible", 66, false, true),
    HYMNS(1, "Hymns", "Hymns", "hymns", 5, false, true),
    TUNES(2, "Tunes", "Hymn Tunes", "tunes", 100, false, false),
    JND(3, "JND", "J.N.Darby", "jnd", 52, true, true),
    JBS(4, "JBS", "J.B.Stoney", "jbs", 17, true, true),
    CHM(5, "CHM", "C.H.Mackintosh", "chm", 18, true, true),
    FER(6, "FER", "F.E.Raven", "fer", 21, true, true),
    CAC(7, "CAC", "C.A.Coates", "cac", 37, true, true),
    JT(8, "JT", "J.Taylor Snr", "jt", 103, true, true),
    GRC(9, "GRC", "G.R.Cowell", "grc", 88, true, true),
    AJG(10, "AJG", "A.J.Gardiner", "ajg", 11, true, true),
    SMC(11, "SMC", "S.McCallum", "smc", 10, true, true),
    WJH(12, "WJH", "W.J.House", "wjh", 23, true, true),
    Misc(13, "Misc", "Various Authors", "misc", 26, true, true);

    private final int index;
    private final String code;
    private final String name;
    private final String folder;
    private final int numVols;
    private final boolean isMinistry;
    private final boolean searchable;
    private String targetFolder;

    Author(int index, String code, String name, String folder, int numVols, boolean isMinistry, boolean searchable) {
        this.index = index;
        this.code = code;
        this.name = name;
        this.folder = folder;
        this.numVols = numVols;
        this.isMinistry = isMinistry;
        this.searchable = searchable;
        this.targetFolder = "target";
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPreparePath() {
        return "source" + File.separator + folder + File.separator;
    }

    public String getPreparePath(String filename) {
        return "source" + File.separator + folder + File.separator + filename;
    }

    public String getPrepareSourceName(int volNumber) {
        return folder + volNumber + ".txt";
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public String getTargetPath() {
        return targetFolder + File.separator + folder + File.separator;
    }

    public String getTargetPath(String filename) {
        return targetFolder + File.separator + folder + File.separator + filename;
    }

    public String getVolumePath(int volumeNumber) {
        return getTargetPath(folder + volumeNumber + ".htm");
    }

    public String getContentsName() {
        return code + "-Contents.htm";
    }

    public String getIndexFilePath() {
        return getTargetPath("index-" + getCode() + ".idx");
    }

    public boolean isMinistry() {
        return isMinistry;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public int getNumVols() {
        return numVols;
    }

    public int getIndex() {
        return index;
    }
}