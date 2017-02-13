package mse.helpers;

import mse.common.Author;
import mse.data.PreparePlatform;
import mse.hymn.HymnBook;
import mse.hymn.HymnBookHelper;

import java.io.*;
import java.util.ArrayList;

/**
 * Handles writing of java serialized files
 * (todo reading of serialized files)
 *
 * @author MichaelPurdy
 */
public class Serializer {

    /**
     * Writes an array list of hymn books to a java serialized file
     *
     * @param hymnbooks List of hymnbooks to serialize
     */
    public static void serializeHymnBooks(String folder, ArrayList<HymnBook> hymnbooks) {

        for (HymnBook nextHymnBook : hymnbooks) {
            serializeHymnBook(folder, nextHymnBook);
        }

    }

    private static void serializeHymnBook(String folder, HymnBook hymnbook) {

        try {
            FileOutputStream fileOut = new FileOutputStream(folder + HymnBookHelper.getSerializedName(hymnbook));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(hymnbook);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in /tmp/employee.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }

    }

    public static ArrayList<HymnBook> readHymnBooks(String folder, String[] names) {
        ArrayList<HymnBook> allHymnBooks = new ArrayList<>( );
        try {
            for (String name : names) {
                FileInputStream fileIn = new FileInputStream(folder + HymnBookHelper.getSerializedName(name));
                ObjectInputStream in = new ObjectInputStream(fileIn);
                HymnBook hymnBook = (HymnBook) in.readObject();
                allHymnBooks.add(hymnBook);
                in.close();
                fileIn.close();
            }
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Hymn Book class not found");
            c.printStackTrace();
            return null;
        }
        return allHymnBooks;
    }
}
