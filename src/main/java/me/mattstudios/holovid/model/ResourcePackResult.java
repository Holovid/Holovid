package me.mattstudios.holovid.model;

public final class ResourcePackResult {

    private final String url;
    private final String hash;

    public ResourcePackResult(final String url, final String hash) {
        this.url = url;
        this.hash = hash;
    }

    public String getUrl() {
        return url;
    }

    public String getHash() {
        return hash;
    }
}
