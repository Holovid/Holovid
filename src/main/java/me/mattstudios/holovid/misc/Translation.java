package me.mattstudios.holovid.misc;


public class Translation {
    private final String start;
    private final String dur;
    private final String content;

    public Translation(String start, String dur, String content) {
        this.start = start;
        this.dur = dur;
        this.content = content;
    }

    public String getStart() {
        return start;
    }

    public String getDur() {
        return dur;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "start='" + start + '\'' +
                ", dur='" + dur + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
