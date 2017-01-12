package mse.menu;

import mse.common.Author;

import java.util.ArrayList;

/**
 * Created by Michael Purdy on 27/10/2016.
 */
public class MenuPrinter {

    public static final String[] mainMenuOptions = {
            "Close",
            "Prepare all files",
            "Prepare single author",
            "Create all indexes",
            "Create single author index",
            "Create super index",
            "Check author index",
            "Check all author indexes",
            "Other"
    };

    public static final String[] otherMainMenuOptions = {
            "Back",
            "Benchmark",
    };

    public static void printMenu(ArrayList<String> menu) {
        int i = 0;
        for (String option : menu) {
            System.out.println(i + " - " + option);
            i++;
        }
        System.out.print("Choose an option: ");
    }

    public static void printMenu(String[] menu) {

        int i = 0;
        for (String option : menu) {
            System.out.println(i + " - " + option);
            i++;
        }
        System.out.print("Choose an option: ");

    }

    public static void printAuthorMenu() {
        System.out.println("Authors: ");
        int i = 0;
        for (Author nextAuthor : Author.values()) {
            System.out.println(i + " - " + nextAuthor.getName());
            i++;
        }
        System.out.print("Choose an option: ");
    }

}
