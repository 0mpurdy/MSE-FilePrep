package com.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mj_pu_000 on 09/09/2015.
 */
public class AuthorIndex_old implements Serializable {

    private Author author;
    private HashMap<String, Integer> tokenCountMap;
    private HashMap<String, Integer> tokenIndexMap;
    private ArrayList<ArrayList<String>> references;

    public AuthorIndex_old(Author author) {
        this.author = author;
        tokenCountMap = new HashMap<>();
        tokenIndexMap = new HashMap<>();
        references = new ArrayList<>();
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

            // if there is not already a reference for this token on this page then add one
            ArrayList<String> currentReferenceList = references.get(tokenIndexMap.get(token));
            String currentReference = String.format("%d:%d", volumeNumber, pageNumber);
            if (!currentReferenceList.contains(currentReference)) {
                currentReferenceList.add(currentReference);
            }
        } else {
            // if it is the first time this token has been found
            count = 1;
            tokenIndexMap.put(token, references.size());
            references.add(new ArrayList<>());
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
}
