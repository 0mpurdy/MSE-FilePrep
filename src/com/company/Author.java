package com.company;

import java.io.File;

public enum Author {

    BIBLE("Bible", "Bible", "bible", "books", true),
    HYMNS("Hymns", "Hymns", "hymns", "hymns", true),
    TUNES("Tunes", "Hymn Tunes", "tunes", "tunes", false),
    JND("JND", "J.N.Darby", "jnd", "jnd", true),
    JBS("JBS", "J.B.Stoney", "jbs", "jbs", true),
    CHM("CHM", "C.H.Mackintosh", "chm", "chm", true),
    FER("FER", "F.E.Raven", "fer", "fer", true),
    CAC("CAC", "C.A.Coates", "cac", "cac", true),
    JT("JT", "J.Taylor Snr", "jt", "jt", true),
    GRC("GRC", "G.R.Cowell", "grc", "grc", true),
    AJG("AJG", "A.J.Gardiner", "ajg", "ajg", true),
    SMC("SMC", "S.McCallum", "smc", "smc", true),
    Misc("Misc", "Various Authors", "misc", "misc", true);

    private final String code;
    private final String name;
    private final String folder;
    private final String contentsName;
    private final boolean searchable;

    Author(String code, String name, String folder, String contentsName, boolean searchable) {
        this.code = code;
        this.name = name;
        this.folder = folder;
        this.contentsName = contentsName;
        this.searchable = searchable;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getFolder() {
        return folder;
    }

    public String getContentsName() {
        return contentsName + ".htm";
    }

    public String getContentsPath() {
        return folder + File.separator + contentsName + ".htm";
    }

    public String getIndexName() {
        return contentsName + ".idx";
    }

    public String getIndexPath() {
        return folder + File.separator + contentsName + ".idx";
    }

    public boolean isSearchable() {
        return searchable;
    }

}
