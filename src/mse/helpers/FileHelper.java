package mse.helpers;

import mse.common.Config;

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

}
