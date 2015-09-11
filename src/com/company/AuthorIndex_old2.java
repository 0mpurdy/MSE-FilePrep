package com.company;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by mj_pu_000 on 09/09/2015.
 */
public class AuthorIndex_old2 implements Serializable {

    private Author author;
    private HashMap<String, Integer> tokenCountMap;
    private HashMap<String, Integer> lastPage;
    private HashMap<String, Integer> nextReferenceIndex;
    private HashMap<String, String[]> references;

    public AuthorIndex_old2(Author author) {
        this.author = author;
        tokenCountMap = new HashMap<>();
        lastPage = new HashMap<>();
        nextReferenceIndex = new HashMap<>();
        references = new HashMap<>();
    }

    public String getAuthorName() {
        return author.getName();
    }

    public HashMap<String, Integer> getTokenCountMap() {
        return tokenCountMap;
    }

    public void incrementTokenCount(String token, int volumeNumber, int pageNumber) {
        int count;

        // if the token already exists
        if ((tokenCountMap.get(token)) != null) {
            count = tokenCountMap.get(token) + 1;

            // if this page hasn't already been added
            if (lastPage.get(token) != pageNumber) {

                String[] currentReferenceList = references.get(token);
                int index = nextReferenceIndex.get(token) + 1;

                String currentReference = String.format("%d:%d", volumeNumber, pageNumber);

                // if the next index is greater than the length of the array
                if (index >= currentReferenceList.length) {
                    String[] newReferenceList = Arrays.copyOf(currentReferenceList, currentReferenceList.length * 10);
                    newReferenceList[index] = currentReference;
                    references.put(token, newReferenceList);
                } else {
                    currentReferenceList[index] = currentReference;
                    references.put(token, currentReferenceList);
                }
            }
        } else {
            // if it is the first time this token has been found

            // create the new reference list
            String[] referencesList = new String[10];

            count = 1;

            // add the reference
            String currentReference = String.format("%d:%d", volumeNumber, pageNumber);
            referencesList[0] = currentReference;
            lastPage.put(token, 0);
            nextReferenceIndex.put(token, -1);
            references.put(token, referencesList);
        }
        tokenCountMap.put(token, count);
    }

    public int getTokenCount(String token) {
        if (tokenCountMap.get(token) != null) {
            return tokenCountMap.get(token);
        } else {
            return 0;
        }
    }

    public Author getAuthor() {
        return author;
    }
}
