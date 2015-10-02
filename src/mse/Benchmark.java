package mse;

/**
 * Created by mj_pu_000 on 10/09/2015.
 */
public class Benchmark implements Runnable {

    static String[] deleteChars = {"?","\"","!",",",".","-","\'",":",
            "1","2","3","4","5","6","7","8","9","0",";","@",")","(","¦","*","[","]","\u00AC","{","}","\u2019", "~",
            "\u201D","°","…","†","&","`","$","§","|","\t","=","+","‘","€","/","¶","_","–","½","£","“","%","#"};

    @Override
    public void run() {

        long startTime = System.nanoTime();

        for (int i = 0; i<10000000; i++){
            String token = "spirit's".toUpperCase();
            if (!isAlpha(token)) {
                token = processString(token);
            }
            if (!isAlpha(token)) {
                token = processUncommonString(token);
                if (!isAlpha(token)) {
//                    if (noErrors) {
//                        noErrors = !noErrors;
//                        System.out.println();
//                    }
//                    System.out.print("\t" + token + "\t" + volumeNumber + ":" + pageNumber);
//                    token = "";
                }
            }
        }

        long endTime = System.nanoTime();

        System.out.println("Time: " + ((endTime - startTime) / 1000000));

    }

    private static boolean isAlpha(String token) {
        char[] chars = token.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    private static String processString(String token) {
        for (String c : deleteChars) {
            if (token.contains(c)) {
                token = token.replace(c, "");
                return token;
            }
        }
        return token;
    }

    private static String processUncommonString(String token) {
        for (String c : deleteChars) {
            token = token.replace(c, "");
        }
        return token;
    }
}
