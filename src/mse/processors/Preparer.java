package mse.processors;

import mse.data.*;
import mse.helpers.HtmlHelper;
import mse.common.Author;
import mse.common.Config;

import java.io.*;
import java.util.HashMap;

/**
 * Created by Michael Purdy on 21/12/2015.
 *
 * This is used to prepare the html/text for indexing, reading and searching.
 * Also prepares contents pages.
 */
public class Preparer {

    // region bible

    public static void prepareBibleHtml(Config cfg, String mseStyleLocation) {

        System.out.print("\nPreparing Bible");

        StringBuilder errMessages = new StringBuilder();

        try {

            // get the paths for the files that are used in preparing the bible html
            BiblePrepareCache bpc = new BiblePrepareCache(cfg);

            // get the synopsis pages map
            bpc.synopsisPages = getSynopsisPages(bpc.getSynopsisSource());

            // for each book in the bible
            for (BibleBook nextBook : BibleBook.values()) {

                System.out.print("\rPreparing " + nextBook.getName());
                bpc.nextBook(nextBook);

                prepareSingleBibleBook(cfg, bpc, mseStyleLocation, errMessages);

            }
        } catch (Exception e) {
            System.out.println("!*** Problem formatting bible ***!");
            System.out.println(e.getMessage());
        }

        if (!errMessages.toString().equals("")) {
            System.out.println("\r" + errMessages);
        }

        System.out.println("\rFinished Preparing Bible");

    }

    public static void prepareSingleBibleBook(Config cfg, BiblePrepareCache bpc, String mseStyleLocation, StringBuilder errMessages) throws IOException {
        // create buffered readers to read the jnd and kjv txt files
        BufferedReader brJND = new BufferedReader(new FileReader(bpc.getJndSource()));
        BufferedReader brKJV = new BufferedReader(new FileReader(bpc.getKjvSource()));

        // create print writers to write the bible html and txt (overwrite any existing files)
        PrintWriter pwBible = new PrintWriter(new FileWriter(bpc.getBibleOutput(), false));
        PrintWriter pwBibleTxt = new PrintWriter(new FileWriter(bpc.getBibleTextOutput()));

        // write the html header
        HtmlHelper.writeHtmlHeader(pwBible, "Darby translation and King James Version of The Bible", mseStyleLocation);
        HtmlHelper.writeBibleStart(pwBible, pwBibleTxt, bpc.book);

        // for each chapter
        for (int chapterCount = 1; chapterCount <= bpc.book.getNumChapters(); chapterCount++) {
            pwBible.println("\t<a href=\"#" + chapterCount + "\">" + chapterCount + "</a> ");
//            System.out.print(String.format("\rPreparing %s chapter %d", bpc.book.getName(), chapterCount));
        }
        pwBible.println("\t<table class=\"bible\">");

        // read the first line of the two versions and assume there are more lines
        bpc.jndLine = brJND.readLine();
        bpc.kjvLine = brKJV.readLine();

        // if there are no lines log error
        if (bpc.jndLine == null) {
            System.out.println("\n!!! No JND lines found");
        }
        if (bpc.kjvLine == null) {
            System.out.println("\n!!! No KJV lines found");
        }

        // while there are more lines
        while ((bpc.jndLine != null) && (bpc.kjvLine != null)) {

            prepareSingleBibleLine(cfg, bpc, errMessages);
            // write the html and text output
            pwBible.println(bpc.bufferString);
            pwBibleTxt.println(bpc.bufferTxt);

            // check if italics run over a line
            if (bpc.startedItalic) {
                errMessages.append("\n\tItalics - ").append(bpc.bufferTxt);
            }

            // read the next line to process
            bpc.jndLine = brJND.readLine();
            bpc.kjvLine = brKJV.readLine();
        }

        // write the end of the html document
        pwBible.println("</table>\n</body>\n\n</html>");

        // close the print writers
        pwBible.close();
        pwBibleTxt.close();

        // close the readers
        brJND.close();
        brKJV.close();
    }

