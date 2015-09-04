package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    private String[] AuthorCodes
            = {"Bible", "JND", "JBS", "CHM", "FER", "CAC", "JT", "GRC", "AJG", "SMC", "Misc", "Hymns"};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Config cfg = new Config();

        Scanner sc = new Scanner(System.in);

        System.out.println("MSE File Prep console application");
        System.out.println("Version: " + cfg.getMseVersion());
        System.out.println();

        printMainMenu();
        int mainMenuChoice;
        mainMenuChoice = sc.nextInt();
        sc.nextLine();

        switch (mainMenuChoice) {
            case 0:
                System.out.println("Not implemented");
                break;
            case 1:
                System.out.println("\nWhich author do you wish to prepare?");
                printAuthorMenu();
                int authorChoice = sc.nextInt();
                sc.nextLine();

                switch (authorChoice) {
                    case 0:
                        System.out.println("\nPreparing " + Author.values()[authorChoice].getName());
                        prepareBibleHtml(cfg);
                        break;
                    default:
                        System.out.println("\nOption " + authorChoice + " is not available at the moment");
                }

                break;
            default:
                System.out.println("Invalid choice");
        }

        System.out.println();
        System.out.println();

    }

    private static void printMainMenu() {

        System.out.println("Options: ");

        ArrayList<String> options = new ArrayList<>();
        options.add("Prepare all files");
        options.add("Prepare single author");

        int i = 0;
        for (String option : options) {
            System.out.println(i + " - " + option);
            i++;
        }
        System.out.print("Choose an option: ");

    }

    private static void printAuthorMenu() {
        System.out.println("Authors: ");
        int i = 0;
        for (Author nextAuthor : Author.values()) {
            System.out.println(i + " - " + nextAuthor.getName());
            i++;
        }
        System.out.print("Choose an option: ");
    }

    private static void prepareBibleHtml(Config cfg) {

        try {
            // get the paths for the files that are used in preparing the bible html
            String jndBiblePath = cfg.getPrepareDir() + "bible" + File.separator + "best" + File.separator;
            String kjvBiblePath = cfg.getPrepareDir() + "kjv" + File.separator + "best" + File.separator;
            System.out.println("Reading JND bible from " + jndBiblePath);
            System.out.println("Reading KJV bible from " + kjvBiblePath);

            // get the file paths that the bible html and text will be written to
            String bibleDestinationPath = cfg.getPrepareDir() + "target" + File.separator + "bible" + File.separator;
            String bibleTxtDestinationPath = cfg.getPrepareDir() + "target" + File.separator + "bibleText" + File.separator;
            System.out.println("Writing bible text to " + bibleTxtDestinationPath);
            System.out.println("Writing bible html to " + bibleDestinationPath);

            String jndLine; //line read from JND file
            String kjvLine; //line read from KJV file
            String bookName = null;
            String chapter = "1";
            String synopsisLink = null;
            String verseNum = null;
            String jndVerse = null;
            String kjvVerse = null;
            String bufferString = null;
            String bufferTxt = null;
            boolean startedItalic = false;

            // get the synopsis pages map
            HashMap<String, String> synopsisPages = getSynopsisPages(jndBiblePath + "pages.txt");

            int i = 0;
            // for each book in the bible
            for (BibleBook nextBook : BibleBook.values()) {
                i++;
                // create buffered readers to read the jnd and kjv txt files
                BufferedReader brJND = new BufferedReader(new FileReader(jndBiblePath + "bible" + i + ".txt"));
                BufferedReader brKJV = new BufferedReader(new FileReader(kjvBiblePath + "kjv" + i + ".txt"));

                String x;
                if (i < 10) {
                    x = "0" + i;
                } else {
                    x = i + "";
                }

                // create print writers to write the bible html and txt (overwrite any existing files)
                PrintWriter pwBible = new PrintWriter(new FileWriter(bibleDestinationPath + nextBook.getName() + ".htm", false));
                PrintWriter pwBibleTxt = new PrintWriter(new FileWriter(bibleTxtDestinationPath + "bible" + x + ": " + nextBook.getName() + ".doc", false));

                // write the title on the outputs
                pwBible.println("<html><head><title>"
                        + "Darby translation and King James Version of The Bible"
                        + "</title></head><body bgcolor=\"#FFFFFF\" link=\"#0000FF\" vlink=\"#0000FF\" alink=\"#0000FF\"><b>Chapters</b> ");
                pwBibleTxt.println("{#" + nextBook.getName() + "}");

                // for each chapter
                for (int chapterCount = 1; chapterCount <= nextBook.getNumChapters(); i++) {
                    pwBible.println("<a href=\"#" + chapterCount + "\">" + chapterCount + "</a> ");
                }
                pwBible.println("<br><br><table border=\"1\" cellspacing=\"0\" cellpadding=\"5\">");

                // read the first line of the two versions and assume there are more lines
                boolean finished = false;
                jndLine = brJND.readLine();
                kjvLine = brKJV.readLine();

                // while there are more lines
                while (!finished) {

                    // if it is a chapter heading
                    if (jndLine.indexOf("{") == 0) {
                        chapter = jndLine.substring(1, jndLine.length() - 1);

                        // if including synopsis
                        if (cfg.isSynopsis()) {
                            synopsisLink = synopsisPages.get(i + "/" + chapter);
                            if (synopsisLink == null) {
                                System.out.println("No synopsis link for " + nextBook.getName() + " chapter " + chapter);
                                synopsisLink = "";
                            } else {
                                System.out.println("Synopsis page for " + nextBook.getName() + " chapter " + chapter + " = " + synopsisLink);
                            }
                        } else {
                            // if not including synopsis
                            synopsisLink = "";
                        }
                        // add the chapter title row for the html output
                        bufferString = String.format("<tr>\n\t<td colspan=\"3\"><h2><a name=%s>%s %s</a></h2></td>\n</tr>\n", chapter, nextBook.getName(), chapter);
                        // add the translation and synopsis for the html output
                        bufferString += String.format("<tr>\n\t<td><\td>\n\t<td><strong>Darby Translation (1889)</strong> - %s</td>\n\t<td><strong>Authorised (King James) Version (1796)</strong></td>\n</tr>", synopsisLink);
                        // add the text for the text output
                        bufferTxt = String.format("{%s}", chapter);
                    } else {
                        // if it is a verse

                        // get the verse number and content
                        verseNum = jndLine.substring(0, jndLine.indexOf(" "));
                        jndVerse = jndVerse.substring(jndLine.indexOf(" ")).trim();
                        kjvVerse = kjvLine.substring(kjvLine.indexOf(" ")).trim();

                        // create the html output
                        bufferString = String.format("<tr>\n\t<td><a name=%s:%s>%s</a></td>\n", chapter, verseNum, verseNum);
                        bufferString += String.format("\t<td>%s</td>\n\t<td>%s</td>\n<tr>");

                        // create the text output
                        bufferTxt = verseNum + " " + jndVerse + " " + kjvVerse;

                        // legacy logging of short verses to find paragraph problems
                        if (jndVerse.length() < 5) {
                            System.out.println("Short verse: " + bufferString);
                        }

                        // insert italics
                        while (bufferString.indexOf("*") != -1) {
                            if (startedItalic) {
                                bufferString = bufferString.substring(0,bufferString.indexOf("*")) + "</i>" + bufferString.substring(bufferString.indexOf("*") + 1);
                            } else {
                                bufferString = bufferString.substring(0, bufferString.indexOf("*")) + "<i>" + bufferString.substring(bufferString.indexOf("*") + 1);
                            }
                            startedItalic = !startedItalic;
                        }

                    }

                    // write the html and text output
                    pwBible.println(bufferString);
                    pwBibleTxt.println(bufferTxt);

                    // check if italics run over a line
                    if (startedItalic) {
                        System.out.println("Italics - " + bufferTxt);
                    }

                    // read the next line to process
                    jndLine = brJND.readLine();
                    kjvLine = brKJV.readLine();
                    if ((jndLine == null) && (kjvLine == null)) {
                        finished = true;
                    }
                }

                // write the end of the html document
                pwBible.println("</table>\n</body>\n</html>");

                // close the print writers
                pwBible.close();
                pwBibleTxt.close();

                // close the readers
                brJND.close();
                brKJV.close();

                // log finished book
                System.out.println("*** Finished " + nextBook.getName());

            }
        } catch (Exception e) {
            System.out.println("!*** Problem formatting bible ***!");
            e.printStackTrace();
        }

    }

    private static HashMap<String, String> getSynopsisPages(String filename) {
        // this gets a map of the corresponding synopsis page in jnd's ministry for
        // each book of the bible

        HashMap<String, String> synopsisMap = new HashMap<>();

        // boolean used to check if there are more pages to read
        boolean morePages = true;

        // buffer for the synopsis file input
        String synopsisLine;

        BufferedReader brPages = null;

        try {
            // buffered reader for reading the synopsis pages link file
            brPages = new BufferedReader(new FileReader(filename));

            // populate the synopsis pages hash map
            while (morePages) {//still more lines in pages.txt
                if ((synopsisLine = brPages.readLine()) != null) {
                    // get the synopsis for a bible book
                    // links are stored in the format {bible book}, {bible book chapter}, {jnd volume}, {jnd volume page}

                    String[] synopsisNumbers = synopsisLine.split(",");
                    synopsisMap.put(String.format("%s/%s", synopsisNumbers[0], synopsisNumbers[2]),
                            String.format(" - <a href=\"../jnd/jnd%s.htm#%s\">go to synopsis</a>", synopsisNumbers[2], synopsisNumbers[3]));
                } else {
                    morePages = false;
                }
            }
        } catch (IOException ex) {
            System.out.println("!***Error creating synopsis hashmap***!");
        } finally {
            if (brPages != null) {
                try {
                    brPages.close();
                } catch (IOException ex) {
                }
            }
        }

        return synopsisMap;
    }
}
