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
                System.out.println();
                prepareBibleHtml(cfg);
                System.out.println();
                prepareHymnsHtml(cfg);
                break;
            case 1:
                System.out.println("\nWhich author do you wish to prepare?");
                printAuthorMenu();
                int authorChoice = sc.nextInt();
                sc.nextLine();

                switch (authorChoice) {
                    case 0:
                        prepareBibleHtml(cfg);
                        break;
                    case 1:
                        prepareHymnsHtml(cfg);
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

        System.out.println("Preparing Bible");

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
                            bufferString = String.format("\t\t<tr");

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

    private static void prepareHymnsHtml(Config cfg) {

        System.out.println("Preparing Hymns");

        try {

            File f;

            // the path of the input
            String hymnsPath = cfg.getPrepareDir() + File.separator + "hymns" + File.separator + "best" + File.separator;
            f = new File(hymnsPath);
            System.out.println("Reading Hymns from: " + f.getCanonicalPath());

            // the path of the output
            String hymnsOutPath = cfg.getPrepareDir()+ File.separator + "target" + File.separator + "hymns" + File.separator;
            f = new File(hymnsOutPath);
            System.out.println("Writing Hymns to: " + f.getCanonicalPath());

            // set up buffers
            String hymnLine;
            String hymnNumber = "0";
            String verseNumber;
            String bufferOutHtml;
            String bufferOutTxt;

            // prepare html for each hymn book
            for (HymnBook nextHymnBook : HymnBook.values()) {

                System.out.print("Preparing " + nextHymnBook.getName() + " ");
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
                        if (info.length >0) {
                            authorEtc = info[0];
                            if (info.length >1) {
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

                System.out.println("Finished " + nextHymnBook.getName());
            }

        } catch (IOException ioe) {
            System.out.println("!*** Error preparing hymns ***!");
            System.out.println(ioe.getMessage());
        }

    }

    private static void prepareMinistry(Config cfg, Author author) {
        
    }

//    private void prepare(String strAuth) {
//        PrintWriter pwLog = null;
//        BufferedReader brLog = null;
//        StringBuffer sbLine = new StringBuffer();
//        StringBuffer sbPageNum = new StringBuffer();
//        int intVol = 0;
//        int intSection = 1;
//
//        // MJP 2x 15/3/15
//        String strVolsPath = Constants.BASIC_FILE_PATH + "src" + File.separator + strAuth + File.separator + "best" + File.separator;
//        String strVolsDestPath = Constants.BASIC_FILE_PATH + "target" + File.separator + strAuth + File.separator;
//
//        try {
//            String strInfo; //line read from file
//            int intFootnotes = 0;
//            int intActualFootnotes = 0;
//            int intMaxFootnotes = 0;
//            String strFootnotes = "";
//            String strActualFootnotes = "";
//            int intLineCount = 0;
//            int intCharPos = 0;
//            int intTempCharPos = 0;
//            int intEndRef = 0;
//            int intBookNamePos = 0;
//            StringBuffer sbBookName = new StringBuffer();
//            StringBuffer sbChapter = new StringBuffer();
//            StringBuffer sbHymn = new StringBuffer();
//            StringBuffer sbVerse = new StringBuffer();
//            boolean bStartedItalic = false;
//            String strBookName;
//            String strCompressedBookName = "";
//            String strReference;
//            String strUCLine;
//            boolean finished;
//            boolean bFinishedVols = false;
//            int intPageNum;
//            int intKeepPageNum;
//
//            intVol = 1;
//            if (strAuth.equals("hymns")) {
//                intVol = Constants.LAST_HYMNBOOK + 1;
//            }
//            while (!bFinishedVols) {
//
//                File fVolTxt = new File(strVolsPath + strAuth + intVol + ".doc");
//                if (!fVolTxt.exists()) {
//                    bFinishedVols = true;
//                } else if (taFilename.getText().equals("") || taFilename.getText().equals("" + intVol)) {
//                    intPageNum = 0;
//                    intKeepPageNum = 0;
//                    taOutput.append(NEW_LINE + "*** Preparing " + strAuth + intVol + ".htm ***");
//                    tfProgress.setText(strAuth + intVol);
//                    brLog = new BufferedReader(new FileReader(strVolsPath + strAuth + intVol + ".doc"));
//                    pwLog = new PrintWriter(new FileWriter(strVolsDestPath + strAuth + intVol + ".htm", false));
//                    pwLog.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"><style>a:hover{background-color:yellow;}</style>");
//                    pwLog.println("<title>" + strAuth.toUpperCase() + " Volume " + intVol + "</title></head><body bgcolor=\"#FFFFFF\" link=\"#0000FF\" vlink=\"#0000FF\" alink=\"#0000FF\">");
//                    while ((strInfo = brLog.readLine()) != null) {//still more lines
//                        sbLine.replace(0, sbLine.length(), strInfo);
//
//                        if ((sbLine.length() < 400) && (sbLine.charAt(0) != '{')) {//probable heading
//                            strUCLine = sbLine.toString().toUpperCase();
//                            if (strUCLine.equals(sbLine.toString()) && (sbLine.charAt(0) != ' ')) {
//                                taOutput.append(NEW_LINE + "<a href=\"" + strAuth + intVol + ".htm#" + sbPageNum + "\"><font size=4>" + sbLine.toString() + "</font></a><br>~");
//                                sbLine.insert(0, "<h2>");
//                                sbLine.append("</h2>");
//                            } else {
//                                //                            taOutput.append(NEW_LINE + "SHORT PARA - " + sbLine.toString());
//                            }
//                        }
//
//                        int intCharsInSection = 0;
//                        intCharPos = 0;
//                        if ((sbLine.charAt(0) != '?') && (sbLine.charAt(0) != '{') && (intActualFootnotes != 0)) {
//                            taOutput.append(NEW_LINE + "FOOTNOTE not at foot of page - " + sbPageNum);
//                        }
//
//                        while (intCharPos < sbLine.length()) {
//                            char charCurr = sbLine.charAt(intCharPos);
//                            if (charCurr == '*') {//italics
//                                if (bStartedItalic) {
//                                    sbLine.replace(intCharPos, intCharPos + 1, "</i>");
//                                } else {
//                                    sbLine.replace(intCharPos, intCharPos + 1, "<i>");
//                                }
//                                bStartedItalic = !bStartedItalic;
//                            } else if (charCurr == '{') {//start page number
//                                intTempCharPos = intCharPos + 1;
////                                    sbPageNum.replace(0, sbLine.length(), "");
//                                StringBuffer sbPageNumTmp = new StringBuffer();
//                                while (sbLine.charAt(intTempCharPos) != '}') {
//                                    sbPageNumTmp.append(sbLine.charAt(intTempCharPos));
//                                    intTempCharPos++;
//                                }
//                                if (sbPageNumTmp.charAt(0) != '#') {
//                                    sbPageNum = sbPageNumTmp;
//                                    intPageNum = Integer.parseInt(sbPageNum.toString());
//                                    if (intPageNum != intKeepPageNum + 1) {
//                                        taOutput.append(NEW_LINE + "Missing page: " + (intKeepPageNum + 1));
//                                    }
//                                    intKeepPageNum = intPageNum;
//                                    sbLine.replace(intCharPos, intCharPos + 1, "<a name=" + sbPageNum + "><center>[Page ");
//                                    intCharPos = intCharPos + 23;
//
//                                    if (intMaxFootnotes != 0) {
//                                        intMaxFootnotes = 0;
//                                        pwLog.println("</font>");
//                                    }
//                                    if (intFootnotes != intActualFootnotes) {
//                                        taOutput.append(NEW_LINE + "Mismatch of actual footnotes: " + sbPageNum +
//                                                ", expected = " + intFootnotes + ", found = " + intActualFootnotes);
//                                    }
//                                    strFootnotes = "";
//                                    strActualFootnotes = "";
//                                    intFootnotes = 0;
//                                    intActualFootnotes = 0;
//                                } else {
//                                    sbLine.replace(intCharPos, intTempCharPos + 1, "");
//                                }
//                                taOutput.append(" " + sbPageNum.toString());
//                            } else if (charCurr == '}') {//end page number
//                                sbLine.replace(intCharPos, intCharPos + 1, "]</center>");
//                                intSection = 1;
//                            } else if (charCurr == '~') {//footnote
//                                sbLine.replace(intCharPos, intCharPos + 1, "<i>see <a href=\"" + strAuth + "_footnotes.htm#" + intVol + ":" + sbPageNum.toString() + "\">footnote</a></i>");
//                                intCharPos = intCharPos + 40;
//                            } else if (charCurr == '?') {//possessive apostrophe
//                                sbLine.replace(intCharPos, intCharPos + 1, "'");
//                            } else if ((charCurr == '.') || (charCurr == '?') || (charCurr == '!')) {//new sentence
//                                if (intCharsInSection > 1) {
//                                    sbLine.insert(intCharPos + 1, "<a name=" + sbPageNum.toString() + ":" + intSection + "></a>");
//                                    intSection++;
//                                }
//                                intCharsInSection = 0;
//                            } else if (charCurr == '?') {
//                                sbLine.replace(intCharPos, intCharPos + 1, ".");
//                            } else if (charCurr == '?') {//footnote
//                                if (intCharPos == 0) {
//                                    if (intActualFootnotes == 0) {
////                                        strFootnotes = "";
//                                        pwLog.println("<font size=\"-1\" color=\"#00A000\">");
//                                    }
//                                    intActualFootnotes++;
//                                    strActualFootnotes = strActualFootnotes + "+";
//                                    sbLine.replace(intCharPos, intCharPos + 1, "<a id=\"footnote\" name=\"#" + sbPageNum.toString() + ":f" + intActualFootnotes + "\"><sup>" + strActualFootnotes + "</sup></a>");
//                                } else {
//                                    intFootnotes++;
//                                    if (intFootnotes > intMaxFootnotes) {
//                                        intMaxFootnotes = intFootnotes;
//                                    }
//                                    strFootnotes = strFootnotes + "+";
//                                    sbLine.replace(intCharPos, intCharPos + 1, "<a href=\"#" + sbPageNum.toString() + ":f" + intFootnotes + "\"><font size=\"-1\" color=\"#00A000\"><sup>" + strFootnotes + "</sup></font></a>");
//                                }
//                            } else if (charCurr == '@') {//start of scripture reference
//
//                                //find book name
//                                sbBookName.replace(0, sbBookName.length(), "");
//                                intTempCharPos = intCharPos + 1;
//                                while (((intTempCharPos - intCharPos) < 4) || (!Character.isDigit(sbLine.charAt(intTempCharPos)))) {
//                                    sbBookName.append(sbLine.charAt(intTempCharPos));
//                                    intTempCharPos++;
//                                }
//
//                                //strip white space
//                                intBookNamePos = sbBookName.length() - 1;
//                                while (sbBookName.charAt(intBookNamePos) == ' ') {
//                                    sbBookName.deleteCharAt(intBookNamePos);
//                                    intBookNamePos--;
//                                }
//
//                                //find chapter
//                                sbChapter.replace(0, sbChapter.length(), "");
//                                finished = false;
//                                while (!finished) {
//                                    if (intTempCharPos >= sbLine.length()) {
//                                        finished = true;
//                                    } else if (Character.isDigit(sbLine.charAt(intTempCharPos))) {
//                                        sbChapter.append(sbLine.charAt(intTempCharPos));
//                                        intTempCharPos++;
//                                    } else {
//                                        finished = true;
//                                    }
//                                }
//                                intEndRef = intTempCharPos;
//
//                                //skip white space
//                                finished = false;
//                                while (!finished) {
//                                    if (intTempCharPos >= sbLine.length()) {
//                                        finished = true;
//                                    } else if (sbLine.charAt(intTempCharPos) == ' ') {
//                                        intTempCharPos++;
//                                    } else {
//                                        finished = true;
//                                    }
//                                }
//
//                                sbVerse.replace(0, sbVerse.length(), "");
//                                if (intTempCharPos < sbLine.length()) {
//                                    if (sbLine.charAt(intTempCharPos) == ':') {//find verse
//                                        intTempCharPos++;
//                                        //skip white space
//                                        finished = false;
//                                        while (!finished) {
//                                            if (intTempCharPos >= sbLine.length()) {
//                                                finished = true;
//                                            } else if (sbLine.charAt(intTempCharPos) == ' ') {
//                                                intTempCharPos++;
//                                            } else {
//                                                finished = true;
//                                            }
//                                        }
//
//                                        finished = false;
//                                        while (!finished) {
//                                            if (intTempCharPos >= sbLine.length()) {
//                                                finished = true;
//                                            } else if (Character.isDigit(sbLine.charAt(intTempCharPos))) {
//                                                sbVerse.append(sbLine.charAt(intTempCharPos));
//                                                intTempCharPos++;
//                                            } else {
//                                                finished = true;
//                                            }
//                                        }
//                                        intEndRef = intTempCharPos;
//                                    }
//                                }
//
//                                strBookName = sbBookName.toString();
//
//                                //strip white space from book name
//                                intBookNamePos = sbBookName.length() - 1;
//                                while (intBookNamePos >= 0) {
//                                    if (sbBookName.charAt(intBookNamePos) == ' ') {
//                                        sbBookName.deleteCharAt(intBookNamePos);
//                                    }
//                                    intBookNamePos--;
//                                }
//                                strCompressedBookName = sbBookName.toString();
//
//                                if (sbVerse.length() > 0) {
//                                    strReference = "<a href=\"../bible/" + strCompressedBookName + ".htm#" + sbChapter.toString() + ":" + sbVerse.toString() + "\">" + strBookName + " " + sbChapter.toString() + ":" + sbVerse.toString() + "</a>";
//                                } else {
//                                    strReference = "<a href=\"../bible/" + strCompressedBookName + ".htm#" + sbChapter.toString() + "\">" + strBookName + " " + sbChapter.toString() + "</a>";
//                                }
//                                sbLine.replace(intCharPos, intEndRef, strReference);
//                                intCharPos = intCharPos + strReference.length() - (intEndRef - intCharPos);
//                            } else if (charCurr == '`') {//start of hymn reference
//                                //find hymn number
//                                intTempCharPos = intCharPos + 1;
//                                sbHymn.replace(0, sbHymn.length(), "");
//                                finished = false;
//                                while (!finished) {
//                                    if (intTempCharPos >= sbLine.length()) {
//                                        finished = true;
//                                    } else if (Character.isDigit(sbLine.charAt(intTempCharPos))) {
//                                        sbHymn.append(sbLine.charAt(intTempCharPos));
//                                        intTempCharPos++;
//                                    } else {
//                                        finished = true;
//                                    }
//                                }
//                                //taOutput.append(NEW_LINE + "sbHymn:" + sbHymn.toString());
//                                sbVerse.replace(0, sbVerse.length(), "");
//                                if (intTempCharPos < sbLine.length()) {
//                                    if (sbLine.charAt(intTempCharPos) == ':') {//find verse
//                                        intTempCharPos++;
//                                        finished = false;
//                                        while (!finished) {
//                                            if (intTempCharPos >= sbLine.length()) {
//                                                finished = true;
//                                            } else if (Character.isDigit(sbLine.charAt(intTempCharPos))) {
//                                                sbVerse.append(sbLine.charAt(intTempCharPos));
//                                                intTempCharPos++;
//                                            } else {
//                                                finished = true;
//                                            }
//                                        }
//                                    }
//                                }
//                                //taOutput.append(NEW_LINE + "sbVerse:" + sbVerse.toString());
//                                if (Constants.bHymnPointers == true) {
//                                    strReference = "<a href=\"..\\hymns\\hymns1.htm#" + sbHymn.toString() + ":" + sbVerse.toString() + "\">Hymn " + sbHymn.toString() + "</a>";
//                                } else {
//                                    strReference = "Hymn " + sbHymn.toString();
//                                }
//                                sbLine.replace(intCharPos, intTempCharPos, strReference);
//                                intCharPos = intCharPos + strReference.length() - (intTempCharPos - intCharPos);
//                            }
//                            intCharPos++;
//                            intCharsInSection++;
//                        }
//                        if (sbLine.length() > 0) {
//                            sbLine.append("<p>");
//                        }
//
//                        pwLog.println(sbLine.toString());
//                        if (bStartedItalic) {
//                            taOutput.append(NEW_LINE + "ITALICS - " + sbLine.toString());
//                        }
//
//                        intLineCount = intLineCount + 1;
//                    }
//
//
//                    pwLog.println("</body></html>");
//                    taOutput.append(NEW_LINE + "*** Finished " + strAuth + intVol + ".htm ***");
//                    pwLog.close();
//                    brLog.close();
//                }
//                intVol = intVol + 1;
//            }
//            tfProgress.setText("Done");
//        } catch (Exception e) {
//            taOutput.append(NEW_LINE + "*** Problem in volume " + taFilename.getText() + " ***");
//            taOutput.append(NEW_LINE + "PageNum: " + sbPageNum.toString());
//            taOutput.append(" Section: " + intSection);
//            taOutput.append(" Line: '" + sbLine.toString() + "'");
//            taOutput.append(NEW_LINE + e);
//        }
//
//        try {
//            pwLog.close();
//            brLog.close();
//        } catch (Exception e) {
//            taOutput.append(NEW_LINE + "*** Problem in volume " + taFilename.getText() + " ***");
//            taOutput.append(NEW_LINE + "Can't close files");
//        }
//    }//prepare

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