    public static void prepareSingleBibleLine(Config cfg, BiblePrepareCache bpc, StringBuilder errMessages) {

        // if it is a chapter heading
        if (bpc.jndLine.indexOf("{") == 0) {
            bpc.chapter = bpc.jndLine.substring(1, bpc.jndLine.length() - 1);

            // if including synopsis
            if (cfg.isSynopsis()) {
                bpc.synopsisLink = bpc.synopsisPages.get(bpc.bookNumber + "/" + bpc.chapter);
                if (bpc.synopsisLink == null) {
                    errMessages.append("\n\tNo synopsis link for ").append(bpc.book.getName()).append(" chapter ").append(bpc.chapter);
                    bpc.synopsisLink = "";
                }
            } else {
                // if not including synopsis
                bpc.synopsisLink = "";
            }
            // add the chapter title row for the html output
            bpc.bufferString = HtmlHelper.getBibleChapterHeader(bpc);
            // add chapter header for text output
            bpc.bufferTxt = String.format("{%s}", bpc.chapter);
        } else {
            // if it is a verse

            // get the verse number and content
            bpc.verseNum = bpc.jndLine.substring(0, bpc.jndLine.indexOf(" "));
            bpc.jndVerse = bpc.jndLine.substring(bpc.jndLine.indexOf(" ")).trim();
            bpc.kjvVerse = bpc.kjvLine.substring(bpc.kjvLine.indexOf(" ")).trim();

            // create the html output
            bpc.bufferString = "\t\t<tr";

            // verse is odd make the class of <td> odd
            if ((Integer.parseInt(bpc.verseNum) % 2) != 0) {
                bpc.bufferString += " class=\"odd\"";
            }

            bpc.bufferString += String.format(">\n\t\t\t<td><a name=%s:%s>%s</a></td>\n", bpc.chapter, bpc.verseNum, bpc.verseNum);
            bpc.bufferString += String.format("\t\t\t<td>%s</td>\n\t\t\t<td>%s</td>\n\t\t</tr>", bpc.jndVerse, bpc.kjvVerse);

            // create the text output
            bpc.bufferTxt = bpc.verseNum + " " + bpc.jndVerse + " " + bpc.kjvVerse;

//                            // legacy logging of short verses to find paragraph problems
//                            if (jndVerse.length() < 5) {
//                                System.out.println("Short verse: " + bufferString);
//                            }

            // insert italics
            while (bpc.bufferString.contains("*")) {
                if (bpc.startedItalic) {
                    bpc.bufferString = bpc.bufferString.substring(0, bpc.bufferString.indexOf("*")) + "</i>" + bpc.bufferString.substring(bpc.bufferString.indexOf("*") + 1);
                } else {
                    bpc.bufferString = bpc.bufferString.substring(0, bpc.bufferString.indexOf("*")) + "<i>" + bpc.bufferString.substring(bpc.bufferString.indexOf("*") + 1);
                }
                bpc.startedItalic = !bpc.startedItalic;
            }

        }
    }

