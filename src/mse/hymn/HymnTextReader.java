package mse.hymn;

import mse.common.Author;
import mse.data.PreparePlatform;

import java.io.*;
import java.util.ArrayList;

/**
 * Reads in hymn books from text in the standard format
 *
 * @author MichaelPurdy
 */
public class HymnTextReader {

    /**
     * Read all the hymn books into cache
     *
     * @param platform platform being generated
     * @return all the hymn books
     */
    public ArrayList<ArrayList<Hymn>> readAllHymnBooks(PreparePlatform platform) {

        System.out.print("Reading Hymns");

        ArrayList<ArrayList<Hymn>> allHymnBooks = new ArrayList<>();

        // folder for input
        String hymnsPath = Author.HYMNS.getPreparePath(platform);

        try {

            File f;

            // file for input
            f = new File(hymnsPath);
            f.mkdirs();
            System.out.print("\rReading Hymns from: " + f.getCanonicalPath());

            // process JSON for each hymn book
            for (HymnBookEnum nextHymnBookEnum : HymnBookEnum.values()) {
                allHymnBooks.add(readSingleHymnBook(nextHymnBookEnum, hymnsPath));
            }

        } catch (IOException e) {
            System.out.println("### Processing Hymns to JSON exception");
            System.out.println(e.getMessage());
        }

        System.out.println("\rFinished reading hymns from " + hymnsPath);

        return allHymnBooks;
    }

    /**
     * Read a single hymnbook into cache
     *
     * @param nextHymnBookEnum Next hymn book to be read in
     * @param hymnsPath    Folder to read the hymnbook from
     * @return all the hymns in the hymnbook
     * @throws IOException
     */
    private ArrayList<Hymn> readSingleHymnBook(HymnBookEnum nextHymnBookEnum, String hymnsPath) throws IOException {

        // static buffers for performance
        ArrayList<Hymn> allHymns = new ArrayList<>();
        String hymnLine;
//        String verseNumber;
        Hymn currentHymn = new Hymn();
        ArrayList<String> verse = new ArrayList<>();

        System.out.print("\r\tPreparing " + nextHymnBookEnum.getName() + " ");
        String inputFileName = hymnsPath + nextHymnBookEnum.getSourceFilename();

        // make the reader and writer
        BufferedReader brHymns = new BufferedReader(new FileReader(inputFileName));

        // read the first line of the hymn book (title eg {#Hymns (1973)}
        hymnLine = brHymns.readLine();
        hymnLine = brHymns.readLine();

        // if there are still more lines
        while (hymnLine != null) {

            // if it is a new hymn
            if (hymnLine.indexOf("{") == 0) {

                // add the previous non-empty verse to the hymn
                verse = addAndClearVerse(currentHymn, verse);

                allHymns.add(currentHymn);
                currentHymn = new Hymn();

                // get the hymn number
                currentHymn.setNumber(Integer.parseInt(hymnLine.substring(1, hymnLine.length() - 1)));

                System.out.printf("\r%d", currentHymn.getNumber());

                // read the meter/author line
                hymnLine = brHymns.readLine();

                // split the line by the comma and extract the info
                String[] info = hymnLine.split(",");
                if (info.length > 0) {
                    currentHymn.setAuthor(info[0]);
                    if (info.length > 1) {
                        currentHymn.setMeter(info[1].substring(1));
                    }
                }
            } else if (hymnLine.indexOf("|") == 0) {
                // if it is a new verse

                // get the verse number
//                hymnLine.substring(1, hymnLine.length() - 1);

                verse = addAndClearVerse(currentHymn, verse);

            } else {
                // if it is a verse line
                verse.add(hymnLine);
            }

            // read the next line of the hymn
            hymnLine = brHymns.readLine();

        }

        // close the reader
        brHymns.close();

        System.out.print(" - Done");

        return allHymns;

    }

    /**
     * Adds a non-empty verse to the hymn then returns new verse
     *
     * @param currentHymn Hymn to add the verse to
     * @param verse       The verse to add
     * @return new verse
     */
    private ArrayList<String> addAndClearVerse(Hymn currentHymn, ArrayList<String> verse) {
        if (verse.size() > 0) currentHymn.addVerse(verse);
        return new ArrayList<>();
    }
}

