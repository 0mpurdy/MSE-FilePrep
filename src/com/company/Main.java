package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {

    static String[] deleteChars = {"?","\"","!",",","",".","-","\'",":"};
    static String[] uncommonDeleteChars = {"1","2","3","4","5","6","7","8","9","0",";","@",")","(","¦","*","[","]","\u00AC","{","}","\u2019", "~",
            "\u201D","°","…","†","&","`","$","§","|","\t","=","+","‘","€","/","¶","_","–","½","£","“","%","#"};
    // 00AC = ¬
    // 2019 = ’
    // 201D = ”
    // 002D = –
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Config cfg = new Config();

        Scanner sc = new Scanner(System.in);

        System.out.println("MSE File Prep console application");
        System.out.println("Version: " + cfg.getMseVersion());

        int mainMenuChoice = -1;

        while (mainMenuChoice != 0) {
            printMainMenu();

            int authorChoice;
            mainMenuChoice = sc.nextInt();
            sc.nextLine();

            switch (mainMenuChoice) {
                case 0:
                    System.out.println("Closing ...");
                    break;
                case 1:
                    System.out.println();
                    prepareBibleHtml(cfg);
                    System.out.println();
                    prepareHymnsHtml(cfg);
                    for (Author nextAuthor : Author.values()) {
                        if (nextAuthor != Author.TUNES) {
                            prepareMinistry(cfg, nextAuthor);
                        }
                    }
                    break;
                case 2:
                    System.out.println("\nWhich author do you wish to prepare?");
                    printAuthorMenu();
                    authorChoice = sc.nextInt();
                    sc.nextLine();

                    if (authorChoice == 0) {
                        prepareBibleHtml(cfg);
                    } else if (authorChoice == 1) {
                        prepareHymnsHtml(cfg);
                    } else if ((authorChoice >= 3) && (authorChoice <= 12)) {
                        prepareMinistry(cfg, Author.values()[authorChoice]);
                    } else {
                        System.out.println("\nOption " + authorChoice + " is not available at the moment");
                    }
                    break;
                case 3:
                    for (Author nextAuthor : Author.values()) {
                        if (nextAuthor != Author.TUNES) {
                            writeIndex(cfg, nextAuthor);
                        }
                    }
                    break;
                case 4:
                    System.out.println("\nWhich author do you wish to prepare?");
                    printAuthorMenu();
                    authorChoice = sc.nextInt();
                    sc.nextLine();
                    if ((authorChoice>=0) && (authorChoice < Author.values().length)){
                        writeIndex(cfg, Author.values()[authorChoice]);
                    } else {
                        System.out.println("This is not a valid option");
                    }
                    break;
                default:
                    System.out.println("Invalid choice");
            }
        }

    }

    private static void printMainMenu() {

        System.out.println("\nOptions: ");

        ArrayList<String> options = new ArrayList<>();
        options.add("Close");
        options.add("Prepare all files");
        options.add("Prepare single author");
        options.add("Create all indexes");
        options.add("Create single author index");

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

        System.out.println("Preparing Bible");

        StringBuilder messages = new StringBuilder();

        try {

            // create file for outputting where the files are being read/written
            File f;

            // get the paths for the files that are used in preparing the bible html
            String jndBiblePath = cfg.getPrepareDir() + "source" + File.separator + "bible" + File.separator;
            f = new File(jndBiblePath);
            System.out.print("Reading JND bible from " + f.getCanonicalPath());

            String kjvBiblePath = cfg.getPrepareDir() + "source" + File.separator + "kjv" + File.separator;
            f = new File(kjvBiblePath);
            System.out.print("\rReading KJV bible from " + f.getCanonicalPath());

            // get the file paths that the bible html and text will be written to
            String bibleDestinationPath = cfg.getPrepareDir() + "target" + File.separator + "bible" + File.separator;
            f = new File(bibleDestinationPath);
            System.out.print("\rWriting bible HTML to " + f.getCanonicalPath());

            String bibleTxtDestinationPath = cfg.getPrepareDir() + "target" + File.separator + "bibleText" + File.separator;
            f = new File(bibleTxtDestinationPath);
            System.out.print("\rWriting bible text to " + f.getCanonicalPath());

            String jndLine; //line read from JND file
            String kjvLine; //line read from KJV file
            String chapter = "1";
            String synopsisLink;
            String verseNum;
            String jndVerse;
            String kjvVerse;
            String bufferString;
            String bufferTxt;
            boolean startedItalic = false;

            // get the synopsis pages map
            HashMap<String, String> synopsisPages = getSynopsisPages(jndBiblePath + "pages.txt");

            int bookNumber = 0;
            // for each book in the bible
            for (BibleBook nextBook : BibleBook.values()) {
                System.out.print("\rPreparing " + nextBook.getName());
                bookNumber++;
                // create buffered readers to read the jnd and kjv txt files
                BufferedReader brJND = new BufferedReader(new FileReader(jndBiblePath + "bible" + bookNumber + ".txt"));
                BufferedReader brKJV = new BufferedReader(new FileReader(kjvBiblePath + "kjv" + bookNumber + ".txt"));

                // create print writers to write the bible html and txt (overwrite any existing files)
                PrintWriter pwBible = new PrintWriter(new FileWriter(bibleDestinationPath + nextBook.getName() + ".htm", false));
                PrintWriter pwBibleTxt = new PrintWriter(new FileWriter(bibleTxtDestinationPath + "bible" + bookNumber + ": " + nextBook.getName() + ".doc", false));

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
                    System.out.print(String.format("\rPreparing %s chapter %d", nextBook.getName(), chapterCount));
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
                                    messages.append("\n\tNo synopsis link for " + nextBook.getName() + " chapter " + chapter);
                                    synopsisLink = "";
                                }
                            } else {
                                // if not including synopsis
                                synopsisLink = "";
                            }
                            // add the chapter title row for the html output
                            bufferString = String.format("\n\t</table>\n\t<table class=\"bible\">\n\t\t<tr>\n\t\t\t<td colspan=\"3\" class=\"chapterTitle\"><a name=%s>%s %s</a></td>\n\t\t</tr>\n", chapter, nextBook.getName(), chapter);
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
                            bufferString = "\t\t<tr";

                            // verse is odd make the class of <td> odd
                            if ((Integer.parseInt(verseNum) % 2) != 0) {
                                bufferString += " class=\"odd\"";
                            }

                            bufferString += String.format(">\n\t\t\t<td><a name=%s:%s>%s</a></td>\n", chapter, verseNum, verseNum);
                            bufferString += String.format("\t\t\t<td>%s</td>\n\t\t\t<td>%s</td>\n\t\t</tr>", jndVerse, kjvVerse);

                            // create the text output
                            bufferTxt = verseNum + " " + jndVerse + " " + kjvVerse;

//                            // legacy logging of short verses to find paragraph problems
//                            if (jndVerse.length() < 5) {
//                                System.out.println("Short verse: " + bufferString);
//                            }

                            // insert italics
                            while (bufferString.contains("*")) {
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
                            messages.append("Italics - " + bufferTxt);
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
                System.out.print(" *Done");

            }
        } catch (Exception e) {
            System.out.println("!*** Problem formatting bible ***!");
            System.out.println(e.getMessage());
        }

        if (!messages.equals("")) {
            messages.replace(0,1,"");
            System.out.println("\r" + messages);
        }

        System.out.println("Finished Preparing Bible");

    }

    private static void prepareHymnsHtml(Config cfg) {

        System.out.println("Preparing Hymns");

        try {

            File f;

            // the path of the input
            String hymnsPath = cfg.getPrepareDir() + File.separator + "source" + File.separator + "hymns" + File.separator;
            f = new File(hymnsPath);
            System.out.print("\tReading Hymns from: " + f.getCanonicalPath());

            // the path of the output
            String hymnsOutPath = cfg.getPrepareDir() + File.separator + "target" + File.separator + "hymns" + File.separator;
            f = new File(hymnsOutPath);
            System.out.print("\tWriting Hymns to: " + f.getCanonicalPath());

            // set up buffers
            String hymnLine;
            String hymnNumber = "0";
            String verseNumber;
            String bufferOutHtml;
            String bufferOutTxt;

            // prepare html for each hymn book
            for (HymnBook nextHymnBook : HymnBook.values()) {

                System.out.print("\r\tPreparing " + nextHymnBook.getName() + " ");
                String inputFileName = hymnsPath + nextHymnBook.getInputFilename();

                // make the reader and writer
                BufferedReader brHymns = new BufferedReader(new FileReader(inputFileName));
                PrintWriter pwHymns = new PrintWriter(new FileWriter(hymnsOutPath + nextHymnBook.getOutputFilename()));

                // read the first line of the hymn book
                hymnLine = brHymns.readLine();

                // print the html header
                pwHymns.println("<html>");
                pwHymns.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"mseStyle.css\">\n");
                pwHymns.println("<head>\n\t<title>"
                        + nextHymnBook.getName()
                        + "</title>\n</head>\n\n<body>");

                // read the second line of the hymn book
                hymnLine = brHymns.readLine();

                // if there are still more lines
                while (hymnLine != null) {

                    // if it is a new hymn
                    if (hymnLine.indexOf("{") == 0) {

                        // get the hymn number
                        hymnNumber = hymnLine.substring(1, hymnLine.length() - 1);

                        if ((Integer.parseInt(hymnNumber) % 10) == 1) {
                            System.out.print(".");
                        }

                        // if it's not the first hymn end the last table
                        if (!hymnNumber.equals("1")) {
                            bufferOutHtml = "\t\t\t</td>\n\t\t</tr>\n\t</table>\n\t<hr>\n";
                        } else {
                            bufferOutHtml = "";
                        }

                        // add the hymn number
                        bufferOutHtml += String.format("\t<a name=%s:0></a>\n\t<table class=\"hymn-table\">\n\t\t<tr>\n\t\t\t<td class=\"hymn-number\">\n\t\t\t\t<a name=%s></a>%s\n\t\t\t</td>", hymnNumber, hymnNumber, hymnNumber);
                        hymnLine = brHymns.readLine();


                        String authorEtc = "";
                        String metre = "";

                        // split the line by the comma and extract the info
                        String[] info = hymnLine.split(",");
                        if (info.length > 0) {
                            authorEtc = info[0];
                            if (info.length > 1) {
                                metre = info[1].substring(1);
                            }
                        }

                        // add the author and metre info
                        bufferOutHtml += String.format("\n\t\t\t<td class=\"authoretc\">%s</td>\n\t\t\t<td class=\"metre\">\n\t\t\t\t<a href=\"..\\tunes\\tunes.htm#%s\">%s</a>", authorEtc, metre, metre);

                    } else if (hymnLine.indexOf("|") == 0) {
                        // if it is a new verse

                        // get the verse number
                        verseNumber = hymnLine.substring(1, hymnLine.length() - 1);

                        // add the verse number html
                        bufferOutHtml = String.format("\t\t\t</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<td class=\"verse-number\">\n\t\t\t\t<a name=%s:%s></a>%s\n\t\t\t</td>\n\t\t\t<td>", hymnNumber, verseNumber, verseNumber);
                    } else {
                        // if it is a verse line
                        bufferOutHtml = "\t\t\t\t" + hymnLine + "<br>";
                    }

                    // write the next line of html
                    pwHymns.println(bufferOutHtml);

                    // read the next line of the hymn
                    hymnLine = brHymns.readLine();

                }

                pwHymns.println("</body>\n\n</html>");

                // close the reader and writer
                brHymns.close();
                pwHymns.close();

                System.out.print(" *Done");
            }

        } catch (IOException ioe) {
            System.out.println("!*** Error preparing hymns ***!");
            System.out.println(ioe.getMessage());
        }

        System.out.println("\rFinished preparing Hymns");
    }

    private static void prepareMinistry(Config cfg, Author author) {

        System.out.println("\nPreparing: " + author.getName());

        try {

            // set up readers/writers
            File f;
            String volPath = cfg.getPrepareDir()+ File.separator + "source" + File.separator + author.getFolder()  + File.separator;
            f = new File(volPath);
            f.mkdirs();
            System.out.print("\r\tReading from " + f.getCanonicalPath());
            String volDestPath = cfg.getPrepareDir() + File.separator + "target" + File.separator + author.getFolder() + File.separator;
            f = new File(volDestPath);
            f.mkdirs();
            System.out.print("\r\tWriting to " + f.getCanonicalPath());

            BufferedReader brLog;
            PrintWriter pwLog;

            String line;
            int lineCount = 0;
            int volumeNumber = 1;
            int section = 1;
            String footnotes = "";
            String actualFootnotes = "";
            int footnotesNumber = 0;
            int actualFootnotesNumber = 0;
            int maxFootnotesNumber = 0;

            // css class
            String cssClass = "";

            boolean finishedVolumes = false;
            boolean startedItalics = false;

            String messages = "";

            // for each volume
            while (!finishedVolumes) {
                try {

                    File volumeFile = new File(volPath + author.getFolder() + volumeNumber + ".txt");
                    if (volumeFile.exists()) {
                        int pageNumber = 0;
                        int keepPageNumber = 0;


                        // print out progress for each volume
                        System.out.print("\r\tVolume: " + volumeNumber);

                        brLog = new BufferedReader(new FileReader(volumeFile));
                        pwLog = new PrintWriter(new FileWriter(volDestPath + author.getCode() + volumeNumber + ".htm"));

                        // write html head
                        pwLog.println(String.format("<!DOCTYPE html>\n<html>\n\n<head>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"mseStyle.css\">\n\t<title>%s Volume %d</title>\n</head>\n\n<body>", author.getName(), volumeNumber));

                        // while there are still more lines
                        while ((line = brLog.readLine()) != null) {
                            StringBuilder outputLine = new StringBuilder(line);

                            if ((author == Author.Misc) && (volumeNumber == 26) && ((lineCount % 10) == 0)) {
                                int debug = 5;
                            }

                            if (outputLine.length() < 1) outputLine.append("<hr/>");

                            // probable heading
                            if ((outputLine.length() < 400) && (outputLine.charAt(0) != '{')) {

                                // if the line is all uppercase
                                String uppercaseLine = outputLine.toString().toUpperCase();
                                if ((uppercaseLine.equals(outputLine.toString()) && (outputLine.charAt(0) != ' '))) {
                                    cssClass = "heading";
                                }
                            }

                            int charsInSection = 0;
                            int charPosition = 0;

                            if ((outputLine.charAt(0) != '\u00a6') && (outputLine.charAt(0) != '{') && (actualFootnotesNumber != 0)) {
                                // if it is a footnote (broken bar)
                                String error = "Footnote not at foot of page " + author.getName() + " volume " + volumeNumber + " page " + pageNumber;
                                if (!messages.contains(error)) {
                                    messages += "\n\t" + error;
                                }
                            }

                            // for each character in the line
                            while ((charPosition < outputLine.length()) && (cssClass == "")) {
                                char currentCharacter = outputLine.charAt(charPosition);

                                // add italics
                                if (currentCharacter == '*') {
                                    if (startedItalics) {
                                        outputLine.replace(charPosition, charPosition + 1, "</i>");
                                    } else {
                                        outputLine.replace(charPosition, charPosition + 1, "<i>");
                                    }
                                    startedItalics = !startedItalics;
                                } else if (currentCharacter == '{') {
                                    // start of page number

                                    int tempCharPosition = charPosition + 1;
                                    while (outputLine.charAt(tempCharPosition) != '}') {
                                        tempCharPosition++;
                                    }

                                    // get the page number string
                                    String pageNumberTemp = outputLine.substring(charPosition + 1, tempCharPosition);

                                    if (pageNumberTemp.charAt(0) != '#') {
                                        // if it is a valid new page

                                        pageNumber = Integer.parseInt(pageNumberTemp);
                                        if ((pageNumber != keepPageNumber + 1) && (keepPageNumber != 0)) {
                                            messages += "\n\tMissing page: " + (keepPageNumber + 1) + " volume " + volumeNumber;
                                        }
                                        keepPageNumber = pageNumber;
                                        outputLine.replace(charPosition, outputLine.length(), String.format("<a name=%d>[Page %s]</a>", pageNumber, pageNumber));

                                        // set char position to past the end of the line
                                        charPosition = outputLine.length();

                                        // reset page specific values
                                        footnotes = "";
                                        actualFootnotes = "";
                                        footnotesNumber = 0;
                                        actualFootnotesNumber = 0;
                                        section = 1;

                                        // set the css class
                                        cssClass = "page-number";

                                    } else {
                                        // remove decoration {#x} from volume title "x"
                                        outputLine.replace(charPosition, charPosition + 2, "");
                                        outputLine.replace(outputLine.length() - 1, outputLine.length(), "");
                                        charPosition = outputLine.length();

                                        // set the css class
                                        cssClass = "volume-title";
                                    }
                                    // end page number
                                } else if (currentCharacter == '~') {
                                    // footnote
                                    outputLine.replace(charPosition, charPosition + 1, String.format("<i>see <a href=\"%s_footnotes.htm#%d:%d\">footnote</a></i>", author.getFolder(), volumeNumber, pageNumber));

                                    // increase character position by number of characters added (minus for testing)
                                    charPosition += 50 + author.getFolder().length() + Integer.toString(volumeNumber).length() + Integer.toString(pageNumber).length() - 5;
                                } else if (currentCharacter == '\u00AC') {
                                    //possessive apostrophe (not character)

                                    outputLine.replace(charPosition, charPosition + 1, "'");
                                } else if ((currentCharacter == '.') || (currentCharacter == '?') || (currentCharacter == '!')) {
                                    // end of sentence

                                    if (charsInSection > 1) {
                                        outputLine.insert(charPosition + 1, String.format("<a name=%d:%d></a>", pageNumber, section));
                                        section++;
                                    }
                                    charsInSection = 0;
                                } else if (currentCharacter == '\u2022') {
                                    // bullet character

                                    outputLine.replace(charPosition, charPosition + 1, ".");
                                } else if (currentCharacter == '\u00A6') {
                                    // footnote (broken bar character

                                    if (charPosition == 0) {
                                        if (actualFootnotesNumber == 0) {
                                            footnotes = "";
                                        }
                                        actualFootnotesNumber++;
                                        actualFootnotes += "+";
                                        outputLine.replace(charPosition, charPosition + 1, String.format("<a class=\"footnote\" name=\"#%d:f%d\"><sup>%s</sup></a>", pageNumber, actualFootnotesNumber, actualFootnotes));

                                        // set css class
                                        cssClass = "footnote";

                                    } else {
                                        footnotesNumber++;
                                        if (footnotesNumber > maxFootnotesNumber) {
                                            maxFootnotesNumber = footnotesNumber;
                                        }
                                        footnotes += "+";
                                        outputLine.replace(charPosition, charPosition + 1, String.format("<a href=\"#%d:f%d\"><sup class=\"footnote-link\">%s</sup></a>", pageNumber, footnotesNumber, footnotes));
                                    }
                                } else if (currentCharacter == '@') {
                                    // start of a scripture reference

                                    // find book name
                                    String bookName = "";
                                    int tempCharPos = charPosition + 1;


                                    //mjp? do you need length<4?
                                    while (((tempCharPos - charPosition) < 4) || (!Character.isDigit(outputLine.charAt(tempCharPos)))) {
                                        tempCharPos++;
                                    }
                                    bookName = outputLine.substring(charPosition + 1, tempCharPos);

                                    // get chapter
                                    String chapter = "";
                                    boolean finishedDoing = false;
                                    while (!finishedDoing) {
                                        if (tempCharPos >= outputLine.length()) {
                                            finishedDoing = true;
                                        } else if (Character.isDigit(outputLine.charAt(tempCharPos))) {
                                            chapter += outputLine.charAt(tempCharPos);
                                            tempCharPos++;
                                        } else {
                                            finishedDoing = true;
                                        }
                                    }

                                    //skip white space
                                    finishedDoing = false;
                                    while (!finishedDoing) {
                                        if (tempCharPos >= outputLine.length()) {
                                            finishedDoing = true;
                                        } else if (outputLine.charAt(tempCharPos) == ' ') {
                                            tempCharPos++;
                                        } else {
                                            finishedDoing = true;
                                        }
                                    }

                                    // find verse
                                    String verse = "";
                                    if (tempCharPos < outputLine.length()) {
                                        if (outputLine.charAt(tempCharPos) == ':') {
                                            tempCharPos++;

                                            //skip white space
                                            finishedDoing = false;
                                            while (!finishedDoing) {
                                                if (tempCharPos >= outputLine.length()) {
                                                    finishedDoing = true;
                                                } else if (outputLine.charAt(tempCharPos) == ' ') {
                                                    tempCharPos++;
                                                } else {
                                                    finishedDoing = true;
                                                }
                                            }

                                            // populate verse string
                                            finishedDoing = false;
                                            while (!finishedDoing) {
                                                if (tempCharPos >= outputLine.length()) {
                                                    finishedDoing = true;
                                                } else if (Character.isDigit(outputLine.charAt(tempCharPos))) {
                                                    verse += outputLine.charAt(tempCharPos);
                                                    tempCharPos++;
                                                } else {
                                                    finishedDoing = true;
                                                }
                                            }
                                        }
                                    } // end finding verse

                                    String reference;
                                    if (verse.length() > 0) {
                                        // if the reference has a verse
                                        reference = String.format("<a href=\"../bible/%s.htm#%s:%s\">%s %s:%s</a>", bookName.replaceAll("\\s", ""), chapter, verse, bookName, chapter, verse);
                                    } else {
                                        reference = String.format("<a href=\"../bible/%s.htm#%s\">%s %s</a>", bookName.replaceAll("\\s", ""), chapter, bookName, chapter);
                                    }
                                    outputLine.replace(charPosition, tempCharPos, reference);
                                    charPosition += reference.length() - 1;

                                    // end scripture reference
                                } else if (currentCharacter == '`') {
                                    // start of hymn reference

                                    // find hymn number
                                    int tempCharPos = charPosition + 1;
                                    String hymnNumber = "";

                                    boolean finishedDoing = false;
                                    while (!finishedDoing) {
                                        if (tempCharPos >= outputLine.length()) {
                                            finishedDoing = true;
                                        } else if (Character.isDigit(outputLine.charAt(tempCharPos))) {
                                            hymnNumber += outputLine.charAt(tempCharPos);
                                            tempCharPos++;
                                        } else {
                                            finishedDoing = true;
                                        }
                                    }

                                    // find verse
                                    String verseNumber = "";
                                    if (tempCharPos < outputLine.length()) {
                                        if (outputLine.charAt(tempCharPos) == ':') {
                                            tempCharPos++;
                                            finishedDoing = false;
                                            while (!finishedDoing) {
                                                if (tempCharPos >= outputLine.length()) {
                                                    finishedDoing = true;
                                                } else if (Character.isDigit(outputLine.charAt(tempCharPos))) {
                                                    verseNumber += outputLine.charAt(tempCharPos);
                                                    tempCharPos++;
                                                } else {
                                                    finishedDoing = true;
                                                }
                                            }
                                        }
                                    } // end finding verse

                                    // create the reference
                                    String reference = String.format("<a href=\"..\\hymns\\hymns1972.htm#%s:%s\">Hymn %s</a>", hymnNumber, verseNumber, hymnNumber);

                                    // insert the reference
                                    outputLine.replace(charPosition, tempCharPos, reference);

                                    // move the char position forward
                                    charPosition += reference.length() - charPosition;

                                    // end hymn reference
                                }
                                charPosition++;
                                charsInSection++;
                            } // end of processing line

                            // add formatting to the line
                            if (cssClass == "") {
                                cssClass = "paragraph";
                            }
                            outputLine.insert(0, "\t<div class=\"" + cssClass +"\">\n\t\t");
                            outputLine.append("\n\t</div>");

                            // reset css class
                            cssClass = "";

                            // output the "outputLine"
                            pwLog.println(outputLine.toString());

                            lineCount++;
                        } // end of processing lines

                        pwLog.println("\n</body>\n\n</html>");
                        pwLog.close();
                        brLog.close();

                        volumeNumber++;

                    } else {
                        finishedVolumes = true;
                        if (!messages.equals("")) {
                            messages = messages.replaceFirst("\n", "");
                            System.out.print("\r" + messages);
                        }
                        System.out.println("\rFinished preparing " + author.getName());
                    }
                } catch (IOException ioe) {
                    System.out.println("\n!*** Error with " + author.getName() + " volume: " + volumeNumber);
                    System.out.println(ioe.getMessage());
                    volumeNumber++;
                }
            }
        } catch (IOException ioe) {
            System.out.println("\n!*** Error preparing " + author.getName() + " ***!");
            System.out.println(ioe.getMessage());
        }
    } // end prepare (used for ministry)

    private static void writeIndex(Config cfg, Author author) {

        HashMap<String, Integer> tokenCountMap = new HashMap<>();

        // source file path
        String sourcePath = cfg.getPrepareDir();

        if (author == Author.BIBLE) {
            sourcePath += "target" + File.separator + "bibleText" + File.separator;
        } else {
            sourcePath += "source" + File.separator + author.getFolder() + File.separator;
        }

        // destination file path
        String destinationPath = cfg.getPrepareDir() + File.separator + "target" + File.separator + author.getFolder() + File.separator;

        int volumeNumber = 1;
        boolean finished = false;

        // for all the volumes
        while (!finished) {
            File inputVolume = new File(sourcePath + author.getCode() + volumeNumber + ".txt");
            if (!inputVolume.exists()) {
                finished = true;
                break;
            } else {
                analyseVolume(inputVolume, author, volumeNumber, tokenCountMap);
            }
            volumeNumber++;
        }


        // write output
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(tokenCountMap);
            File outputFile = new File(destinationPath + File.separator + author.getFolder() + ".idx");
            PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
            pw.println(json);
        } catch (IOException ioe) {
            System.out.println("\nError printing index for " + author.getName());
            System.out.println(ioe.getMessage());
        }
        System.out.println();
    } // writeIndex

    private static void analyseVolume(File inputVolume, Author author, int volumeNumber, HashMap<String, Integer> tokenCountMap) {

        System.out.print("\rAnalysing " + author.getName() + " volume " + volumeNumber);

        HashMap<String, Integer> tokenMap = new HashMap<>();

        int pageNumber = 0;

        try {

            // line read
            String line;
            boolean noErrors = true;

            if (volumeNumber == 26) {
                int debug = 1;
            }

            // create reader
            FileReader inputReader = new FileReader(inputVolume);
            BufferedReader brLog = new BufferedReader(inputReader);

            // while there are more lines
            while ((line = brLog.readLine()) != null) {

                StringBuilder outputLine = new StringBuilder(line);
                int charPos = 0;
                boolean skip = false;

                if (outputLine.length() > 0) {
                    if (outputLine.charAt(charPos) == '{') {
                        // if it is a page number or a title

                        charPos++;
                        StringBuilder specialLine = new StringBuilder();

                        // find the end of the page number
                        while (outputLine.charAt(charPos) != '}') {
                            specialLine.append(outputLine.charAt(charPos));
                            charPos++;
                        }

                        if (specialLine.charAt(0) != '#') {
                            // page number
                            pageNumber = Integer.parseInt(specialLine.toString());
                            skip = true;
                        } else {
                            outputLine = new StringBuilder(specialLine.substring(1));
                        }

                    }

                    // if the line contains html
                    while (outputLine.indexOf("<") != -1) {
                        charPos = 0;
                        while ((charPos < outputLine.length()) && (outputLine.charAt(charPos) != '<')) {
                            charPos++;
                        }
                        int tempCharPos = charPos + 1;
                        while ((tempCharPos < outputLine.length()) && (outputLine.charAt(tempCharPos) != '>')) {
                            tempCharPos++;
                        }
                        if ((charPos < outputLine.length()) && (tempCharPos <= outputLine.length()))
                            outputLine.replace(charPos, tempCharPos + 1, "");
                    }

                    if (!skip) {
                        // split the line into tokens by " " characters
                        String[] tokens = outputLine.toString().split(" ");

                        // make each token into a word that can be searched
                        for (String token : tokens) {
                            token = token.toUpperCase();
                            if (!isAlpha(token)) {
                                token = processString(token);
                            }
                            if (!isAlpha(token)) {
                                token = processUncommonString(token);
                                if (!isAlpha(token)) {
                                    if (noErrors) {
                                        noErrors = !noErrors;
                                        System.out.println();
                                    }
                                    System.out.print("\t" + token + "\t" + volumeNumber + ":" + pageNumber);
                                    token = "";
                                }
                            }

                            if (!token.equals("")) {
                                // if the string isn't empty

                                Integer tokenCount = tokenCountMap.get(token);
                                if (tokenCount != null) {
                                    // if the word has already been added
                                    tokenCount++;
                                    tokenCountMap.put(token, tokenCount);
                                } else {
                                    tokenCountMap.put(token, 1);
                                }
                            }
                        }
                    } // end if (!skip)
                }// end if outputline is !empty
            }

        } catch (FileNotFoundException fnf) {
            System.out.println("Error - could not find file " + inputVolume.getAbsolutePath());
            System.out.println(fnf.getMessage());
        } catch (IOException ioe) {
            System.out.println("Error reading " + inputVolume.getAbsolutePath());
            System.out.println(ioe.getMessage());
        }


    }

    private static boolean isAlpha(String token) {
        char[] chars = token.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    private static String processString(String token) {

        for (String c : deleteChars) {
            token = token.replace(c, "");
        }

//        boolean finished = false;
//        while ((token.contains("\'") && !finished)) {
//            // if it is the first or last character then remove it
//            if (token.indexOf('\'') == 0) {
//                token = token.substring(1);
//            } else if (token.indexOf('\'') == (token.length() - 1)) {
//                token = token.substring(0,token.length()-1);
//            } else if (!Character.isLetter(token.charAt(token.indexOf('\'') + 1))) {
//                // if it isn't followed by a letter then remove it
//                token = token.substring(0, token.indexOf('\'')) + token.substring(token.indexOf('\'')+1, token.length());
//            } else {
//                finished = true;
//            }
//        }
//
//        if (token.contains("-")) {
//            if (!Character.isLetter(token.charAt(token.indexOf('-') - 1))) {
//                // if it isn't preceded by a letter then remove it
//                token = token.substring(0, token.indexOf('-')) + token.substring(token.indexOf('-')+1, token.length());
//            }
//        }

        return token;
    }

    private static String processUncommonString(String token) {
        for (String c : uncommonDeleteChars) {
            token = token.replace(c, "");
        }
        return token;
    }

    private static HashMap<String, String> getSynopsisPages(String filename) {
        // this gets a map of the corresponding synopsis page in JND's ministry for each book of the bible

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
            System.out.println("!***Error creating synopsis hash map***!");
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
