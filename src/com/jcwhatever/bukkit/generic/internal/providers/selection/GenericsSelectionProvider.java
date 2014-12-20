/*
 * This file is part of GenericsLib for Bukkit, licensed under the MIT License (MIT).
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

package com.jcwhatever.bukkit.generic.internal.providers.selection;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.internal.Lang;
import com.jcwhatever.bukkit.generic.internal.Msg;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.mixins.IDisposable;
import com.jcwhatever.bukkit.generic.player.collections.PlayerMap;
import com.jcwhatever.bukkit.generic.providers.IRegionSelectProvider;
import com.jcwhatever.bukkit.generic.regions.selection.IRegionSelection;
import com.jcwhatever.bukkit.generic.regions.selection.RegionSelection;
import com.jcwhatever.bukkit.generic.utils.LocationUtils;
import com.jcwhatever.bukkit.generic.utils.PreCon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * GenericsLib's default selection provider when no other
 * provider is available.
 */
public final class GenericsSelectionProvider implements IRegionSelectProvider, IDisposable{

    @Localizable static final String _P1_SELECTED =
            "{DARK_PURPLE}P1 region point selected:\n{DARK_PURPLE}{0: location}";

    @Localizable static final String _P2_SELECTED =
            "{DARK_PURPLE}P2 region point selected:\n{DARK_PURPLE}{0: location}";

    private final Map<UUID, Location> _p1Selections = new PlayerMap<>(GenericsLib.getPlugin());
    private final Map<UUID, Location> _p2Selections = new PlayerMap<>(GenericsLib.getPlugin());
    private final BukkitEventListener _listener;

    private boolean _isDisposed;

    public GenericsSelectionProvider() {
        _listener = new BukkitEventListener();
        Bukkit.getPluginManager().registerEvents(_listener, GenericsLib.getPlugin());
    }

    @Override
    public String getName() {
        return "GenericsRegionSelector";
    }

    @Override
    public String getVersion() {
        return GenericsLib.getPlugin().getDescription().getVersion();
    }

    @Override
    public int getLogicalVersion() {
        return 0;
    }

    @Nullable
    @Override
    public IRegionSelection getSelection(Player player) {
        PreCon.notNull(player);

        Location p1 = _p1Selections.get(player.getUniqueId());
        if (p1 == null)
            return null;

        Location p2 = _p2Selections.get(player.getUniqueId());
        if (p2 == null)
            return null;

        return new RegionSelection(p1.clone(), p2.clone());
    }

    @Override
    public boolean setSelection(Player player, IRegionSelection selection) {
        PreCon.notNull(player);
        PreCon.notNull(selection);

        if (!selection.isDefined())
            return false;

        _p1Selections.put(player.getUniqueId(), selection.getP1().clone());
        _p2Selections.put(player.getUniqueId(), selection.getP2().clone());

        return true;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {
        _isDisposed = true;
        HandlerList.unregisterAll(_listener);
    }

    private class BukkitEventListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        private void onPlayerInteract(PlayerInteractEvent event) {
            if (!event.hasBlock())
                return;

            Player player = event.getPlayer();

            if (!hasPermission(player))
                return;

            ItemStack item = player.getInventory().getItemInHand();
            if (item == null)
                return;

            if (item.getType() != Material.WOOD_AXE)
                return;

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location p1 = event.getClickedBlock().getLocation();

                Location previous = _p1Selections.put(player.getUniqueId(), p1);

                if (!p1.equals(previous)) {
                    Msg.tell(player, Lang.get(_P1_SELECTED, LocationUtils.locationToString(p1, 2)));
                }
                event.setCancelled(true);
            }
            else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Location p2 = event.getClickedBlock().getLocation();

                Location previous = _p2Selections.put(player.getUniqueId(), p2);

                if (!p2.equals(previous)) {
                    Msg.tell(player, Lang.get(_P2_SELECTED, LocationUtils.locationToString(p2, 2)));
                }
                event.setCancelled(true);
            }
        }

        @EventHandler
        private void onCommand(PlayerCommandPreprocessEvent event) {
            if (event.getMessage().equals("//wand")) {
                event.getPlayer().getInventory().addItem(new ItemStack(Material.WOOD_AXE));
                event.setCancelled(true);
            }
        }
    }

    private boolean hasPermission(Player player) {
        return player.isOp();
    }
}
