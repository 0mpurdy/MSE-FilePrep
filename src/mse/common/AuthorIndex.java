package mse.common;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mj_pu_000 on 09/09/2015.
 */
public class AuthorIndex implements Serializable {

    private Author author;
    private HashMap<String, Integer> tokenCountMap;
    private HashMap<String, short[]> lastRefMap;
    private HashMap<String, Integer> nextReferenceIndex;
    private HashMap<String, short[]> references;

    // c - current
    int index;
    private short[] cLastReference = new short[2];
    private short[] cReferences;
    short[] newReferences;
    int count;

    public AuthorIndex(Author author) {
        this.author = author;
        tokenCountMap = new HashMap<>();
        lastRefMap = new HashMap<>();
        nextReferenceIndex = new HashMap<>();
        references = new HashMap<>();
    }

    public String getAuthorName() {
        return author.getName();
    }

    public HashMap<String, Integer> getTokenCountMap() {
        return tokenCountMap;
    }

    public HashMap<String, short[]> getReferencesMap() { return references; }

    public void incrementTokenCount(String token, short volumeNumber, short pageNumber) {
        count = -1;

        // if the token already exists
        if ((tokenCountMap.get(token)) != null) {

            // if the token should be ignored
            if (tokenCountMap.get(token) != -1) {
                count = tokenCountMap.get(token) + 1;

                // if the token is too frequent
                if (count < 10000) {

                    cLastReference = lastRefMap.get(token);

                    // if it is still in the same volume
                    if (cLastReference[0] == volumeNumber) {

                        // if not in the same page
                        if (cLastReference[1] != pageNumber) {

                            cReferences = references.get(token);
                            index = nextReferenceIndex.get(token);
                            nextReferenceIndex.put(token, index + 1);

                            // if the next index is greater than the length of the array
                            if (index >= cReferences.length) {
                                newReferences = Arrays.copyOf(cReferences, cReferences.length * 10);
                                newReferences[index] = pageNumber;
                                references.put(token, newReferences);
                            } else {
                                cReferences[index] = pageNumber;
                                references.put(token, cReferences);
                            }
                        }
                    } else {
                        // not in the same volume

                        cReferences = references.get(token);
                        index = nextReferenceIndex.get(token);
                        nextReferenceIndex.put(token, index + 2);

                        // if the next index (+1 for page number also added) is greater than the length of the array
                        if ((index + 1) >= cReferences.length) {
                            newReferences = Arrays.copyOf(cReferences, cReferences.length * 10);
                            newReferences[index] = (short) (volumeNumber * -1);
                            newReferences[index + 1] = pageNumber;
                            references.put(token, newReferences);
                        } else {
                            cReferences[index] = (short) (volumeNumber * -1);
                            cReferences[index + 1] = pageNumber;
                            references.put(token, cReferences);
                        }
                    }
                } else {

                    // if the token is too frequent
                    // empty the maps and ignore any future tokens of this type
                    references.put(token, new short[0]);
                    nextReferenceIndex.put(token, -1);

                    count = -1;
                }
            }
        } else {
            // if it is the first time this token has been found

            // create the new reference list

            count = 1;

            // add the reference
            cReferences = new short[2];
            cReferences[0] = (short) (volumeNumber * -1);
            cReferences[1] = pageNumber;
            nextReferenceIndex.put(token, 2);
            references.put(token, cReferences);
        }

        // update the last reference
        cLastReference = new short[2];
        cLastReference[0] = volumeNumber;
        cLastReference[1] = pageNumber;
        lastRefMap.put(token, cLastReference);

        tokenCountMap.put(token, count);
    }

    public int getTokenCount(String token) {
        if (tokenCountMap.get(token) != null) {
            return tokenCountMap.get(token);
        } else {
            return 0;
        }
    }

    public void cleanIndexArrays() {

        HashMap<String, short[]> newReferencesMap = new HashMap<>();

        for(Map.Entry<String, short[]> entry : references.entrySet()) {
            String token = entry.getKey();
            short[] oldReferences = entry.getValue();

            int nextReference = nextReferenceIndex.get(token);

            // ignore references with "nextReference" of -1
            if (nextReference != -1) {
                if (nextReference != oldReferences.length) {
                    short[] newReferences = Arrays.copyOf(oldReferences, nextReference);
                    newReferencesMap.put(token, newReferences);
                } else {
                    newReferencesMap.put(token, oldReferences);
                }
            }
        }
        references = newReferencesMap;
    }

    public Author getAuthor() {
        return author;
    }

    public short[] getReferences(String key) {
        return references.get(key);
    }

    public void loadIndex(String resLocation) {

        // try to load the index of the current author
        try {
            InputStream inStream = new FileInputStream(resLocation + author.getIndexFilePath());
            BufferedInputStream bInStream = new BufferedInputStream(inStream);
            ObjectInput input = new ObjectInputStream(bInStream);
            this.tokenCountMap = (HashMap<String, Integer>) input.readObject();
            this.references = (HashMap<String, short[]>) input.readObject();
        } catch (FileNotFoundException fnfe) {
            System.out.println("Could not file find file: " + resLocation + author.getIndexFilePath());
        } catch (IOException ioe) {
            System.out.println("Error loading from: " + author.getIndexFilePath());
        } catch (ClassCastException cce) {
            System.out.println("Error casting class when loading new index");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Class not found when loading new index");
        }

    }

    public void writeIndex(String location) {

        ObjectOutputStream objectOutputStream = null;

        try {

            File outputFile = new File(location);
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
                outputFile.createNewFile();
            }

            OutputStream file = new FileOutputStream(outputFile);
            BufferedOutputStream buffer = new BufferedOutputStream(file);
            objectOutputStream = new ObjectOutputStream(buffer);
            objectOutputStream.writeObject(tokenCountMap);
            objectOutputStream.writeObject(references);
        }
        catch(IOException ex){
            System.out.println("\nError writing index for " + author.getName() + " at location " + location);
        } finally {
            if (objectOutputStream != null) try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
