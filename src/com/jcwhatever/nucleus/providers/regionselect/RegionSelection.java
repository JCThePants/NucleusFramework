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

package com.jcwhatever.nucleus.providers.regionselect;

import com.jcwhatever.nucleus.Nucleus;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/**
 * Static convenience methods for accessing the region selection provider.
 */
public final class RegionSelection {

    private RegionSelection() {}

    /**
     * Get the specified players current region selection.
     *
     * @param player  The player to check.
     *
     * @return  Null if the player does not have a selected region.
     */
    @Nullable
    public static IRegionSelection get(Player player) {
        return Nucleus.getProviders().getRegionSelection().getSelection(player);
    }

    /**
     * Set the specified players region selection.
     *
     * @param player     The player.
     * @param selection  The new selection to set.
     *
     * @return  True if the players region selection was set.
     */
    public static boolean set(Player player, IRegionSelection selection) {
        return Nucleus.getProviders().getRegionSelection().setSelection(player, selection);
    }
}
