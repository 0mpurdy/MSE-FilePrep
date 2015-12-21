/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mse.data;

/**
 * @author mj_pu_000
 */
public enum HymnBook {

    h1973("1973 Hymn Book", "hymns1973"),
    h1962("1962 Hymn Book", "hymns1962"),
    h1951("1951 Hymn Book", "hymns1951"),
    h1932("1932 Hymn Book", "hymns1932"),
    h1903("1903 Hymn Book", "hymns1903");

    private String name;
    private String filename;

    HymnBook(String name, String filename) {
        this.name = name;
        this.filename = filename;
    }

    public String getCode() {
        return filename;
    }

    public String getInputFilename() {
        return filename + ".txt";
    }

    public String getName() {
        return name;
    }

    public String getOutputFilename() {
        return filename + ".htm";
    }

    public String getContentsName() {
        return filename + "-Contents.htm";
    }
}
