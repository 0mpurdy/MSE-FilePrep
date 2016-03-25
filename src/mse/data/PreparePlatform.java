package mse.data;

import mse.common.Author;

/**
 * Created by Michael on 12/12/2015.
 */
public enum PreparePlatform {

    PC("PC", "target", "../../mseStyle.css", "", false),
    ANDROID("Android", "target_a", "../../mseStyle.css", "mse:", true);

    private String name;
    private String targetFolder;
    private String stylesLink;
    private String linkPrefix;
    private boolean fullLink;

    PreparePlatform(String name, String targetFolder, String stylesLink, String linkPrefix, boolean fullLink) {
        this.name = name;
        this.targetFolder = targetFolder;
        this.stylesLink = stylesLink;
        this.linkPrefix = linkPrefix;
        this.fullLink = fullLink;
    }

    public String getName() {
        return name;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public String getStylesLink() {
        return stylesLink;
    }

    public String getLinkPrefix(Author author) {
        String link = "";
        if (fullLink) link = author.getTargetPath();
        return linkPrefix + link;
    }
}
