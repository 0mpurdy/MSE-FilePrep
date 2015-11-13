package mse;

import mse.common.Author;

/**
 * Created by mj_pu_000 on 10/09/2015.
 */
public class ReferenceQueueItem {

    private Author author;
    private String token;

    // public values are bad but faster

    public short volumeNumber;
    public short pageNumber;

    public ReferenceQueueItem(Author author, String token, short volumeNumber, short pageNumber) {
        this.token = token;
        this.volumeNumber = volumeNumber;
        this.pageNumber = pageNumber;
    }

    public String getToken() {
        return token;
    }
}
