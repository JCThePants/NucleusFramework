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

package com.jcwhatever.nucleus.collections.players;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

/**
 * A {@code SetMultimap} wrapper that stores elements using the player ID as key.
 *
 * <p> When the player logs out, the entry is automatically removed.</p>
 *
 * @param <V>  The value type
 */
public class PlayerSetMultimap<V> extends PlayerMultimap<V> {

    private final Object _sync = new Object();

    /**
     * Constructor.
     *
     * @param plugin   The owning plugin.
     */
    public PlayerSetMultimap(Plugin plugin, int keySize, int valueSize) {
        //noinspection unchecked
        super(plugin, (SetMultimap)MultimapBuilder.hashKeys(keySize).hashSetValues(valueSize).build());
    }

    @Override
    public Object getSync() {
        return _sync;
    }

    @Override
    public Set<V> get(@Nonnull UUID playerId) {
        return (Set<V>) super.get(playerId);
    }

    @Override
    public Set<V> removeAll(@Nonnull Object playerId) {
        return (Set<V>) super.removeAll(playerId);
    }

    @Override
    public Set<V> replaceValues(UUID playerId, Iterable<? extends V> iterable) {
        return (Set<V>)super.replaceValues(playerId, iterable);
    }
}
