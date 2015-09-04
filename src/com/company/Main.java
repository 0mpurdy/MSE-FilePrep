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

            // create file for outputting where the files are being read/written
            File f;

            // get the paths for the files that are used in preparing the bible html
            String jndBiblePath = cfg.getPrepareDir() + "bible" + File.separator + "best" + File.separator;
            f = new File(jndBiblePath);
            System.out.println("Reading JND bible from " + f.getCanonicalPath());

            String kjvBiblePath = cfg.getPrepareDir() + "kjv" + File.separator + "best" + File.separator;
            f = new File(kjvBiblePath);
            System.out.println("Reading KJV bible from " + f.getCanonicalPath());

            // get the file paths that the bible html and text will be written to
            String bibleDestinationPath = cfg.getPrepareDir() + "target" + File.separator + "bible" + File.separator;
            f = new File(bibleDestinationPath);
            System.out.println("Writing bible HTML to " + f.getCanonicalPath());

            String bibleTxtDestinationPath = cfg.getPrepareDir() + "target" + File.separator + "bibleText" + File.separator;
            f = new File(bibleTxtDestinationPath);
            System.out.println("Writing bible text to " + f.getCanonicalPath());

            String jndLine; //line read from JND file
            String kjvLine; //line read from KJV file
            String bookName = null;
            String chapter = "1";
            String synopsisLink;
            String verseNum;
            String jndVerse = null;
            String kjvVerse;
            String bufferString;
            String bufferTxt;
            boolean startedItalic = false;

            // get the synopsis pages map
            HashMap<String, String> synopsisPages = getSynopsisPages(jndBiblePath + "pages.txt");

            System.out.println("\nPreparing Bible...\n");

            int bookNumber = 0;
            // for each book in the bible
            for (BibleBook nextBook : BibleBook.values()) {
                System.out.print("Preparing " + nextBook.getName() + " ");
                bookNumber++;
                // create buffered readers to read the jnd and kjv txt files
                BufferedReader brJND = new BufferedReader(new FileReader(jndBiblePath + "bible" + bookNumber + ".txt"));
                BufferedReader brKJV = new BufferedReader(new FileReader(kjvBiblePath + "kjv" + bookNumber + ".txt"));

                String formattedBookNumber;
                if (bookNumber < 10) {
                    formattedBookNumber = "0" + bookNumber;
                } else {
                    formattedBookNumber = bookNumber + "";
                }

                // create print writers to write the bible html and txt (overwrite any existing files)
                PrintWriter pwBible = new PrintWriter(new FileWriter(bibleDestinationPath + nextBook.getName() + ".htm", false));
                PrintWriter pwBibleTxt = new PrintWriter(new FileWriter(bibleTxtDestinationPath + "bible" + formattedBookNumber + ": " + nextBook.getName() + ".doc", false));

                // write the html header
                pwBible.println("<html>");
                pwBible.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"mseStyle.css\">\n");
                pwBible.println("<head>\n\t<title>"
                        + "Darby translation and King James Version of The Bible"
                        + "</title>\n</head>");


                pwBible.println("\n<body>\n\t<strong>Chapters</strong> ");
                pwBibleTxt.println("{#" + nextBook.getName() + "}");

                // for each chapter
                for (int chapterCount = 1; chapterCount <= nextBook.getNumChapters(); chapterCount++) {
                    pwBible.println("\t<a href=\"#" + chapterCount + "\">" + chapterCount + "</a> ");
                }
                pwBible.println("\t<table class=\"bible\">");

                // read the first line of the two versions and assume there are more lines
                boolean finished = false;
                jndLine = brJND.readLine();
                kjvLine = brKJV.readLine();

                // if there are no lines log error
                if (jndLine == null) {
                    System.out.println("\n!!! No JND lines found");
                }
                if (kjvLine == null) {
                    System.out.println("\n!!! No kjv lines found");
                }
                // while there are more lines
                while (!finished) {

                    if ((jndLine != null) && (kjvLine != null)) {
                        // if it is a chapter heading
                        if (jndLine.indexOf("{") == 0) {
                            chapter = jndLine.substring(1, jndLine.length() - 1);

                            // for each chapter output .
                            System.out.print(".");

                            // if including synopsis
                            if (cfg.isSynopsis()) {
                                synopsisLink = synopsisPages.get(bookNumber + "/" + chapter);
                                if (synopsisLink == null) {
                                    System.out.print(" No synopsis link for " + nextBook.getName() + " chapter " + chapter + " ");
                                    synopsisLink = "";
                                }
                            } else {
                                // if not including synopsis
                                synopsisLink = "";
                            }
                            // add the chapter title row for the html output
                            bufferString = String.format("\t\t<tr>\n\t\t\t<td colspan=\"3\" class=\"chapterTitle\"><a name=%s>%s %s</a></td>\n\t\t</tr>\n", chapter, nextBook.getName(), chapter);
                            // add the translation and synopsis for the html output
                            bufferString += String.format("\t\t<tr>\n\t\t\t<td></td>\n\t\t\t<td><strong>Darby Translation (1889)</strong> %s</td>\n\t\t\t<td><strong>Authorised (King James) Version (1796)</strong></td>\n\t\t</tr>", synopsisLink);
                            // add the text for the text output
                            bufferTxt = String.format("{%s}", chapter);
                        } else {
                            // if it is a verse

                            // get the verse number and content
                            verseNum = jndLine.substring(0, jndLine.indexOf(" "));
                            jndVerse = jndLine.substring(jndLine.indexOf(" ")).trim();
                            kjvVerse = kjvLine.substring(kjvLine.indexOf(" ")).trim();

                            // create the html output
                            bufferString = String.format("\t\t<tr>\n\t\t\t<td><a name=%s:%s>%s</a></td>\n", chapter, verseNum, verseNum);
                            bufferString += String.format("\t\t\t<td>%s</td>\n\t\t\t<td>%s</td>\n\t\t</tr>", jndVerse, kjvVerse);

                            // create the text output
                            bufferTxt = verseNum + " " + jndVerse + " " + kjvVerse;

//                        // legacy logging of short verses to find paragraph problems
//                        if (jndVerse.length() < 5) {
//                            System.out.println("Short verse: " + bufferString);
//                        }

                            // insert italics
                            while (bufferString.indexOf("*") != -1) {
                                if (startedItalic) {
                                    bufferString = bufferString.substring(0, bufferString.indexOf("*")) + "</i>" + bufferString.substring(bufferString.indexOf("*") + 1);
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
                    } else {
                        finished = true;
                    }
                }

                // write the end of the html document
                pwBible.println("</table>\n</body>\n\n</html>");

                // close the print writers
                pwBible.close();
                pwBibleTxt.close();

                // close the readers
                brJND.close();
                brKJV.close();

                // log finished book
                System.out.println(" *Done");

            }
        } catch (Exception e) {
            System.out.println("!*** Problem formatting bible ***!");
            System.out.println(e.getMessage());
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
                    String key = String.format("%s/%s", synopsisNumbers[0], synopsisNumbers[1]);
                    String value = String.format(" - <a href=\"../jnd/jnd%s.htm#%s\">go to synopsis</a>", synopsisNumbers[2], synopsisNumbers[3]);
                    synopsisMap.put(key, value);
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
                    System.out.println(ex.getMessage());
                }
            }
        }

        return synopsisMap;
    }
}
