package me.mattstudios.holovid.display;

public final class TaskInfo {

    private final int frames;
    private final int height;
    private final int fps;
    private final boolean interlacing;

    public TaskInfo(final int frames, final int height, final int fps, final boolean interlacing) {
        this.frames = frames;
        this.height = height;
        this.fps = fps;
        this.interlacing = interlacing;
    }

    public int getFrames() {
        return frames;
    }

    public int getHeight() {
        return height;
    }

    public int getFps() {
        return fps;
    }

    public boolean interlacing() {
        return interlacing;
    }
}
