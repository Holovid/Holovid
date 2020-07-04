package me.mattstudios.holovid.display;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.hologram.HologramLine;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DisplayTask implements Runnable {

    private final Holovid plugin;
    private final long frameDelay;
    private final boolean repeat;
    private long lastDisplayed;
    protected int frameCounter;

    private Lock runningInfoLock = new ReentrantLock();
    private Thread runningThread = null;
    private boolean deadBeforeStarted = false;

    protected DisplayTask(final Holovid plugin, final boolean repeat, final int fps) {
        this.plugin = plugin;
        this.repeat = repeat;
        this.frameDelay = 1000 / fps;
    }

    @Override
    public void run() {
        this.runningInfoLock.lock();
        if (deadBeforeStarted)
            return;

        this.runningThread = Thread.currentThread();
        this.runningInfoLock.unlock();


        try {
            prerun();
        } catch (InterruptedException e) {
            // We were stopped during the prerun, so just exit now.
            return;
        }

        do {
            try {
                runCycle();
            } catch (InterruptedException e) {
                return;
            }
        } while (!Thread.interrupted());
    }

    protected void prerun() throws InterruptedException {}

    private void runCycle() throws InterruptedException {
        // Load the frame in
        final IChatBaseComponent[] frame = getCurrentFrame();
        if (frame == null) return;

        // Frame delay
        final long timeSinceLast = System.currentTimeMillis() - lastDisplayed;
        if (timeSinceLast < frameDelay) {
            Thread.sleep(frameDelay - timeSinceLast);
        }

        // Set hologram lines
        final List<HologramLine> lines = plugin.getHologram().getLines();
        for (int i = 0; i < frame.length; i++) {
            final IChatBaseComponent line = frame[i];
            final HologramLine hologramLine = lines.get(i);
            hologramLine.updateText(line);
        }

        if (++frameCounter == getMaxFrames()) {
            if (!repeat) {
                plugin.stopTask();
                return;
            }

            frameCounter = 0;
        }

        lastDisplayed = System.currentTimeMillis();
    }

    /**
     * @return number of frames of the video
     */
    public abstract int getMaxFrames();

    protected abstract IChatBaseComponent[] getCurrentFrame() throws InterruptedException;

    public void stop() {
        // This should only actually block for any period of time when the task is initially starting
        this.runningInfoLock.lock();
        // Set this regardless of whether runningThread has been set yet - it doesn't matter.
        deadBeforeStarted = true;
        if (this.runningThread != null) {
            this.runningThread.interrupt();
        }
        this.runningInfoLock.unlock();
    }
}
