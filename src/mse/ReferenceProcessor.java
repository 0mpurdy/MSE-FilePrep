package mse;

import mse.common.Author;
import mse.common.AuthorIndex;
import mse.common.Config;

/**
 * Created by mj_pu_000 on 10/09/2015.
 */
public class ReferenceProcessor extends Thread {

    private ReferenceQueue tokenQueue;
    private AuthorIndex authorIndex;
    private Config cfg;
    private Author author;

    public ReferenceProcessor(ReferenceQueue tokenQueue){
        this.tokenQueue = tokenQueue;
        this.author = tokenQueue.getAuthor();
        this.authorIndex = new AuthorIndex(author);
        this.cfg = tokenQueue.getConfig();
    }

    @Override
    public void run() {
        while (!(isInterrupted() && tokenQueue.isEmpty())) {
            if (!tokenQueue.isEmpty()) {
                ReferenceQueueItem nextItem = tokenQueue.remove();
                authorIndex.incrementTokenCount(nextItem.getToken(), nextItem.getVolumeNumber(), nextItem.getPageNumber());
            }
        }

        // clean up the index arrays
        authorIndex.cleanIndexArrays();

        // testing output
        /*// write tokenCountMapJson
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String tokenCountMapJson = gson.toJson(authorIndex.getTokenCountMap());
            File outputFile = new File(cfg.getResDir() + author.getTargetPath("index-" + author.getCode() + "-tokenCount-json.idx"));
            PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
            pw.println(tokenCountMapJson);
        } catch (IOException ioe) {
            System.out.println("\nError writing token count for " + author.getName());
            System.out.println(ioe.getMessage());
        }

        // write json author index
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String authorIndexJson = gson.toJson(authorIndex);
            File outputFile = new File(cfg.getResDir() + author.getTargetPath("index-" + author.getCode() + "-json.idx"));
            PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
            pw.println(authorIndexJson);
        } catch (IOException ioe) {
            System.out.println("\nError writing json index for " + author.getName());
            System.out.println(ioe.getMessage());
        }*/

        // output index
        authorIndex.writeIndex(author.getIndexFilePath());

    }
}
