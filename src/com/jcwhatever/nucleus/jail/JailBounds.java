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


package com.jcwhatever.nucleus.jail;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.regions.Region;
import com.jcwhatever.nucleus.regions.options.LeaveRegionReason;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * A region that represents the boundaries of a jail.
 */
public class JailBounds extends Region {

    private Jail _jail;

    /**
     * Constructor.
     *
     * @param plugin       The owning plugin.
     * @param jail         The owning jail.
     * @param name         The name of the region.
     * @param settings     The region data node.
     */
    JailBounds(Plugin plugin, Jail jail, String name, IDataNode settings) {
        super(plugin, name, settings);

        PreCon.notNull(jail);

        _jail = jail;
        setEventListener(true);
    }

    /**
     * Determine if the {@link #onPlayerLeave} method can be called.
     *
     * @param player  The player leaving the region.
     */
    @Override
    protected boolean canDoPlayerLeave(Player player, LeaveRegionReason reason) {
        PreCon.notNull(player);

        return reason != LeaveRegionReason.QUIT_SERVER && _jail.isPrisoner(player);
    }

    /**
     * Prevent imprisoned players from leaving the jail region.
     *
     * @param player  The player leaving the region.
     */
    @Override
    protected void onPlayerLeave (final Player player, LeaveRegionReason reason) {
        PreCon.notNull(player);

        Scheduler.runTaskLater(Nucleus.getPlugin(), 10, new Runnable() {
            @Override
            public void run() {

                // prevent player from leaving jail
                Location tpLocation = _jail.getRandomTeleport();

                if (tpLocation == null)
                    tpLocation = JailBounds.this.getCenter();

                player.teleport(tpLocation);
            }
        });

    }
}
