package mse.hymn;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Hymn
 *
 * @author MichaelPurdy
 */
public class Hymn implements Serializable {

    private int number;
    private String meter;
    private String author;
    private ArrayList<ArrayList<String>> verses;

    public Hymn() {
        verses = new ArrayList<>();
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void addVerse(ArrayList<String> verse) {
        this.verses.add(verse);
    }

    public void clearHymn() {
        this.number = 0;
        this.meter = null;
        this.author = null;
        this.verses.clear();
    }

    public int getNumber() {
        return number;
    }

    public String getAuthor() {
        return author;
    }
}
