package me.mattstudios.holovid.display;

import me.mattstudios.holovid.Holovid;
import net.minecraft.server.v1_16_R1.ChatBaseComponent;
import net.minecraft.server.v1_16_R1.ChatComponentText;
import net.minecraft.server.v1_16_R1.ChatHexColor;
import net.minecraft.server.v1_16_R1.ChatModifier;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public final class BufferedDisplayTask extends DisplayTask {

    private final Queue<int[][]> frames;
    private final int bufferCapacity;
    private final long startDelay;
    private final int max;

    public BufferedDisplayTask(final Holovid plugin, final long startDelay, final boolean repeat, final int max, final int fps) {
        super(plugin, repeat, fps);
        this.startDelay = startDelay;
        this.max = max;

        // Buffer up to 30 seconds beforehand
        this.bufferCapacity = 30 * fps;
        this.frames = new ArrayBlockingQueue<>(bufferCapacity);
    }

    @Override
    public void run() {
        // Give the processing a headstart
        if (startDelay > 0) {
            try {
                Thread.sleep(startDelay);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        super.run();
    }

    @Override
    public int getMaxFrames() {
        return max;
    }

    @Override
    protected IChatBaseComponent[] getCurrentFrame() {
        // Block until the frame is processed
        int[][] frame;
        do {
            frame = frames.poll();
        } while (frame == null && running);

        if (frame == null) return null;

        // Convert to json component
        final IChatBaseComponent[] frameText = new IChatBaseComponent[frame.length];
        for (int i = 0; i < frame.length; i++) {
            frameText[i] = dataToComponent(frame[i]);
        }
        return frameText;
    }

    @Override
    public void stop() {
        super.stop();
        frames.clear();
    }

    public Queue<int[][]> getFrameQueue() {
        return frames;
    }

    public boolean isQueueFull() {
        return frames.size() >= bufferCapacity;
    }

    private IChatBaseComponent dataToComponent(final int[] row) {
        final ChatBaseComponent component = new ChatComponentText("");
        for (final int rgb : row) {
            final ChatComponentText text = new ChatComponentText("█");
            text.setChatModifier(ChatModifier.b.setColor(ChatHexColor.a(rgb & 0x00FFFFFF)));
            component.addSibling(text);
        }
        return component;
    }
}
