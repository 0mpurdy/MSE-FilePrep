package mse.hymn;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Hymn book
 *
 * @author MichaelPurdy
 */
public class HymnBook implements Serializable {

    private String name;
    private String year;
    private ArrayList<Hymn> hymns;

    public HymnBook() {
        hymns = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void addHymn(Hymn hymn) {
        hymns.add(hymn);
    }
}
