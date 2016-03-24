package mse.processors;

import mse.data.*;
import mse.helpers.HtmlHelper;
import mse.common.Author;
import mse.common.Config;
import mse.processors.prepare.MinistryLine;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Michael Purdy on 21/12/2015.
 * <p>
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

                System.out.print("\rPreparing " + nextBook.getNameWithSpaces());
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
                    errMessages.append("\n\tNo synopsis link for ").append(bpc.book.getNameWithSpaces()).append(" chapter ").append(bpc.chapter);
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

                    pw.println("\t\t<div class=\"row bible-contents-row\">\n\t\t\t<div class=\"col-xs-6\"><a href=\"" +
                            preparePlatform.getLinkPrefix(Author.BIBLE) + BibleBook.values()[i].getBookFileName() + "\">" +
                            BibleBook.values()[i].getNameWithSpaces() + "</a></div>");

                    // if i+1 is less than the number of new testament books
                    if (i < BibleBook.getNumNewTestamentBooks()) {
                        pw.println("\t\t\t<div class=\"col-xs-6\"><a href=\"" + preparePlatform.getLinkPrefix(Author.BIBLE) +
                                BibleBook.values()[i + BibleBook.getNumOldTestamentBooks()].getBookFileName() + "\">" +
                                BibleBook.values()[i + BibleBook.getNumOldTestamentBooks()].getNameWithSpaces() + "</a></div>");
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
                    String value = String.format(" - <a href=\"../jnd/JND%s.html#%s\">go to synopsis</a>", synopsisNumbers[2], synopsisNumbers[3]);
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

            PrintWriter pwOverallHymnBooksContents = new PrintWriter(new FileWriter(hymnsOutPath + Author.HYMNS.getContentsName()));

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
            String sourceFolder = cfg.getResDir() + File.separator + author.getPreparePath();
            f = new File(sourceFolder);
            f.mkdirs();
            System.out.print("\r\tReading from " + f.getCanonicalPath());

            String targetFolder = cfg.getResDir() + File.separator + author.getTargetPath();
            f = new File(targetFolder);
            f.mkdirs();
            System.out.print("\r\tWriting to " + f.getCanonicalPath());

            BufferedReader brSourceText = null;
            PrintWriter pwHtml = null;

            AuthorPrepareCache apc = new AuthorPrepareCache(author);

            // create contents file
            pwContents = new PrintWriter(new FileWriter(targetFolder + author.getContentsName()));

            // write html head
            HtmlHelper.writeHtmlHeader(pwContents, author.getName() + " contents", mseStylesLocation);
            HtmlHelper.writeStart(pwContents);
            HtmlHelper.writeContentsTitle(pwContents, author.getName() + " Contents");

//            printContentsVolumeNumbers(pwContents, author);

            // for each volume
            while (!apc.finishedVolumes) {
                prepareMinistryVolume(apc, sourceFolder, author, brSourceText, pwHtml, pwContents, targetFolder, mseStylesLocation);
            }

            HtmlHelper.writeContentsClose(pwContents);

        } catch (IOException ioe) {
            System.out.println("\n!*** Error preparing " + author.getName() + " ***!");
            System.out.println(ioe.getMessage());
        } finally {
            if (pwContents != null) pwContents.close();
        }
    } // end prepare (used for ministry)

    private static void prepareMinistryVolume(AuthorPrepareCache apc, String sourceFolder, Author author,
                                              BufferedReader brSourceText, PrintWriter pwHtml, PrintWriter pwContents,
                                              String targetFolder, String mseStylesLocation) throws IOException {
        try {

            apc.clearVolumeValues();

            // check if there is a source file for the next volume
            File volumeFile = new File(sourceFolder + author.getPrepareSourceName(apc.volNum));
            if (volumeFile.exists()) {

                // print progress
                System.out.print("\rPreparing " + author.getCode() + " Volume: " + apc.volNum);

                // get source file
                brSourceText = new BufferedReader(new FileReader(volumeFile));

                // get output file
                pwHtml = new PrintWriter(new FileWriter(targetFolder + author.getVolumeName(apc.volNum)));

                // write html head
                HtmlHelper.writeHtmlHeader(pwHtml, author.getName() + " Volume " + apc.volNum, mseStylesLocation);
                HtmlHelper.writeStartAndContainer(pwHtml);

                StringBuilder outputLine;

                // read the volume title
                apc.line = brSourceText.readLine();

                if (apc.line != null) {

                    // check the volume title is valid
                    int volumeTitleLength = apc.line.length();
                    if (volumeTitleLength > 3 && apc.line.charAt(0) == '{' ||
                            apc.line.charAt(1) == '#' && apc.line.charAt(volumeTitleLength - 1) == '}') {

                        String volumeTitle = apc.line.substring(2, volumeTitleLength - 1);
                        printContentsVolumeTitle(pwContents, volumeTitle, apc.volNum);
                        HtmlHelper.printWrappedHtml(pwHtml, "volume-title", volumeTitle);

                        // process all pages
                        apc.line = brSourceText.readLine();
                        if (isPageNumber(apc.line)) {
                            apc.pageNum = getNextPage(apc.line);
                            validatePageNumber(apc);
                            String pageNumberText = String.format("<a name=%d>[Page %d]</a>", apc.pageNum, apc.pageNum);
                            HtmlHelper.printWrappedHtml(pwHtml, "page-number", pageNumberText);

                            while (apc.pageNum >= 0) {

                                processMinistryPage(apc, brSourceText, pwHtml, pwContents, author);

                                if (isPageNumber(apc.line)) {
                                    apc.pageNum = getNextPage(apc.line);
                                    validatePageNumber(apc);
                                    pageNumberText = String.format("<a name=%d>[Page %d]</a>", apc.pageNum, apc.pageNum);
                                    HtmlHelper.printWrappedHtml(pwHtml, "page-number", pageNumberText);
                                } else {
                                    apc.pageNum = -1;
                                }
                            }
                        } else {
                            apc.addMessage("Second line not a page number in volume " + apc.volNum);
                        }

                    } else {
                        apc.addMessage(author.getCode() + " volume " + apc.volNum + " error with title : " + apc.line);
                    }
                } else {
                    apc.addMessage("No first line for " + apc.author.getCode() + " " + apc.volNum + ":" + apc.pageNum);
                }

                apc.pageNum = 0;
                apc.prevPageNumber = 0;


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

    private static void processMinistryPage(AuthorPrepareCache apc, BufferedReader brSourceText, PrintWriter pwHtml,
                                            PrintWriter pwContents, Author author) throws IOException {

        StringBuilder outputLine;

        // while there are still more lines in the page
        while ((apc.line = brSourceText.readLine()) != null && !isPageNumber(apc.line)) {
            outputLine = new StringBuilder(apc.line);

            if (outputLine.length() > 0) {

                // heading or special line
                if (outputLine.charAt(0) == '{') {
                    // start of special line
                    outputLine = getSpecialLine(apc, outputLine);
                } else if (outputLine.length() < 400) {

                    // if the line is all uppercase
                    String uppercaseLine = outputLine.toString().toUpperCase();
                    if ((uppercaseLine.equals(outputLine.toString()) && (outputLine.charAt(0) != ' '))) {
                        printContentsHeading(pwContents, outputLine, author, apc.volNum, apc.pageNum);
                        apc.cssClass = "heading";
                    }
                }

//                if ((outputLine.charAt(0) != '^') && (outputLine.charAt(0) != '{') && (apc.actualFootnotesNumber != 0)) {
//                    // if it is a footnote
//                    String error = "Missing footnote: " + author.getCode() + " " + apc.volNum + ":" + apc.pageNum;
//                    if (!apc.messages.contains(error)) {
//                    }
//                }

                processMinistryLine(outputLine, apc, author);

            } else {
                outputLine.append("<hr/>");
            }

            HtmlHelper.wrapContent(apc.cssClass, outputLine);

            // reset css class
            apc.cssClass = "";

            // output the "outputLine"
            pwHtml.println(outputLine.toString());

            apc.lineCount++;
        } // end of processing lines
    }

    private static void processMinistryLine(StringBuilder outputLine, AuthorPrepareCache apc, Author author) {

        int charsInSentence = 0;
        int charPosition = 0;

        MinistryLine mLine = new MinistryLine(outputLine.toString());

        // for each character in the line
        while ((charPosition < outputLine.length()) && (apc.cssClass.equals(""))) {
            char currentCharacter = outputLine.charAt(charPosition);

            // add italics
            switch (currentCharacter) {
                case '*': // italics
                    apc.mPage.invertItalics();
                    break;
                case '~': // specific footnote
                    outputLine.replace(charPosition, charPosition + 1,
                            String.format("<i>see <a href=\"%s_footnotes.html#%d:%d\">footnote</a></i>",
                                    author.getContentsName(), apc.volNum, apc.pageNum));

                    // increase character position by number of characters added (minus for testing)
                    charPosition += 50 + author.getContentsName().length() + Integer.toString(apc.volNum).length() +
                            Integer.toString(apc.pageNum).length() - 5;
                    break;
                case '.':
                case '?':
                case '!': // end of sentence
                    if (charsInSentence > 1) {
                        String sentenceLink = String.format("<a name=%d:%d></a>", apc.pageNum, apc.section);
                        outputLine.insert(charPosition + 1, sentenceLink);
                        apc.section++;
                        charPosition += sentenceLink.length();
                    }
                    charsInSentence = 0;
                    break;
                case '^': // generic footnote
                    charPosition = addFootnote(outputLine, apc, charPosition);
                    break;
                case '@': // start of a scripture reference
                    charPosition = addScriptureLink(charPosition, outputLine, apc);
                    break;
                case '`': // start of hymn reference
                    charPosition = addHymnLink(charPosition, outputLine);
                    break;
            }
            charPosition++;
            charsInSentence++;
        } // end of processing line
    }

    private static int addHymnLink(int charPosition, StringBuilder outputLine) {
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
        String reference = String.format("<a href=\"..\\hymns\\hymns1972.html#%s:%s\">Hymn %s</a>", hymnNumber, verseNumber, hymnNumber);

        // insert the reference
        outputLine.replace(charPosition, tempCharPos, reference);

        // move the char position forward
        return charPosition + reference.length() - 1;

        // end hymn reference
    }

    private static int addScriptureLink(int charPosition, StringBuilder outputLine, AuthorPrepareCache apc) {

        int startOfBookName = charPosition + 1;
        int endOfBookName = startOfBookName;

        // find start of bookname
        while (Character.isWhitespace(outputLine.charAt(startOfBookName))) {
            startOfBookName++;
        }

        // find start of book word (to include books with preceding numbers eg: 2 Timothy)
        while (endOfBookName < outputLine.length() && (!Character.isLetter(outputLine.charAt(endOfBookName)))) {
            endOfBookName++;
        }

        // find end of book name
        while (endOfBookName < outputLine.length() && ((!Character.isDigit(outputLine.charAt(endOfBookName))))) {
            endOfBookName++;
        }
        String bookName = outputLine.substring(startOfBookName, endOfBookName);

        bookName = removeTrailingWhiteSpace(bookName);
        if (bookName.length() < 1) {
            apc.addMessage("Malformed Scripture link " + apc.author + " " + apc.volNum + ":" + apc.pageNum);
        }

        if (bookName.equalsIgnoreCase("Psalm")) {
//            System.out.println("Malformed link " + apc.author.getCode() + " " + apc.volNum + ":" + apc.pageNum);
            bookName = "Psalms";
        }

        int startOfChapter = endOfBookName;
        int endOfChapter = startOfChapter;

        // get chapter
        String chapter = "";
        boolean finishedDoing = false;
        while (!finishedDoing) {
            if (endOfChapter >= outputLine.length()) {
                finishedDoing = true;
            } else if (Character.isDigit(outputLine.charAt(endOfChapter))) {
                chapter += outputLine.charAt(endOfChapter);
                endOfChapter++;
            } else {
                finishedDoing = true;
            }
        }

        // find verse
        int startOfVerse = endOfChapter;

        //skip white space
        finishedDoing = false;
        while (!finishedDoing) {
            if (startOfVerse >= outputLine.length()) {
                finishedDoing = true;
            } else if (outputLine.charAt(startOfVerse) == ' ') {
                startOfVerse++;
            } else {
                finishedDoing = true;
            }
        }

        // find verse
        String verse = "";
        int endOfVerse = startOfVerse;
        if (endOfVerse < outputLine.length()) {
            if (outputLine.charAt(endOfVerse) == ':') {
                endOfVerse++;

                //skip white space
                finishedDoing = false;
                while (!finishedDoing) {
                    if (endOfVerse >= outputLine.length()) {
                        finishedDoing = true;
                    } else if (outputLine.charAt(endOfVerse) == ' ') {
                        endOfVerse++;
                    } else {
                        finishedDoing = true;
                    }
                }

                // populate verse string
                finishedDoing = false;
                while (!finishedDoing) {
                    if (endOfVerse >= outputLine.length()) {
                        finishedDoing = true;
                    } else if (Character.isDigit(outputLine.charAt(endOfVerse))) {
                        verse += outputLine.charAt(endOfVerse);
                        endOfVerse++;
                    } else {
                        finishedDoing = true;
                    }
                }
            }
        } // end finding verse

        String reference;
        int endOfReference;
        if (verse.length() > 0) {
            // if the reference has a verse
            reference = HtmlHelper.getBibleHtmlLink(bookName, chapter, verse);
            endOfReference = endOfVerse;
        } else {
            reference = HtmlHelper.getBibleHtmlLink(bookName, chapter);
            endOfReference = endOfChapter;
        }
        outputLine.replace(charPosition, endOfReference, reference);
        return charPosition + reference.length() - 1;

        // end scripture reference
    }

    private static String removeTrailingWhiteSpace(String bookName) {
        int i = bookName.length() - 1;
        if (i < 0) return "";
        while (Character.isWhitespace(bookName.charAt(i))) {
            i--;
            if (i < 0) return "";
        }
        return bookName.substring(0, i + 1);
    }


    private static int addFootnote(StringBuilder outputLine, AuthorPrepareCache apc, int charPosition) {
        String footnoteLink;

        if (charPosition != 0) {
            apc.unresolvedFootnotes++;
            apc.unresolvedFootnoteIdentifier += "+";
            footnoteLink = String.format("<a href=\"#%d:f%d\"><sup class=\"footnote-link\">%s</sup></a>",
                    apc.pageNum, apc.unresolvedFootnotes, apc.unresolvedFootnoteIdentifier);

            outputLine.replace(charPosition, charPosition + 1, footnoteLink);
            return charPosition + footnoteLink.length();

        } else {
            apc.resolvedFootnotes++;
            apc.resolvedFootnoteIdentifier += "+";
            footnoteLink = String.format("<a class=\"footnote\" name=\"#%d:f%d\"><sup>%s</sup></a>",
                    apc.pageNum, apc.resolvedFootnotes, apc.resolvedFootnoteIdentifier);

            // set css class
            apc.cssClass = "footnote";
        }

        outputLine.replace(charPosition, charPosition + 1, footnoteLink);
        return charPosition + footnoteLink.length();
    }

    private static boolean isPageNumber(String line) {
        if (line == null) return false;
        if (line.length() < 3) return false;
        if (line.charAt(0) != '{') return false;
        if (line.charAt(line.length() - 1) != '}') return false;

        // check all characters between {} are digits
        for (int i = 1; i < line.length() - 1; i++) {
            if (!(Character.isDigit(line.charAt(i)))) return false;
        }

        return true;
    }

    private static int getNextPage(String line) {
        // remove brackets
        line = line.substring(1, line.length() - 1);

        int pageNum;
        try {
            pageNum = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            pageNum = -1;
            e.printStackTrace();
        }

        return pageNum;
    }

    private static void validatePageNumber(AuthorPrepareCache apc) {
        if ((apc.pageNum != apc.prevPageNumber + 1) && (apc.prevPageNumber != 0)) {
            if (apc.pageNum == apc.prevPageNumber) {
                apc.addMessage("Duplicate page: " + apc.author.getCode() + " " + apc.volNum + ":" + (apc.prevPageNumber));
            } else {
                apc.addMessage("Missing page: " + apc.author.getCode() + " " + apc.volNum + ":" + (apc.prevPageNumber + 1));
            }
        }

        // if there are unresolved footnotes print error
        if (apc.unresolvedFootnotes > apc.resolvedFootnotes) {
            apc.addMessage("Missing footnote: " + apc.author.getCode() + " " + apc.volNum + ":" + apc.prevPageNumber);
        }

        apc.prevPageNumber = apc.pageNum;

        // reset page specific values
        apc.clearPageValues();
    }

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


            // set the css class
            apc.cssClass = "page-number";

            // if there are unresolved footnotes print error
            if (apc.unresolvedFootnotes > apc.resolvedFootnotes) {
                System.out.println("Missing footnote: " + apc.author.getCode() + " " + apc.volNum + ":" + apc.pageNum);
            }

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
                author.getCode() + volNum + ".html#" + pageNum, outputLine, pageNum));
        pwContents.println("\t\t\t\t<br>");

    }

    private static void printContentsVolumeTitle(PrintWriter pwContents, String volumeTitle, int volNum) {

        if (volNum != 1) {
            HtmlHelper.writeSinglePanelBodyClose(pwContents);
        } else {
            HtmlHelper.writePanelGroupOpen(pwContents);
        }

        HtmlHelper.writePanelHeading(pwContents, volNum, volumeTitle);
        HtmlHelper.writePanelBodyOpen(pwContents, volNum);
//
//        pwContents.println(String.format("\t\t<a class=\"btn btn-lg btn-success\" id=\"" +
//                volNum + "\" href=\"%s\" role=\"button\">%s</a>", author.getCode() + volNum + ".htm", outputLine));

    }

    // endregion

}
