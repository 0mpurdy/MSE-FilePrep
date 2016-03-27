package mse.helpers;

import mse.common.Author;
import mse.common.Config;
import mse.data.PreparePlatform;

import java.io.File;
import java.io.IOException;

/**
 * Created by Michael Purdy on 28/12/2015.
 *
 * This performs common file operations
 */
public class FileHelper {

    public static String checkSourceFolder(Config cfg, String folder) throws IOException {
        String pathName = cfg.getResDir() + "source" + File.separator + folder + File.separator;
        File f = new File(pathName);
        System.out.print("\rReading " + folder + " from " + f.getCanonicalPath());
        if (f.exists() || f.mkdirs()) {
            return pathName;
        } else {
            return null;
        }
    }

    public static String checkTargetFolder(Config cfg, String targetPath) throws IOException {
        String pathName = cfg.getResDir() + targetPath + File.separator;
        File f = new File(pathName);
        System.out.print("\rWriting to " + f.getCanonicalPath());
        if (f.exists() || f.mkdirs()) {
            return pathName;
        } else {
            return null;
        }
    }

    private static final String A_TARGET_FOLDER = "files/target_a";

    // region target paths

    public static String getTargetPath(Author author, PreparePlatform platform) {
        return platform.getTargetFolder() + File.separator + author.getPath();
    }

    public static String getTargetPath(Author author, String filename, PreparePlatform platform) {
        return platform.getTargetFolder() + File.separator + author.getPath() + filename;
    }

    public static String getTargetVolumePath(Author author, int volumeNumber, PreparePlatform platform) {
        return getTargetPath(author, author.getVolumeName(volumeNumber), platform);
    }

    public static String getIndexTargetPath(Author author, PreparePlatform platform) {
        return getTargetPath(author, author.getIndexFileName(), platform);
    }


    public static String getHtmlLink(Author author, String filename, PreparePlatform platform) {
        return "../../../" + platform.getTargetFolder() + "/" + author.getPath() + filename;
    }

    // endregion

}
