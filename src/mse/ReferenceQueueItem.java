package mse;

import mse.common.Author;

/**
 * Created by mj_pu_000 on 10/09/2015.
 */
public class ReferenceQueueItem {

    private Author author;
    private String token;
    private int volumeNumber;
    private int pageNumber;

    public ReferenceQueueItem(Author author, String token, int volumeNumber, int pageNumber) {
        this.token = token;
        this.volumeNumber = volumeNumber;
        this.pageNumber = pageNumber;
    }

    public String getToken() {
        return token;
    }

    public int getVolumeNumber() {
        return volumeNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
