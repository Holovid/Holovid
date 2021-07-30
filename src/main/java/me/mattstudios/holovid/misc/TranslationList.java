package me.mattstudios.holovid.misc;

import java.util.ArrayList;
import java.util.List;

public class TranslationList {
    private final List<Translation> translations = new ArrayList<>();

    public TranslationList() {
    }

    public void add(final Translation translation) {
        this.translations.add(translation);
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    @Override
    public String toString() {
        return "TranslationList{" +
                "translations=" + translations +
                '}';
    }
}
