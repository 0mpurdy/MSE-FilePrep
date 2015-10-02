package mse.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Config {

    // the number of times a word has to appear before it is too frequent
    public final int TOO_FREQUENT = 10000;

    private String mseVersion;
    private String defaultBrowser;
    private String workingDir;
    private String resDir;
    private String resultsFileName;
    private String searchString;
    private String searchType;
    private ArrayList<Boolean> selectedAuthors;
    private boolean synopsis;
    private boolean beep;
    private boolean splashWindow;
    private boolean autoLoad;
    private boolean fullScan;
    private boolean loggingActions;
    private boolean debugOn;

    public Config() {
        setDefaults();
    }

    private void setDefaults() {

        mseVersion = "3.0.0";
        workingDir = "";
        resDir = "PrepareRes" + File.separator;
        defaultBrowser = "/usr/bin/firefox";
        resultsFileName = "search_results.htm";
        searchString = "";
        searchType = "Phrase";

        // set the selected books to be searched to only the bible
        selectedAuthors = new ArrayList<>();
        for (Author nextAuthor : Author.values()) {
            if (nextAuthor.isSearchable()) {
                selectedAuthors.add(false);
            }
        }
        selectedAuthors.set(0, true);

        synopsis = true;
        beep = false;
        splashWindow = false;
        autoLoad = false;
        fullScan = false;
        debugOn = false;

    }

    public String getMseVersion() {
        return mseVersion;
    }

    public String getDefaultBrowser() {
        return defaultBrowser;
    }

    public void setDefaultBrowser(String defaultBrowser) {
        this.defaultBrowser = defaultBrowser;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public String getResDir() {
        return resDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getResultsFileName() {
        return resultsFileName;
    }

    public void setResultsFileName(String resultsFileName) {
        this.resultsFileName = resultsFileName;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public ArrayList<Boolean> getSelectedAuthors() {
        return selectedAuthors;
    }

    public void setSelectedAuthors(ArrayList<Boolean> selectedAuthors) {
        this.selectedAuthors = selectedAuthors;
    }

    public boolean isBeep() {
        return beep;
    }

    public void setBeep(boolean beep) {
        this.beep = beep;
    }

    public boolean isSplashWindow() {
        return splashWindow;
    }

    public void setSplashWindow(boolean splashWindow) {
        this.splashWindow = splashWindow;
    }

    public boolean isAutoLoad() {
        return autoLoad;
    }

    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    public boolean isFullScan() {
        return fullScan;
    }

    public void setFullScan(boolean fullScan) {
        this.fullScan = fullScan;
    }

    public boolean isDebugOn() {
        return debugOn;
    }

    public void setDebugOn(boolean debugOn) {
        this.debugOn = debugOn;
    }

    public boolean isSynopsis() {
        return synopsis;
    }

    public void setSynopsis(boolean synopsis) {
        this.synopsis = synopsis;
    }

    public boolean isLoggingActions() {
        return loggingActions;
    }

    public void setLoggingActions(boolean loggingActions) {
        this.loggingActions = loggingActions;
    }

    public void writeToLog(String message) {

        if (loggingActions) {
            FileWriter logWriter = null;
            try {
                File logFile = new File(workingDir + "log.txt");
                logWriter = new FileWriter(logFile, true);
                logWriter.write("\n" + message);
            } catch (IOException ioe) {
                // ignore io exception
            } finally {
                if (logWriter != null) {
                    try {
                        logWriter.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

}
