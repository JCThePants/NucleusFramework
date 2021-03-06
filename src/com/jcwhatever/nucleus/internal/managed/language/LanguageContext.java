/*
 * This file is part of NucleusFramework for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.nucleus.internal.managed.language;

import com.jcwhatever.nucleus.internal.NucMsg;
import com.jcwhatever.nucleus.managed.language.ILanguageContext;
import com.jcwhatever.nucleus.managed.language.Localized;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.nucleus.utils.text.components.IChatMessage;
import com.jcwhatever.nucleus.utils.text.format.TextFormatterSettings;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal implementation of {@link ILanguageContext}.
 */
class LanguageContext implements ILanguageContext {

    private final Plugin _plugin;
    private final Object _owner;
    private final TextFormatterSettings _formatSettings;

    private Map<String, String> _localizationMap;
    private LanguageKeys _keys;

    /**
     * Constructor.
     *
     * @param plugin  The owning plugin.
     */
    public LanguageContext(Plugin plugin) {
        this(plugin, null);
    }

    /**
     * Constructor.
     *
     * @param plugin   The owning plugin.
     * @param context  Optional. An object instance from the jar file that owns the language context.
     *                 Otherwise, the plugin is the owner. Use when a plugin loads from multiple jar
     *                 files (modules) and a jar has its own language manager.
     */
    public LanguageContext(Plugin plugin, @Nullable Object context) {
        PreCon.notNull(plugin);

        _plugin = plugin;
        _owner = context == null ? plugin : context;
        _formatSettings = TextUtils.getPluginFormatters(plugin, null);

        loadInternalLanguage();
    }

    @Override
    public Plugin getPlugin() {
        return _plugin;
    }

    @Override
    public void clear() {
        _localizationMap.clear();
    }

    @Override
    public void reload() {
        _localizationMap.clear();

        loadInternalLanguage();
    }

    @Override
    public boolean addFile(File file) throws FileNotFoundException {
        PreCon.notNull(file);
        PreCon.isValid(file.isFile());

        FileInputStream stream = new FileInputStream(file);

        Language language = new Language(stream);

        return mergeLanguage(language);
    }

    @Override
    @Localized
    public IChatMessage get(CharSequence text, Object... params) {
        PreCon.notNull(text);

        if (_keys == null) {
            return format(text, params);
        }

        String localizedText = _localizationMap.get(text.toString());
        if (localizedText == null) {
            return format(text, params);
        }

        return format(localizedText, params);
    }

    /*
     * Load language key and file from owner jar resource file, if any.
     */
    private void loadInternalLanguage() {

        InputStream langStream = _owner.getClass().getResourceAsStream("/res/lang.txt");
        if (langStream == null)
            return;

        Language language = new Language(langStream);

        mergeLanguage(language);
    }

    /*
     * Parse language file from stream.
     */
    @Nullable
    private LanguageParser parseLanguage(InputStream stream) {

        LanguageParser langParser = new LanguageParser(stream);

        try {
            langParser.parseStream();

            return langParser;
        }
        catch (InvalidLocalizedTextException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     *  Get or load language keys from resource file.
     */
    @Nullable
    private LanguageKeys getLanguageKeys() {

        if (_keys != null)
            return _keys;

        InputStream langStream = _owner.getClass().getResourceAsStream("/res/lang.keys.txt");
        if (langStream == null)
            return null;

        return _keys = new LanguageKeys(langStream);
    }

    /*
     *  Merge a language into the language manager.
     *  The new language entries overwrite the existing.
     */
    private boolean mergeLanguage(Language language) {

        LanguageKeys keys = getLanguageKeys();
        if (keys == null)
            return false;

        if (!keys.isCompatible(language)) {
            NucMsg.warning(_plugin, "Could not merge language file due to incompatible version.");
            return false;
        }

        _localizationMap = new HashMap<>(keys.size());

        List<LocalizedText> localized = language.getLocalizedText();

        for (LocalizedText text : localized) {

            String key = keys.getText(text.getIndex());
            if (key == null) {
                NucMsg.warning(_plugin, "Failed to find localization key indexed {0}.", text.getIndex());
                continue;
            }

            _localizationMap.put(key, text.getText());
        }

        return true;
    }

    /*
     * text format helper
     */
    private IChatMessage format(CharSequence text, Object... params) {
        return TextUtils.format(_formatSettings, text, params);
    }
}
