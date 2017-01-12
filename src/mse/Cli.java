package mse;

import mse.common.Config;
import mse.data.PreparePlatform;
import mse.menu.MenuAction;
import mse.menu.MenuPrinter;

import java.util.Scanner;

/**
 * Created by michaelpurdy on 27/10/2016.
 */
public class Cli {

    protected void start(Config cfg) {
        System.out.println("MSE File Prep console application");
        System.out.println("Version: " + cfg.getMseVersion());

        showMenu(cfg);

    }

    private void showMenu(Config cfg) {

        int mainMenuChoice = -1;

        Scanner sc = new Scanner(System.in);

        while (mainMenuChoice != 0) {
            MenuPrinter.printMenu(MenuPrinter.otherMainMenuOptions);

            mainMenuChoice = sc.nextInt();
            sc.nextLine();

            MenuAction.executeMenuChoice(mainMenuChoice, sc, cfg);
        }

    }

}