    public static void createBibleContents(Config cfg, PreparePlatform preparePlatform) {

        System.out.print("Preparing Bible contents...");

        String contentsFilePath = cfg.getResDir() + Author.BIBLE.getTargetPath(Author.BIBLE.getContentsName());

        File bibleContentsFile = new File(contentsFilePath);

        PrintWriter pw = null;
        try {

            if (bibleContentsFile.exists() || bibleContentsFile.createNewFile()) {

                pw = new PrintWriter(bibleContentsFile);

                HtmlHelper.writeHtmlHeader(pw, "Bible Contents", preparePlatform.getStylesLink());
                pw.println("");
                pw.println("\t<div class=\"container bible-contents-table\">");
                pw.println("\t\t<div class=\"row bible-contents-header\">");
                pw.println("\t\t\t<div class=\"col-xs-6\">Old<br>Testament</div>");
                pw.println("\t\t\t<div class=\"col-xs-6\">New<br>Testament</div>");
                pw.println("\t\t</div>");

                for (int i = 0; i < BibleBook.getNumOldTestamentBooks(); i++) {

                    pw.println("\t\t<div class=\"row bible-contents-row\">\n\t\t\t<div class=\"col-xs-6\"><a href=\"" + preparePlatform.getLinkPrefix(Author.BIBLE) + BibleBook.values()[i].getName()
                            + ".htm\">" + BibleBook.values()[i].getName() + "</a></div>");

                    // if i+1 is less than the number of new testament books
                    if (i < BibleBook.getNumNewTestamentBooks()) {
                        pw.println("\t\t\t<div class=\"col-xs-6\"><a href=\"" + preparePlatform.getLinkPrefix(Author.BIBLE) + BibleBook.values()[i + BibleBook.getNumOldTestamentBooks()].getName()
                                + ".htm\">" + BibleBook.values()[i + BibleBook.getNumOldTestamentBooks()].getName() + "</a></div>");
                    } else {
                        pw.println("\t\t\t<div class=\"col-xs-6\"></div>");
                    }

                    pw.println("\t\t</div>");

                }

                pw.println("\t</div>");

                pw.println("</body>");
                pw.println();
                pw.println("</html>");

            } else {
                System.out.println("Could not find/create: " + contentsFilePath);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + contentsFilePath);
        } finally {
            if (pw != null) pw.close();
        }

        System.out.println("\rFinished preparing Bible contents");

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
                    String value = String.format(" - <a href=\"../jnd/JND%s.htm#%s\">go to synopsis</a>", synopsisNumbers[2], synopsisNumbers[3]);
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

    // endregion

    // region hymns

    public static void prepareHymnsHtml(Config cfg, String mseStyleLocation) {

        System.out.print("Preparing Hymns");

        try {

            File f;

            // the path of the input
            String hymnsPath = cfg.getResDir() + Author.HYMNS.getPreparePath();
            f = new File(hymnsPath);
            f.mkdirs();
            System.out.print("\r\tReading Hymns from: " + f.getCanonicalPath());

            // the path of the output
            String hymnsOutPath = cfg.getResDir() + Author.HYMNS.getTargetPath();
            f = new File(hymnsOutPath);
            f.mkdirs();
            System.out.print("\r\tWriting Hymns to: " + f.getCanonicalPath());

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
                HtmlHelper.writeHtmlHeader(pwHymns, nextHymnBook.getName(), mseStyleLocation);
                HtmlHelper.writeStart(pwHymns);

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

                pwHymns.println("\t\t\t</td>" +
                        "\n\t\t</tr>" +
                        "\n\t</table>" +
                        "\n\n</body>\n\n</html>");

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

    public static void createHymnsContents(Config cfg, String mseStyleLocation) {

        System.out.print("Preparing hymns contents");

        try {

            File f;

            // the path of the input
            String hymnsPath = cfg.getResDir() + Author.HYMNS.getPreparePath();
            f = new File(hymnsPath);
            f.mkdirs();
            System.out.print("\r\tReading Hymns from: " + f.getCanonicalPath());

            // the path of the output
            String hymnsOutPath = cfg.getResDir() + Author.HYMNS.getTargetPath();
            f = new File(hymnsOutPath);
            f.mkdirs();
            System.out.print("\r\tWriting Hymns to: " + f.getCanonicalPath());

            // set up buffers
            String hymnLine;
            String hymnNumber;

            PrintWriter pwOverallHymnBooksContents = new PrintWriter(new FileWriter(hymnsOutPath + "Hymns-Contents.htm"));

            // print the html header for the overall contents page
            HtmlHelper.writeHtmlHeader(pwOverallHymnBooksContents, "Hymn Contents", mseStyleLocation);
            HtmlHelper.writeStart(pwOverallHymnBooksContents);

            // prepare html for each hymn book
            for (HymnBook nextHymnBook : HymnBook.values()) {

                // create a print writer for the hymnbook
                PrintWriter pwSingleHymnBookContents = new PrintWriter(new File(hymnsOutPath + nextHymnBook.getContentsName()));

                // print the html header for the single book contents page
                HtmlHelper.writeHtmlHeader(pwSingleHymnBookContents, "Hymn Contents", mseStyleLocation);
                HtmlHelper.writeStart(pwSingleHymnBookContents);

                System.out.print("\r\tScanning " + nextHymnBook.getName() + " ");
                String inputFileName = hymnsPath + nextHymnBook.getInputFilename();

                // make the reader and writer
                BufferedReader brHymns = new BufferedReader(new FileReader(inputFileName));

                // read the first line of the hymn book
                hymnLine = brHymns.readLine();

                pwOverallHymnBooksContents.println("\t<h1 class=\"volume-title\"><a href=\"" + nextHymnBook.getContentsName() + "\">" + nextHymnBook.getName() + "</a></h1>");
                pwSingleHymnBookContents.println("\t<h1 class=\"volume-title\">" + nextHymnBook.getName() + "</h1>");

                // read the second line of the hymn book
                hymnLine = brHymns.readLine();

                // print out the start of the table

                // if there are still more lines
                while (hymnLine != null) {

                    // if it is a new hymn
                    if (hymnLine.indexOf("{") == 0) {

                        // get the hymn number
                        hymnNumber = hymnLine.substring(1, hymnLine.length() - 1);

                        int hymnNum = Integer.parseInt(hymnNumber);

                        String colClass = "col-xs-2";

                        if (hymnNum % 5 == 1) {
                            // if it is the first of a large column
                            if (hymnNum % 10 == 1) {
                                // if the first of a whole row
                                pwSingleHymnBookContents.println("\t<div class=\"row\">");
                            }
                            pwSingleHymnBookContents.println("\t\t<div class=\"col-lg-6\">" +
                                    "\n\t\t\t<div class=\"btn-toolbar\" role=\"toolbar\">" +
                                    "\n\t\t\t\t<div class=\"btn-group btn-group-lg btn-group-justified btn-group-fill-height\">");
                        }

                        printSingleHymnToContents(pwSingleHymnBookContents, nextHymnBook.getOutputFilename(), hymnNumber);

                        if (hymnNum % 5 == 0) {
                            // if it is the last hymn in a large column
                            pwSingleHymnBookContents.println("\t\t\t\t</div>" +
                                    "\n\t\t\t</div>" +
                                    "\n\t\t</div>");
                            if (hymnNum % 10 == 0) {
                                // if it is the last hymn in a whole row
                                pwSingleHymnBookContents.println("\n\t</div>");
                            }
                        }

                    }

                    // read the next line of the hymn book
                    hymnLine = brHymns.readLine();

                }

                // close the reader
                brHymns.close();

                // close the hymns table
                pwSingleHymnBookContents.println("\t\t\t\t</div>" +
                        "\n\t\t\t</div>" +
                        "\n\t\t</div>" +
                        "\n\t</div>");

                pwSingleHymnBookContents.println("</body>\n\n</html>");
                pwSingleHymnBookContents.close();

            }

            pwOverallHymnBooksContents.println("</body>\n\n</html>");

            // close the writer
            pwOverallHymnBooksContents.close();

        } catch (IOException ioe) {
            System.out.println("!*** Error preparing hymns contents ***!");
            System.out.println(ioe.getMessage());
        }

        System.out.println("\rFinished preparing Hymns contents");

    }

    private static void printSingleHymnToContents(PrintWriter pw, String hymnbookHtmlPath, String hymnNumber) {
        pw.println("\t\t\t\t\t<a class=\"btn btn-primary-outline\" href=\"" + hymnbookHtmlPath + "#" + hymnNumber + "\" role=\"button\">" + hymnNumber + "</a>");
    }

    // endregion

    // region ministry

    public static void prepareMinistry(Config cfg, Author author, String mseStylesLocation) {

        System.out.print("Preparing: " + author.getName());

        PrintWriter pwContents = null;

        try {

            // set up readers/writers
            File f;
            String volPath = cfg.getResDir() + File.separator + author.getPreparePath();
            f = new File(volPath);
            f.mkdirs();
            System.out.print("\r\tReading from " + f.getCanonicalPath());

            String volDestPath = cfg.getResDir() + File.separator + author.getTargetPath();
            f = new File(volDestPath);
            f.mkdirs();
            System.out.print("\r\tWriting to " + f.getCanonicalPath());

            BufferedReader brSourceText = null;
            PrintWriter pwHtml = null;

            AuthorPrepareCache apc = new AuthorPrepareCache(author);

            // write html head
            pwContents = new PrintWriter(new FileWriter(volDestPath + author.getCode() + "-Contents.htm"));
            HtmlHelper.writeHtmlHeader(pwContents, author.getName() + " contents", mseStylesLocation);
            HtmlHelper.writeStart(pwContents);
            HtmlHelper.writeContentsTitle(pwContents, author.getName() + " Contents");

//            printContentsVolumeNumbers(pwContents, author);

            // for each volume
            while (!apc.finishedVolumes) {
                try {

                    apc.clearVolumeValues();

                    File volumeFile = new File(volPath + author.getPrepareSourceName(apc.volNum));
                    if (volumeFile.exists()) {
                        apc.pageNum = 0;
                        apc.keepPageNumber = 0;

                        // print out progress for each volume
                        System.out.print("\rPreparing " + author.getCode() + " Volume: " + apc.volNum);

                        brSourceText = new BufferedReader(new FileReader(volumeFile));
                        pwHtml = new PrintWriter(new FileWriter(volDestPath + author.getCode() + apc.volNum + ".htm"));

                        // write html head
                        HtmlHelper.writeHtmlHeader(pwHtml, author.getName() + " Volume " + apc.volNum, mseStylesLocation);
                        HtmlHelper.writeStartAndContainer(pwHtml);

                        StringBuilder outputLine;
                        boolean skipLine;

                        // while there are still more lines
                        while ((apc.line = brSourceText.readLine()) != null) {
                            outputLine = new StringBuilder(apc.line);

                            if (outputLine.length() < 1) outputLine.append("<hr/>");

                            int charPosition = 0;

                            // heading or special line
                            if (outputLine.charAt(0) == '{') {
                                // start of special line
                                outputLine = getSpecialLine(apc, outputLine);

                                if (apc.cssClass.contains("volume-title"))
                                    printContentsVolumeTitle(pwContents, outputLine, author, apc.volNum);
                            } else if (outputLine.length() < 400) {

                                // if the line is all uppercase
                                String uppercaseLine = outputLine.toString().toUpperCase();
                                if ((uppercaseLine.equals(outputLine.toString()) && (outputLine.charAt(0) != ' '))) {
                                    printContentsHeading(pwContents, outputLine, author, apc.volNum, apc.pageNum);
                                    apc.cssClass = "heading";
                                }
                            }

                            int charsInSection = 0;

                            if ((outputLine.charAt(0) != '\u00a6') && (outputLine.charAt(0) != '{') && (apc.actualFootnotesNumber != 0)) {
                                // if it is a footnote (broken bar)
                                String error = "Missing footnote: " + author.getCode() + " " + apc.volNum + ":" + apc.pageNum;
                                if (!apc.messages.contains(error)) {
                                    apc.messages += "\n\t" + error;
                                }
                            }

                            // for each character in the line
                            while ((charPosition < outputLine.length()) && (apc.cssClass.equals(""))) {
                                char currentCharacter = outputLine.charAt(charPosition);

                                // add italics
                                if (currentCharacter == '*') {
                                    if (apc.startedItalics) {
                                        outputLine.replace(charPosition, charPosition + 1, "</i>");
                                    } else {
                                        outputLine.replace(charPosition, charPosition + 1, "<i>");
                                    }
                                    apc.startedItalics = !apc.startedItalics;
                                } else if (currentCharacter == '~') {
                                    // footnote
                                    outputLine.replace(charPosition, charPosition + 1,
                                            String.format("<i>see <a href=\"%s_footnotes.htm#%d:%d\">footnote</a></i>",
                                                    author.getContentsName(), apc.volNum, apc.pageNum));

                                    // increase character position by number of characters added (minus for testing)
                                    charPosition += 50 + author.getContentsName().length() + Integer.toString(apc.volNum).length() +
                                            Integer.toString(apc.pageNum).length() - 5;
                                } else if (currentCharacter == '\u00AC') {
                                    //possessive apostrophe (not character)

                                    outputLine.replace(charPosition, charPosition + 1, "'");
                                } else if ((currentCharacter == '.') || (currentCharacter == '?') || (currentCharacter == '!')) {
                                    // end of sentence

                                    if (charsInSection > 1) {
                                        outputLine.insert(charPosition + 1, String.format("<a name=%d:%d></a>", apc.pageNum, apc.section));
                                        apc.section++;
                                    }
                                    charsInSection = 0;
                                } else if (currentCharacter == '\u2022') {
                                    // bullet character

                                    outputLine.replace(charPosition, charPosition + 1, ".");
                                } else if (currentCharacter == '\u00A6') {
                                    // footnote (broken bar character

                                    if (charPosition == 0) {
                                        if (apc.actualFootnotesNumber == 0) {
                                            apc.footnotes = "";
                                        }
                                        apc.actualFootnotesNumber++;
                                        apc.actualFootnotes += "+";
                                        outputLine.replace(charPosition, charPosition + 1,
                                                String.format("<a class=\"footnote\" name=\"#%d:f%d\"><sup>%s</sup></a>",
                                                        apc.pageNum, apc.actualFootnotesNumber, apc.actualFootnotes));

                                        // set css class
                                        apc.cssClass = "footnote";

                                    } else {
                                        apc.footnotesNumber++;
                                        if (apc.footnotesNumber > apc.maxFootnotesNumber) {
                                            apc.maxFootnotesNumber = apc.footnotesNumber;
                                        }
                                        apc.footnotes += "+";
                                        outputLine.replace(charPosition, charPosition + 1, String.format("<a href=\"#%d:f%d\"><sup class=\"footnote-link\">%s</sup></a>",
                                                apc.pageNum, apc.footnotesNumber, apc.footnotes));
                                    }
                                } else if (currentCharacter == '@') {
                                    // start of a scripture reference

                                    // find book name
                                    int tempCharPos = charPosition + 1;


                                    //mjp? do you need length<4?
                                    while (((tempCharPos - charPosition) < 4) || (!Character.isDigit(outputLine.charAt(tempCharPos)))) {
                                        tempCharPos++;
                                    }
                                    String bookName = outputLine.substring(charPosition + 1, tempCharPos);

                                    if (bookName.equalsIgnoreCase("Psalm ")) {
//                                        System.out.println("Malformed link " + apc.author.getCode() + " " + apc.volNum + ":" + apc.pageNum);
                                        bookName = "Psalms ";
                                    }

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
                                    charPosition += reference.length() - 1;

                                    // end hymn reference
                                }
                                charPosition++;
                                charsInSection++;
                            } // end of processing line

                            HtmlHelper.wrapContent(apc, outputLine);

                            // reset css class
                            apc.cssClass = "";

                            // output the "outputLine"
                            pwHtml.println(outputLine.toString());

                            apc.lineCount++;
                        } // end of processing lines


                        pwHtml.println("\t</div>");
                        pwHtml.println("\n</body>\n\n</html>");
                        pwHtml.close();
                        brSourceText.close();

                        apc.volNum++;

                    } else {
                        apc.finishedVolumes = true;
                        if (!apc.messages.equals("")) {
                            apc.messages = apc.messages.replaceFirst("\n", "");
                            System.out.println("\r" + apc.messages);
                        }
                        System.out.println("\rFinished preparing " + author.getName());
                    }
                } catch (IOException ioe) {
                    System.out.println("\n!*** Error with " + author.getName() + " volume: " + apc.volNum);
                    System.out.println(ioe.getMessage());
                    apc.volNum++;
                } finally {
                    if (brSourceText != null) brSourceText.close();
                    if (pwHtml != null) pwHtml.close();
                }
            }

            HtmlHelper.writeContentsClose(pwContents);

        } catch (IOException ioe) {
            System.out.println("\n!*** Error preparing " + author.getName() + " ***!");
            System.out.println(ioe.getMessage());
        } finally {
            if (pwContents != null) pwContents.close();
        }
    } // end prepare (used for ministry)

    private static StringBuilder getSpecialLine(AuthorPrepareCache apc, StringBuilder line) {

        int charPosition = 0;
        int tempCharPosition = charPosition + 1;

        // get the bounds of the page number
        while (line.charAt(tempCharPosition) != '}') {
            tempCharPosition++;
        }

        // get the page number string
        String pageNumberTemp = line.substring(charPosition + 1, tempCharPosition);

        // if it is a page number
        if (pageNumberTemp.charAt(0) != '#') {
            // if it is a valid new page

            apc.pageNum = Integer.parseInt(pageNumberTemp);

            if ((apc.pageNum != apc.keepPageNumber + 1) && (apc.keepPageNumber != 0)) {
                if (apc.pageNum == apc.keepPageNumber) {
                    apc.messages += "\n\tDuplicate page: " + apc.author.getCode() + " " + apc.volNum + ":" + (apc.keepPageNumber);
                } else {
                    apc.messages += "\n\tMissing page: " + apc.author.getCode() + " " + apc.volNum + ":" + (apc.keepPageNumber + 1);
                }
            }
            apc.keepPageNumber = apc.pageNum;
            line.replace(charPosition, line.length(), String.format("<a name=%d>[Page %s]</a>", apc.pageNum, apc.pageNum));

            // reset page specific values
            apc.footnotes = "";
            apc.actualFootnotes = "";
            apc.footnotesNumber = 0;
            apc.actualFootnotesNumber = 0;
            apc.section = 1;

            // set the css class
            apc.cssClass = "page-number";

        } else {
            // remove decoration {#x} from volume title "x"
            line.replace(charPosition, charPosition + 2, "");
            line.replace(line.length() - 1, line.length(), "");

            // set the css class
            apc.cssClass = "volume-title";
        }
        return line;
    }

    private static void printContentsVolumeNumbers(PrintWriter pwContents, Author author) {

        pwContents.println("\t<p class=\"contents-title\">" + author.getName() + "</p>");
        pwContents.println("\t<p class=\"contents-sub-title\">Volumes:</p>");

        int i = 0;

        while (i < author.getNumVols()) {

            if ((i % 5) == 0) {
                if (i % 10 == 0) {
                    pwContents.println("\t<div class=\"row\">");
                }
                pwContents.println("\t\t<div class=\"col-lg-6\">" +
                        "\n\t\t\t<div class=\"btn-toolbar\" role=\"toolbar\">" +
                        "\n\t\t\t\t<div class=\"btn-group btn-group-lg btn-group-justified btn-group-fill-height\">");
            }

            pwContents.println(String.format("\t\t\t\t\t<a class=\"btn btn-primary-outline\" href=\"%s\" role=\"button\">%s</a>",
                    "#" + i, i));

            if ((i % 5) == 4) {
                pwContents.println("\t\t\t\t</div>" +
                        "\n\t\t\t</div>" +
                        "\n\t\t</div>");
                if (i % 10 == 9) {
                    pwContents.println("\t</div>");
                }
            }
            i++;
        }

        if ((i % 5) != 4) {
            pwContents.println("\t\t\t\t</div>" +
                    "\n\t\t\t</div>" +
                    "\n\t\t</div>");
            if (i % 10 != 9) {
                pwContents.println("\t</div>");
            }
        }

    }

    private static void printContentsHeading(PrintWriter pwContents, StringBuilder outputLine, Author author, int volNum, int pageNum) {

        pwContents.println(String.format("\t\t\t\t<a class=\"btn btn-success-outline\" href=\"%s\" role=\"button\">%s</a><span class=\"label label-primary\">%d</span>",
                author.getCode() + volNum + ".htm#" + pageNum, outputLine, pageNum));
        pwContents.println("\t\t\t\t<br>");

    }

    private static void printContentsVolumeTitle(PrintWriter pwContents, StringBuilder outputLine, Author author, int volNum) {

        if (volNum != 1) {
            HtmlHelper.writeSinglePanelBodyClose(pwContents);
        } else {
            HtmlHelper.writePanelGroupOpen(pwContents);
        }

        HtmlHelper.writePanelHeading(pwContents, volNum, outputLine.toString());
        HtmlHelper.writePanelBodyOpen(pwContents, volNum);
//
//        pwContents.println(String.format("\t\t<a class=\"btn btn-lg btn-success\" id=\"" +
//                volNum + "\" href=\"%s\" role=\"button\">%s</a>", author.getCode() + volNum + ".htm", outputLine));

    }

    // endregion

}
