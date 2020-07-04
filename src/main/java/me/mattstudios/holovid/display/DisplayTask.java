package me.mattstudios.holovid.display;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.hologram.HologramLine;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;

import java.util.List;

public abstract class DisplayTask implements Runnable {

    private final Holovid plugin;
    private final long frameDelay;
    private final boolean repeat;
    private long lastDisplayed;
    protected boolean running = true;
    protected int frameCounter;

    protected DisplayTask(final Holovid plugin, final boolean repeat, final int fps) {
        this.plugin = plugin;
        this.repeat = repeat;
        this.frameDelay = 1000 / fps;
    }

    @Override
    public void run() {
        while (running) {
            // Load the frame in
            final IChatBaseComponent[] frame = getCurrentFrame();
            if (frame == null) return;

            // Frame delay
            final long timeSinceLast = System.currentTimeMillis() - lastDisplayed;
            if (timeSinceLast < frameDelay) {
                try {
                    Thread.sleep(frameDelay - timeSinceLast);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
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
    }

    /**
     * @return number of frames of the video
     */
    public abstract int getMaxFrames();

    protected abstract IChatBaseComponent[] getCurrentFrame();

    public void stop() {
        this.running = false;
    }
}
