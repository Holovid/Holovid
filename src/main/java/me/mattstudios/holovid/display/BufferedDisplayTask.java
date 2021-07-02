package me.mattstudios.holovid.display;

import me.mattstudios.holovid.Holovid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public final class BufferedDisplayTask extends DisplayTask {

    private final long startDelay;
    private final int max;

    public BufferedDisplayTask(final Holovid plugin, final long startDelay, final boolean repeat, final int max, final int fps, final boolean interlace) {
        super(plugin, repeat, fps, interlace);
        this.startDelay = startDelay;
        this.max = max;
    }

    @Override
    protected void prerun() throws InterruptedException {
        // Give the processing a headstart
        if (startDelay > 0) {
            Thread.sleep(startDelay);
        }
    }

    @Override
    public int getMaxFrames() {
        return max;
    }

    @Override
    protected Component[] getCurrentFrame() throws InterruptedException {
        // Block until the frame is processed
        final int[][] frame = plugin.getVideoProcessor().getFrameQueue().take();

        // Convert to json component
        final Component[] frameText = new Component[interlace ? frame.length / 2 : frame.length];
        if (interlace) {
            for (int y = oddFrame ? 1 : 0; y < frame.length; y += 2) {
                frameText[y / 2] = dataToComponent(frame[y]);
            }
        } else {
            for (int i = 0; i < frame.length; i++) {
                frameText[i] = dataToComponent(frame[i]);
            }
        }
        return frameText;
    }

    private Component dataToComponent(final int[] row) {
        final TextComponent.Builder component = Component.text();
        TextComponent.Builder lastComponent = null;
        int lastRgb = 0xFFFFFF;

        for (int rgb : row) {
            rgb &= 0x00FFFFFF;
            lastComponent = appendComponent(component, lastComponent, rgb, lastRgb);
            lastRgb = rgb;
        }

        if (lastComponent != null){
            component.append(lastComponent); // Append last component as there are no more pixels to process
        }

        return component.build();
    }
}
