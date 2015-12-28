package mse;

import mse.common.Author;
import mse.common.AuthorIndex;
import mse.common.Config;
import mse.data.BibleBook;
import mse.data.HymnBook;
import mse.data.PreparePlatform;
import mse.helpers.HtmlHelper;
import mse.processors.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Config cfg = new Config();

        Scanner sc = new Scanner(System.in);

        // the platform that is being prepared for
        PreparePlatform platform = null;

        System.out.println("MSE File Prep console application");
        System.out.println("Version: " + cfg.getMseVersion());

        int mainMenuChoice = -1;

        while (mainMenuChoice != 0) {
            printMainMenu();

            int authorChoice;
            mainMenuChoice = sc.nextInt();
            sc.nextLine();

            switch (mainMenuChoice) {
                case 0:
                    // exit
                    System.out.println("Closing ...");
                    break;
                case 1:
                    // prepare all files
                    platform = chooseSystem(sc);
                    if (platform != null) {

                        System.out.println("Preparing all files");

                        // prepare bible
                        Author.BIBLE.setTargetFolder(platform.getTargetFolder());
                        Preparer.prepareBibleHtml(cfg, platform.getStylesLink());

                        // prepare hymns
                        Author.HYMNS.setTargetFolder(platform.getTargetFolder());
                        Preparer.prepareHymnsHtml(cfg, platform.getStylesLink());

                        // prepare ministry
                        for (Author nextAuthor : Author.values()) {
                            if (nextAuthor.isMinistry()) {
                                nextAuthor.setTargetFolder(platform.getTargetFolder());
                                Preparer.prepareMinistry(cfg, nextAuthor, platform.getStylesLink());
                            }
                        }
                        Preparer.createBibleContents(cfg, platform);
                        Preparer.createHymnsContents(cfg, platform.getStylesLink());
                    }
                    break;
                case 2:
                    // prepare single author
                    System.out.println("\nWhich author do you wish to prepare?");
                    printAuthorMenu();
                    authorChoice = sc.nextInt();
                    sc.nextLine();

                    platform = chooseSystem(sc);
                    if (platform != null) {
                        if (authorChoice == 0) {
                            Author.BIBLE.setTargetFolder(platform.getTargetFolder());
                            Preparer.prepareBibleHtml(cfg, platform.getStylesLink());
                        } else if (authorChoice == 1) {
                            Author.HYMNS.setTargetFolder(platform.getTargetFolder());
                            Preparer.prepareHymnsHtml(cfg, platform.getStylesLink());
                        } else if ((authorChoice >= 3) && (authorChoice <= 12)) {
                            Author.values()[authorChoice].setTargetFolder(platform.getTargetFolder());
                            Preparer.prepareMinistry(cfg, Author.values()[authorChoice], platform.getStylesLink());
                        } else {
                            System.out.println("\nOption " + authorChoice + " is not available at the moment");
                        }
                    }
                    break;
                case 3:
                    // create all indexes
                    platform = chooseSystem(sc);
                    if (platform != null) {
                        System.out.println("Creating all indexes ...");
                        long startIndexing = System.nanoTime();
                        // add a reference processor for each author then write the index
                        for (Author nextAuthor : Author.values()) {
                            if (nextAuthor.isSearchable()) {
                                nextAuthor.setTargetFolder(platform.getTargetFolder());
                                processAuthor(nextAuthor, cfg);
                            }
                        }
                        long endIndexing = System.nanoTime();
                        System.out.println("Total Index Time: " + ((endIndexing - startIndexing) / 1000000) + "ms");
                    } // end creating all indexes
                    break;
                case 4:
                    // create single author index
                    System.out.println("\nWhich author do you wish to index?");
                    printAuthorMenu();
                    authorChoice = sc.nextInt();
                    sc.nextLine();

                    platform = chooseSystem(sc);
                    if (platform != null) {
                        if ((authorChoice >= 0) && (authorChoice < Author.values().length)) {
                            Author author = Author.values()[authorChoice];
                            author.setTargetFolder(platform.getTargetFolder());
                            processAuthor(author, cfg);
                        } else {
                            System.out.println("This is not a valid option");
                        }
                    }
                    break;
                case 5:
                    // create super index
                    System.out.println("Creating super index");
                    createSuperIndex(cfg);
                    break;
                case 6:
                    // check author index
                    System.out.println("\nWhich author index do you wish to check?");
                    printAuthorMenu();
                    authorChoice = sc.nextInt();
                    sc.nextLine();
                    if ((authorChoice >= 0) && (authorChoice < Author.values().length)) {
                        Author author = Author.values()[authorChoice];
                        if (author.isSearchable()) {
                            AuthorIndex authorIndex = new AuthorIndex(author);
                            authorIndex.loadIndex(cfg.getResDir());
                            System.out.println(authorIndex.getTokenCountMap().size());

                            System.out.print("Do you wish to write the index to a file (y/n): ");
                            if (sc.nextLine().equalsIgnoreCase("y")) {
                                try {
                                    BufferedWriter bw = new BufferedWriter(new FileWriter("index.txt"));
                                    for (Map.Entry<String, short[]> entry : authorIndex.getReferencesMap().entrySet()) {
                                        bw.write("\"" + entry.getKey() + "\": [");
                                        for (short ref : entry.getValue()) {
                                            bw.write(ref + ", ");
                                        }
                                        bw.write("]\n");
                                    }
                                } catch (IOException ioe) {
                                    System.out.println("Error writing index");
                                }
                            }
                        } else {
                            System.out.println("This author is not searchable");
                        }
                    } else {
                        System.out.println("This is not a valid option");
                    }
                    break;
                case 7:
                    // check all indexes
                    ArrayList<AuthorIndex> authorIndexes = new ArrayList<>();
                    for (Author nextAuthor : Author.values()) {
                        if (nextAuthor.isSearchable()) {
                            AuthorIndex authorIndex = new AuthorIndex(nextAuthor);
                            authorIndex.loadIndex(cfg.getResDir());
                            authorIndexes.add(authorIndex);
                        }
                    }
                    break;
                case 8:
                    // create bible contents
                    platform = chooseSystem(sc);
                    if (platform != null) {
                        Author.BIBLE.setTargetFolder(platform.getTargetFolder());
                        Preparer.createBibleContents(cfg, platform);
                    }
                    break;
                case 9:
                    // create hymns contents
                    platform = chooseSystem(sc);
                    if (platform != null) {
                        Author.HYMNS.setTargetFolder(platform.getTargetFolder());
                        Preparer.createHymnsContents(cfg, platform.getStylesLink());
                    }
                    break;
                case 10:
                    // benchmark
                    System.out.println("Benchmarking ...\n\n");
                    new Benchmark().run();
                    break;
                default:
                    System.out.println("Invalid choice");
            }
        }

    }

    private static void printMainMenu() {

        System.out.println("\nOptions: ");

        ArrayList<String> options = new ArrayList<>();
        options.add("Close");
        options.add("Prepare all files");
        options.add("Prepare single author");
        options.add("Create all indexes");
        options.add("Create single author index");
        options.add("Create super index");
        options.add("Check author index");
        options.add("Check all author indexes");
        options.add("Prepare Bible contents");
        options.add("Prepare hymns contents");
        options.add("Benchmark");

        printMenu(options);

    }

    private static void printAuthorMenu() {
        System.out.println("Authors: ");
        int i = 0;
        for (Author nextAuthor : Author.values()) {
            System.out.println(i + " - " + nextAuthor.getName());
            i++;
        }
        System.out.print("Choose an option: ");
    }

    private static PreparePlatform chooseSystem(Scanner sc) {

        System.out.println("\nChoose a system:");

        ArrayList<String> systems = new ArrayList<>();
        systems.add("Cancel");
        for (PreparePlatform platform : PreparePlatform.values()) {
            systems.add(platform.getName());
        }

        printMenu(systems);
        int option = sc.nextInt();
        sc.nextLine();

        switch (option) {
            case 0:
                return null;
            default:
                return PreparePlatform.values()[option - 1];
        }
    }

    private static void printMenu(ArrayList<String> menu) {
        int i = 0;
        for (String option : menu) {
            System.out.println(i + " - " + option);
            i++;
        }
        System.out.print("Choose an option: ");
    }

    private static void processAuthor(Author author, Config cfg) {

        ArrayList<String> messages = new ArrayList<>();

        long startAuthor = System.nanoTime();

        final ReferenceQueue referenceQueue = new ReferenceQueue(author, cfg);
        ReferenceProcessor referenceProcessor = new ReferenceProcessor(referenceQueue);
        referenceProcessor.start();
        Indexer.indexAuthor(cfg, author, referenceQueue, messages);
        System.out.println();
        referenceProcessor.interrupt();
        while (referenceProcessor.isAlive()) {
            try {
                referenceProcessor.join(500);
                System.out.print("\rWords left: " + referenceQueue.size());
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        long endAuthor = System.nanoTime();

        System.out.println("\nAuthor Time: " + ((endAuthor - startAuthor) / 1000000) + "ms");

        for (String message : messages) {
            System.out.println(message);
        }

    }

    private static void createSuperIndex(Config cfg) {

        // super index token count
        HashMap<String, Integer> superIndex = new HashMap<>();

        // Read each index and create the super index
        for (Author nextAuthor : Author.values()) {
            if (nextAuthor.isSearchable()) {

                System.out.println("Creating super index for " + nextAuthor.getName());

                AuthorIndex nextAuthorIndex = new AuthorIndex(nextAuthor);
                nextAuthorIndex.loadIndex(cfg.getResDir());

                // if the index loads
                if (nextAuthorIndex != null) {
                    // for each word in the author index
                    HashMap<String, Integer> authorTokenCount = nextAuthorIndex.getTokenCountMap();
                    for (String token : authorTokenCount.keySet()) {

                        // key: author code + token
                        // value: token count for author
                        superIndex.put(nextAuthor.getCode() + "-" + token, authorTokenCount.get(token));
                    }
                } else {
                    System.out.println("Author index cannot be null");
                }
            }

        }

        try {

            // output super index
            FileOutputStream fileOutStream = new FileOutputStream(cfg.getResDir() + "super.idx");
            ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
            objectOutStream.writeObject(superIndex);
            objectOutStream.flush();
            objectOutStream.close();

        } catch (IOException ioe) {
            System.out.println("Error writing super index file");
            System.out.println(ioe.getMessage());
        }
    }

//    private static AuthorIndex readIndex(String filename) {
//
//        AuthorIndex result;
//        ObjectInputStream objectInputStream = null;
//
//        try {
//            File indexFile = new File(filename);
//            InputStream inStream = new FileInputStream(indexFile);
//            BufferedInputStream bInStream = new BufferedInputStream(inStream);
//            objectInputStream = new ObjectInputStream(bInStream);
//
//            result =  (AuthorIndex) objectInputStream.readObject();
//
//        } catch (IOException ioe) {
//            System.out.println("Error reading file: " + filename);
//            System.out.println(ioe.getMessage());
//            result = null;
//        } catch (ClassNotFoundException cnfe) {
//            System.out.println("Invalid class in file: " + filename);
//            result = null;
//        } finally {
//            if (objectInputStream != null) {
//                try {
//                    objectInputStream.close();
//                } catch (IOException ioe) {
//                    System.out.println("Error closing: " + filename);
//                }
//            }
//        }
//        return result;
//    }

    private static HashMap<String, Integer> readSuperIndex(String filename) {

        HashMap<String, Integer> result;
        ObjectInputStream objectInputStream = null;

        try {
            File indexFile = new File(filename);
            InputStream inStream = new FileInputStream(indexFile);
            BufferedInputStream bInStream = new BufferedInputStream(inStream);
            objectInputStream = new ObjectInputStream(bInStream);

            try {
                result = (HashMap<String, Integer>) objectInputStream.readObject();
            } catch (ClassCastException cce) {
                result = null;
            }
        } catch (IOException ioe) {
            System.out.println("Error reading file: " + filename);
            System.out.println(ioe.getMessage());
            result = null;
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Invalid class in file: " + filename);
            result = null;
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException ioe) {
                    System.out.println("Error closing: " + filename);
                }
            }
        }
        return result;
    }




}
