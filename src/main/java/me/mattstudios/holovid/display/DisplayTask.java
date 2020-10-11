package me.mattstudios.holovid.display;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.hologram.HologramLine;
import net.minecraft.server.v1_16_R2.ChatBaseComponent;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.ChatHexColor;
import net.minecraft.server.v1_16_R2.ChatModifier;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DisplayTask implements Runnable {

    protected final Holovid plugin;
    private final long frameDelayNanos;
    private final boolean repeat;
    protected final boolean interlace;

    private long nextDisplayNanos;
    protected boolean oddFrame;
    protected int frameCounter;

    private final Lock runningInfoLock = new ReentrantLock();
    private Thread runningThread;
    private boolean deadBeforeStarted;

    protected DisplayTask(final Holovid plugin, final boolean repeat, final int fps, final boolean interlace) {
        this.plugin = plugin;
        this.repeat = repeat;
        // 1000ms to nanos
        this.frameDelayNanos = (1000L * 1_000_000L) / fps;
        this.interlace = interlace;
    }

    @Override
    public void run() {
        this.runningInfoLock.lock();
        if (deadBeforeStarted) {
            return;
        }

        this.runningThread = Thread.currentThread();
        this.runningInfoLock.unlock();

        try {
            prerun();
        } catch (final InterruptedException e) {
            // We were stopped during the prerun, so just exit now.
            return;
        }

        nextDisplayNanos = System.nanoTime();

        do {
            try {
                runCycle();
            } catch (final InterruptedException e) {
                return;
            }
        } while (!Thread.interrupted());
    }

    protected void prerun() throws InterruptedException {
    }

    private void runCycle() throws InterruptedException {
        // Load the frame in
        final IChatBaseComponent[] frame = getCurrentFrame();
        if (frame == null) return;

        // Frame delay
        final long nanoTime = System.nanoTime();
        final long delay = nextDisplayNanos - nanoTime;
        if (delay > 0) {
            final long delayMillis = delay / 1_000_000;
            Thread.sleep(delayMillis, (int) (delay % 1_000_000));
        }

        // Try to keep this in perfect sync
        nextDisplayNanos += frameDelayNanos;

        // Set hologram lines
        final List<HologramLine> lines = plugin.getHologram().getLines();
        if (interlace) {
            final int x = oddFrame ? 1 : 0;
            for (int i = 0; i < frame.length; i++) {
                final IChatBaseComponent line = frame[i];
                final HologramLine hologramLine = lines.get(x + (i * 2));

                hologramLine.updateText(line);
            }

            oddFrame = !oddFrame;
        } else {
            for (int i = 0; i < frame.length; i++) {
                final IChatBaseComponent line = frame[i];
                final HologramLine hologramLine = lines.get(i);
                hologramLine.updateText(line);
            }
        }

        if (++frameCounter == getMaxFrames()) {
            if (!repeat) {
                plugin.stopDisplayTask();
                return;
            }

            frameCounter = 0;
        }
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

    protected ChatComponentText appendComponent(final ChatBaseComponent parent, final int rgb, final int lastRgb, final ChatComponentText lastComponent) {
        final ChatComponentText component;
        if (lastComponent != null && rgb == lastRgb) {
            // Add the character to the last component (with the same color)
            component = new ChatComponentText(lastComponent.h() + "█");
            component.setChatModifier(lastComponent.getChatModifier());

            // Replace old component
            parent.getSiblings().set(parent.getSiblings().size() - 1, component);
        } else {
            // Add a new component
            component = new ChatComponentText("█");
            component.setChatModifier(ChatModifier.a.setColor(ChatHexColor.a(rgb)));
            parent.addSibling(component);
        }
        return component;
    }
}
