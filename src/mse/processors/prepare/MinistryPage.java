package mse.processors.prepare;

/**
 * Created by michaelpurdy on 08/02/2016.
 */
public class MinistryPage {

    boolean startedItalics;

    public MinistryPage() {
        reset();
    }

    public void reset() {
        startedItalics = false;
    }

    public void invertItalics() {
        this.startedItalics = !startedItalics;
    }

    public boolean getItalics() {
        return startedItalics;
    }
}
