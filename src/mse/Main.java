package mse;

import mse.common.Config;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Cli cli = new Cli();

        cli.start(new Config());

    }

}
