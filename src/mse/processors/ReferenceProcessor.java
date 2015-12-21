package mse.processors;

import mse.common.Author;
import mse.common.AuthorIndex;
import mse.common.Config;

/**
 * Created by mjp on 10/09/2015.
 */
public class ReferenceProcessor extends Thread {

    private ReferenceQueue tokenQueue;
    private AuthorIndex authorIndex;
    private Config cfg;
    private Author author;
    private long totalTokenCount = 0;

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
                authorIndex.incrementTokenCount(nextItem.getToken(), nextItem.volumeNumber, nextItem.pageNumber);
                totalTokenCount++;
            }
        }

        // clean up the index arrays
        authorIndex.cleanIndexArrays();

        // output index
        authorIndex.writeIndex(cfg.getResDir() + author.getIndexFilePath());

        System.out.println("Total token count: " + totalTokenCount);

    }
}
