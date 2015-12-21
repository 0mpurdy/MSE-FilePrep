package mse.processors;

import mse.data.AuthorPrepareCache;
import mse.data.BibleBook;
import mse.data.PreparePlatform;
import mse.helpers.HtmlHelper;
import mse.data.HymnBook;
import mse.common.Author;
import mse.common.Config;

import java.io.*;
import java.util.HashMap;

/**
 * Created by mj_pu_000 on 21/12/2015.
 */
public class Preparer {

    public static void prepareBibleHtml(Config cfg, String mseStyleLocation) {

        System.out.print("\nPreparing Bible");

        StringBuilder messages = new StringBuilder();

        try {

            // create file for outputting where the files are being read/written
            File f;

            // get the paths for the files that are used in preparing the bible html
            String jndBiblePath = cfg.getResDir() + "source" + File.separator + "bible" + File.separator;
            f = new File(jndBiblePath);
            f.mkdirs();
            System.out.print("\rReading JND bible from " + f.getCanonicalPath());

            String kjvBiblePath = cfg.getResDir() + "source" + File.separator + "kjv" + File.separator;
            f = new File(kjvBiblePath);
            f.mkdirs();
            System.out.print("\rReading KJV bible from " + f.getCanonicalPath());

            // get the file paths that the bible html and text will be written to
            String bibleDestinationPath = cfg.getResDir() + Author.BIBLE.getTargetPath();
            f = new File(bibleDestinationPath);
            f.mkdirs();
            System.out.print("\rWriting bible HTML to " + f.getCanonicalPath());

            String bibleTxtDestinationPath = cfg.getResDir() + "target" + File.separator + "bibleText" + File.separator;
            f = new File(bibleTxtDestinationPath);
            f.mkdirs();
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
                PrintWriter pwBible = new PrintWriter(new FileWriter(bibleDestinationPath + nextBook.getName().replaceAll("\\s", "") + ".htm", false));
                PrintWriter pwBibleTxt = new PrintWriter(new FileWriter(bibleTxtDestinationPath + Author.BIBLE.getCode() + bookNumber + ".txt"));

                // write the html header
                pwBible.println("<html>");
                pwBible.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + mseStyleLocation + "\">\n");
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
                                    messages.append("\n\tNo synopsis link for ").append(nextBook.getName()).append(" chapter ").append(chapter);
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
                            messages.append("Italics - ").append(bufferTxt);
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

        if (!messages.toString().equals("")) {
            System.out.println("\r" + messages);
        }

        System.out.print("\rFinished Preparing Bible");

    }

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
                pwHymns.println("<html>");
                pwHymns.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + mseStyleLocation + "\">\n");
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

            printContentsVolumeNumbers(pwContents, author);

            pwContents.println("\t<div class=\"container\">");

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
                        pwHtml.println(String.format("<!DOCTYPE html>\n<html>\n\n<head>\n\t<link rel=\"stylesheet\" type=\"" +
                                        "text/css\" href=\"" + mseStylesLocation + "\">\n\t<title>%s Volume %d</title>\n</head>\n\n<body>",
                                author.getName(), apc.volNum));

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

                            // add formatting to the line
                            if (apc.cssClass.equals("")) {
                                apc.cssClass = "paragraph";
                            }
                            outputLine.insert(0, "\t<div class=\"" + apc.cssClass + "\">\n\t\t");
                            outputLine.append("\n\t</div>");

                            // reset css class
                            apc.cssClass = "";

                            // output the "outputLine"
                            pwHtml.println(outputLine.toString());

                            apc.lineCount++;
                        } // end of processing lines

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

            pwContents.println("\t</div>");

        } catch (IOException ioe) {
            System.out.println("\n!*** Error preparing " + author.getName() + " ***!");
            System.out.println(ioe.getMessage());
        } finally {
            if (pwContents != null) pwContents.close();
        }
    } // end prepare (used for ministry)

    public static void createBibleContents(Config cfg, PreparePlatform preparePlatform) {

        System.out.print("\nCreating Bible contents...");

        String contentsFilePath = cfg.getResDir() + Author.BIBLE.getTargetPath(Author.BIBLE.getContentsName());

        File bibleContentsFile = new File(contentsFilePath);

        if (!bibleContentsFile.exists()) {
            bibleContentsFile.getParentFile().mkdirs();
            try {
                bibleContentsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PrintWriter pw = null;
        try {

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

        } catch (FileNotFoundException e) {
            System.out.println("Could not find file: " + contentsFilePath);
        } finally {
            if (pw != null) pw.close();
        }

        System.out.println("\rFinished creating Bible contents");

    }

    public static void createHymnsContents(Config cfg, String mseStyleLocation) {

        System.out.println("Creating hymns contents");

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

            PrintWriter pwHymns = new PrintWriter(new FileWriter(hymnsOutPath + "Hymns-Contents.htm"));

            // print the html header for the overall contents page
            HtmlHelper.writeHtmlHeader(pwHymns, "Hymn Contents", mseStyleLocation);

            // prepare html for each hymn book
            for (HymnBook nextHymnBook : HymnBook.values()) {

                // create a print writer for the hymnbook
                PrintWriter pwSingleHymnBook = new PrintWriter(new File(hymnsOutPath + nextHymnBook.getContentsName()));

                // print the html header for the single book contents page
                HtmlHelper.writeHtmlHeader(pwSingleHymnBook, "Hymn Contents", mseStyleLocation);

                System.out.print("\r\tScanning " + nextHymnBook.getName() + " ");
                String inputFileName = hymnsPath + nextHymnBook.getInputFilename();

                // make the reader and writer
                BufferedReader brHymns = new BufferedReader(new FileReader(inputFileName));

                // read the first line of the hymn book
                hymnLine = brHymns.readLine();

                pwHymns.println("<h1 class=\"volume-title\"><a href=\"" + nextHymnBook.getContentsName() + "\">" + nextHymnBook.getName() + "</a></h1>");
                pwSingleHymnBook.println("<h1 class=\"volume-title\">" + nextHymnBook.getName() + "</h1>");

                // read the second line of the hymn book
                hymnLine = brHymns.readLine();

                // print out the start of the table

                // if there are still more lines
                while (hymnLine != null) {

                    // if it is a new hymn
                    if (hymnLine.indexOf("{") == 0) {

                        // get the hymn number
                        hymnNumber = hymnLine.substring(1, hymnLine.length() - 1);

                        System.out.print("\r\tNumber: " + hymnNumber);

                        int hymnNum = Integer.parseInt(hymnNumber);

                        String colClass = "col-xs-2";

                        if (hymnNum % 5 == 1) {
                            // if it is the first of a large column
                            if (hymnNum % 10 == 1) {
                                // if the first of a whole row
                                pwSingleHymnBook.println("\t<div class=\"row\">");
                            }
                            pwSingleHymnBook.println("\t\t<div class=\"col-lg-6\">" +
                                    "\n\t\t\t<div class=\"btn-toolbar\" role=\"toolbar\">" +
                                    "\n\t\t\t\t<div class=\"btn-group btn-group-lg btn-group-justified btn-group-fill-height\">");
                            colClass = "col-xs-2 col-xs-offset-1";
                        }

                        printSingleHymnToContents(pwSingleHymnBook, nextHymnBook.getOutputFilename(), hymnNumber, colClass);

                        if (hymnNum % 5 == 0) {
                            // if it is the last hymn in a large column
                            pwSingleHymnBook.println("\t\t\t\t</div>" +
                                    "\n\t\t\t</div>" +
                                    "\n\t\t</div>");
                            if (hymnNum % 10 == 0) {
                                // if it is the last hymn in a whole row
                                pwSingleHymnBook.println("\n\t</div>");
                            }
                        }

                    }

                    // read the next line of the hymn book
                    hymnLine = brHymns.readLine();

                }

                // close the reader
                brHymns.close();

                // close the hymns table
                pwSingleHymnBook.println("\t\t\t\t</div>" +
                        "\n\t\t\t</div>" +
                        "\n\t\t</div>" +
                        "\n\t</div>");

                pwSingleHymnBook.println("</body>\n\n</html>");
                pwSingleHymnBook.close();

            }

            pwHymns.println("</body>\n\n</html>");

            // close the writer
            pwHymns.close();

            System.out.print(" *Done");

        } catch (IOException ioe) {
            System.out.println("!*** Error preparing hymns ***!");
            System.out.println(ioe.getMessage());
        }

        System.out.println("\rFinished preparing Hymns");

    }

    private static void printSingleHymnToContents(PrintWriter pw, String hymnbookHtmlPath, String hymnNumber, String colClass) {
        pw.println("\t\t\t\t\t<a class=\"btn btn-primary-outline\" href=\"" + hymnbookHtmlPath + "#" + hymnNumber + "\" role=\"button\">" + hymnNumber + "</a>");
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
        pwContents.println(String.format("\t\t\t\t<a class=\"btn btn-success-outline\" href=\"%s\" role=\"button\">%s</a>", author.getCode() + volNum + ".htm#" + pageNum, outputLine));

    }

    private static void printContentsVolumeTitle(PrintWriter pwContents, StringBuilder outputLine, Author author, int volNum) {

//        if (volNum > 1) pwContents.println("\t</div>");

        pwContents.println(String.format("\t\t<a class=\"btn btn-lg btn-success\" id=\"" +
                volNum + "\" href=\"%s\" role=\"button\">%s</a>", author.getCode() + volNum + ".htm", outputLine));

//        pwContents.println("\t<div class=\"btn-group-vertical\">");

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

}
