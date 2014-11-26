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


package com.jcwhatever.bukkit.generic.regions;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.collections.EntryCounter;
import com.jcwhatever.bukkit.generic.collections.EntryCounter.RemovalPolicy;
import com.jcwhatever.bukkit.generic.messaging.Messenger;
import com.jcwhatever.bukkit.generic.player.collections.PlayerMap;
import com.jcwhatever.bukkit.generic.regions.Region.EnterRegionReason;
import com.jcwhatever.bukkit.generic.regions.Region.LeaveRegionReason;
import com.jcwhatever.bukkit.generic.regions.Region.PriorityType;
import com.jcwhatever.bukkit.generic.regions.Region.RegionReason;
import com.jcwhatever.bukkit.generic.regions.data.OrderedRegions;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.Scheduler;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Global Region Manager
 */
public class RegionManager {

    // Player watcher regions chunk map. String key is chunk coordinates. Set<> value is OrderedRegions<> instance.
    private final Map<String, Set<IRegion>> _listenerRegionsMap = new HashMap<>(500);

    // All regions chunk map. String key is chunk coordinates
    private final Map<String, Set<IRegion>> _allRegionsMap = new HashMap<>(500);

    // worlds that have regions
    private EntryCounter<World> _listenerWorlds = new EntryCounter<>(RemovalPolicy.REMOVE);

    // cached regions the player was detected in in last player watcher cycle. Set<> value is OrderedRegions<> instance.
    private Map<UUID, Set<IRegion>> _playerCacheMap;

    // locations the player was detected in between player watcher cycles.
    private Map<UUID, LinkedList<CachedLocation>> _playerLocationCache;

    // hash set of all registered regions
    private Set<IRegion> _regions = new HashSet<>(500);

    // synchronization object
    private final Object _sync = new Object();

    /**
     * Constructor. Used by GenericsLib to initialize RegionEventManager.
     *
     * <p>Not meant for public instantiation. For internal use only.</p>
     */
    public RegionManager(Plugin plugin) {

        if (!(plugin instanceof GenericsLib)) {
            throw new RuntimeException("RegionManager is for GenericsLib internal use only.");
        }

        _playerCacheMap = new PlayerMap<>(plugin);
        _playerLocationCache = new PlayerMap<>(plugin);
        Scheduler.runTaskRepeat(plugin,  3, 3, new PlayerWatcher(this));
    }

    /**
     * Get number of regions registered.
     */
    public int getRegionCount() {
        return _regions.size();
    }

    /**
     * Get a set of regions that contain the specified location.
     *
     * @param location  The location to check.
     */
    public List<IRegion> getRegions(Location location) {
        PreCon.notNull(location);

        return getRegion(location, PriorityType.ENTER, _allRegionsMap);
    }

    /**
     * Get a set of regions that the specified location
     * is inside of and are player watchers/listeners.
     *
     * @param location  The location to check.
     */
    public List<IRegion> getListenerRegions(Location location) {
        return getRegion(location, PriorityType.ENTER, _listenerRegionsMap);
    }

    /**
     * Get a set of regions that the specified location
     * is inside of and are player watchers/listeners.
     *
     * @param location      The location to check.
     * @param priorityType  The priority sorting type of the returned list.
     */
    public List<IRegion> getListenerRegions(Location location, PriorityType priorityType) {
        return getRegion(location, priorityType, _listenerRegionsMap);
    }

    /**
     * Get all regions that intersect with the specified chunk.
     *
     * @param chunk  The chunk to check.
     */
    public Set<IRegion> getRegionsInChunk(Chunk chunk) {
        synchronized(_sync) {

            if (getRegionCount() == 0)
                return new HashSet<>(0);

            String key = getChunkKey(chunk.getWorld(), chunk.getX(), chunk.getZ());

            Set<IRegion> regions = _allRegionsMap.get(key);
            if (regions == null)
                return new HashSet<>(0);

            _sync.notifyAll();
            return new HashSet<>(regions);
        }
    }

    /**
     * Get all regions that player is currently in.
     *
     * @param p  The player to check.
     */
    public List<IRegion> getPlayerRegions(Player p) {
        PreCon.notNull(p);

        if (_playerCacheMap == null)
            return new ArrayList<>(0);

        synchronized(_sync) {
            Set<IRegion> regions = _playerCacheMap.get(p.getUniqueId());
            if (regions == null)
                return new ArrayList<>(0);

            _sync.notifyAll();
            return new ArrayList<>(regions);
        }
    }

    /**
     * Add a location that the player has moved to so it can be
     * cached and processed by the player watcher the next time it
     * runs.
     *
     * @param p         The player.
     * @param location  The location to cache.
     * @param reason    The reason that will be used if the player enters a region.
     */
    public void updatePlayerLocation(Player p, Location location, RegionReason reason) {
        PreCon.notNull(p);
        PreCon.notNull(location);
        PreCon.notNull(reason);

        if (p.isDead())
            return;

        // ignore NPC's
        if (p.hasMetadata("NPC"))
            return;

        LinkedList<CachedLocation> locations = getPlayerLocations(p.getUniqueId());
        locations.add(new CachedLocation(location, reason));
    }

    /**
     * Update player location when the player does not have a location.
     *
     * <p>Declares the player as leaving all current regions.</p>
     *
     * @param p       The player.
     * @param reason  The reason the player is leaving the regions.
     */
    public void updatePlayerLocation(Player p, LeaveRegionReason reason) {

        synchronized (_sync) {

            OrderedRegions<IRegion> regions =
                    (OrderedRegions<IRegion>) _playerCacheMap.remove(p.getUniqueId());

            if (regions == null)
                return;

            Iterator<IRegion> iterator = regions.iterator(PriorityType.LEAVE);
            while (iterator.hasNext()) {
                IRegion region = iterator.next();

                ((ReadOnlyRegion)region).getHandle().doPlayerLeave(p, reason);
            }

            clearPlayerLocations(p.getUniqueId());
        }

    }

    /**
     * Causes a region to re-fire the onPlayerEnter event
     * if the player is already in it
     * .
     * @param p       The player.
     * @param region  The region.
     */
    public void resetPlayerRegion(Player p, Region region) {
        PreCon.notNull(p);
        PreCon.notNull(region);

        if (_playerCacheMap == null)
            return;

        synchronized(_sync) {

            Set<IRegion> regions = _playerCacheMap.get(p.getUniqueId());
            if (regions == null)
                return;

            regions.remove(new ReadOnlyRegion(region));
            _sync.notifyAll();
        }
    }

    /*
     * Get all regions contained in the specified location using
     * the supplied region map.
     */
    private List<IRegion> getRegion(Location location, PriorityType priorityType,
                                           Map<String, Set<IRegion>> map) {
        synchronized(_sync) {

            List<IRegion> results = new ArrayList<>(10);

            if (getRegionCount() == 0)
                return results;

            // calculate chunk location instead of getting it from chunk
            // to prevent asynchronous issues
            int chunkX = (int)Math.floor(location.getX() / 16);
            int chunkZ = (int)Math.floor(location.getZ() / 16);

            String key = getChunkKey(location.getWorld(), chunkX, chunkZ);

            Set<IRegion> regions = map.get(key);
            if (regions == null)
                return results;

            Iterator<IRegion> iterator;

            if (regions instanceof OrderedRegions) {
                OrderedRegions<IRegion> orderedRegions =
                        (OrderedRegions<IRegion>)regions;

                iterator = orderedRegions.iterator(priorityType);
            }
            else {
                iterator = regions.iterator();
            }

            while (iterator.hasNext()) {
                IRegion region = iterator.next();
                if (region.contains(location))
                    results.add(region);
            }

            _sync.notifyAll();
            return results;
        }
    }

    /**
     * Register a region so it can be found in searches
     * and its events called if it is a player watcher.
     *
     * @param region  The Region to register.
     */
    void register(Region region) {
        PreCon.notNull(region);

        if (!region.isDefined()) {
            Messenger.debug(GenericsLib.getLib(),
                    "Failed to register region '{0}' with RegionManager because " +
                            "it's coords are undefined.", region.getName());
            return;
        }

        ReadOnlyRegion readOnlyRegion = new ReadOnlyRegion(region);

        _regions.add(readOnlyRegion);

        synchronized(_sync) {

            int xMax = region.getChunkX() + region.getChunkXWidth();
            int zMax = region.getChunkZ() + region.getChunkZWidth();

            boolean hasRegion = false;

            for (int x= region.getChunkX(); x < xMax; x++) {
                for (int z= region.getChunkZ(); z < zMax; z++) {

                    //noinspection ConstantConditions
                    String key = getChunkKey(region.getWorld(), x, z);

                    if (region.isPlayerWatcher()) {
                        addToMap(_listenerRegionsMap, key, readOnlyRegion);
                    }
                    else {
                        hasRegion = removeFromMap(_listenerRegionsMap, key, readOnlyRegion);
                    }

                    addToMap(_allRegionsMap, key, readOnlyRegion);
                }
            }

            if (region.isPlayerWatcher()) {
                //noinspection ConstantConditions
                _listenerWorlds.add(region.getWorld());
            }
            else if (hasRegion){
                //noinspection ConstantConditions
                _listenerWorlds.subtract(region.getWorld());
            }

            _sync.notifyAll();
        }
    }

    /**
     * Unregister a region and its events completely.
     *
     * <p>Called when a region is disposed.</p>
     *
     * @param region  The Region to unregister.
     */
    void unregister(Region region) {
        PreCon.notNull(region);

        if (!region.isDefined())
            return;

        ReadOnlyRegion readOnlyRegion = new ReadOnlyRegion(region);

        synchronized(_sync) {

            int xMax = region.getChunkX() + region.getChunkXWidth();
            int zMax = region.getChunkZ() + region.getChunkZWidth();

            for (int x= region.getChunkX(); x < xMax; x++) {
                for (int z= region.getChunkZ(); z < zMax; z++) {

                    //noinspection ConstantConditions
                    String key = getChunkKey(region.getWorld(), x, z);

                    removeFromMap(_listenerRegionsMap, key, readOnlyRegion);
                    removeFromMap(_allRegionsMap, key, readOnlyRegion);
                }
            }

            if (_regions.remove(readOnlyRegion) && region.isPlayerWatcher()) {
                //noinspection ConstantConditions
                _listenerWorlds.subtract(region.getWorld());
            }

            _sync.notifyAll();
        }
    }

    /*
     * Get the cached movement locations of a player that have not been processed
      * by the PlayerWatcher task yet.
     */
    private LinkedList<CachedLocation> getPlayerLocations(UUID playerId) {

        LinkedList<CachedLocation> locations = _playerLocationCache.get(playerId);
        if (locations == null) {
            locations = new LinkedList<CachedLocation>();
            _playerLocationCache.put(playerId, locations);
        }

        return locations;
    }

    /*
     * Clear cached movement locations of a player
     */
    private void clearPlayerLocations(UUID playerId) {
        LinkedList<CachedLocation> locations = getPlayerLocations(playerId);
        locations.clear();
    }

    /*
     * Add a region to a region map.
     */
    private void addToMap(Map<String, Set<IRegion>> map, String key, ReadOnlyRegion region) {
        Set<IRegion> regions = map.get(key);
        if (regions == null) {
            regions = new OrderedRegions<>(5);
            map.put(key, regions);
        }
        regions.add(region);
    }

    /*
     * Remove a region from a region map.
     */
    private boolean removeFromMap(Map<String, Set<IRegion>> map, String key, ReadOnlyRegion region) {
        Set<IRegion> regions = map.get(key);
        return regions != null && regions.remove(region);
    }

    /*
     * Get a regions chunk map key.
     */
    private String getChunkKey(World world, int x, int z) {
        return world.getName() + '.' + String.valueOf(x) + '.' + String.valueOf(z);
    }

    /*
     * Repeating task that determines which regions need events fired.
     */
    private static final class PlayerWatcher implements Runnable {

        private RegionManager _manager;

        PlayerWatcher(RegionManager manager) {
            _manager = manager;
        }

        @Override
        public void run() {

            List<World> worlds = new ArrayList<World>(_manager._listenerWorlds.getEntries());

            final List<WorldPlayers> worldPlayers = new ArrayList<WorldPlayers>(worlds.size());

            // get players in worlds with regions
            for (World world : worlds) {

                if (world == null)
                    continue;

                List<Player> players = world.getPlayers();

                if (players == null || players.isEmpty())
                    continue;

                worldPlayers.add(new WorldPlayers(_manager, world, players));
            }

            // end if there are no players in region worlds
            if (worldPlayers.isEmpty())
                return;

            Scheduler.runTaskLaterAsync(GenericsLib.getLib(), 1, new PlayerWatcherAsync(_manager, worldPlayers));
        }
    }

    /*
     * Async portion of the player watcher
     */
    private static class PlayerWatcherAsync implements Runnable {

        private final Object _sync;
        private final Collection<WorldPlayers> _worldPlayers;
        private final Map<UUID, Set<IRegion>> _playerCacheMap;
        private final RegionManager _manager;

        PlayerWatcherAsync(RegionManager manager, Collection<WorldPlayers> worldPlayers) {
            _manager = manager;
            _sync = manager._sync;
            _worldPlayers = worldPlayers;
            _playerCacheMap = manager._playerCacheMap;
        }

        @Override
        public void run() {

            for (WorldPlayers wp : _worldPlayers) {

                synchronized (_sync) {

                    // get players in world
                    List<WorldPlayer> worldPlayers = wp.players;

                    // iterate players
                    for (WorldPlayer worldPlayer : worldPlayers) {

                        UUID playerId = worldPlayer.player.getUniqueId();

                        // get regions the player is in (cached from previous check)
                        OrderedRegions<IRegion> cachedRegions =
                                (OrderedRegions<IRegion>)_playerCacheMap.get(playerId);

                        if (cachedRegions == null) {
                            cachedRegions = new OrderedRegions<>(7);
                            _playerCacheMap.put(playerId, cachedRegions);
                        }

                        // iterate cached locations
                        while (!worldPlayer.locations.isEmpty()) {
                            CachedLocation location = worldPlayer.locations.removeFirst();

                            // see which regions a player actually is in
                            List<IRegion> inRegions = _manager.getListenerRegions(location, PriorityType.ENTER);

                            // check for entered regions
                            if (inRegions != null && !inRegions.isEmpty()) {
                                for (IRegion region : inRegions) {

                                    // check if player was not previously in region
                                    if (!cachedRegions.contains(region)) {

                                        onPlayerEnter(region, worldPlayer.player,
                                                location.getReason());

                                        cachedRegions.add(region);
                                    }
                                }
                            }

                            // check for regions player has left
                            if (!cachedRegions.isEmpty()) {
                                Iterator<IRegion> iterator = cachedRegions.iterator(PriorityType.LEAVE);
                                while(iterator.hasNext()) {
                                    IRegion region = iterator.next();

                                    // check if player was previously in region
                                    if (inRegions == null || !inRegions.contains(region)) {

                                        onPlayerLeave(region, worldPlayer.player,
                                                location.getReason());

                                        iterator.remove();
                                    }
                                }
                            }
                        }


                    } // END for (Player

                    _sync.notifyAll();

                } // END synchronized

            } // END for (WorldPlayers

        } // END run()

        /*
         * Executes doPlayerEnter method in the specified region on the main thread.
         */
        private void onPlayerEnter(final IRegion region, final Player p, RegionReason reason) {

            final EnterRegionReason enterReason = reason.getEnterReason();
            if (enterReason == null)
                throw new AssertionError();

            Scheduler.runTaskSync(GenericsLib.getLib(), new Runnable() {

                @Override
                public void run() {
                    ((ReadOnlyRegion)region).getHandle().doPlayerEnter(p, enterReason);
                }

            });
        }

        /*
         * Executes doPlayerLeave method in the specified region on the main thread.
         */
        private void onPlayerLeave(final IRegion region, final Player p, RegionReason reason) {

            final LeaveRegionReason leaveReason = reason.getLeaveReason();
            if (leaveReason == null)
                throw new AssertionError();

            Scheduler.runTaskSync(GenericsLib.getLib(), new Runnable() {

                @Override
                public void run() {
                    ((ReadOnlyRegion)region).getHandle().doPlayerLeave(p, leaveReason);
                }

            });
        }
    }

    /*
     * Stores a collection of players that are in a world.
     */
    private static class WorldPlayers {

        public final World world;
        public final List<WorldPlayer> players;

        public WorldPlayers (RegionManager manager, World world, List<Player> players) {
            this.world = world;

            List<WorldPlayer> worldPlayers = new ArrayList<WorldPlayer>(players.size());
            for (Player p : players) {

                LinkedList<CachedLocation> locations = manager.getPlayerLocations(p.getUniqueId());
                if (locations.isEmpty())
                    continue;

                WorldPlayer worldPlayer = new WorldPlayer(p, new LinkedList<CachedLocation>(locations));
                worldPlayers.add(worldPlayer);

                manager.clearPlayerLocations(p.getUniqueId());
            }

            this.players = worldPlayers;
        }
    }

    /**
     * Represents a player and the locations they have been
     * since the last player watcher update.
     */
    private static class WorldPlayer {
        public final Player player;
        public final LinkedList<CachedLocation> locations;

        public WorldPlayer(Player p, LinkedList<CachedLocation> locations) {
            this.player = p;
            this.locations = locations;
        }
    }

    /**
     * Represents a location a player has been along
     * with the reason the location was added.
     */
    private static class CachedLocation extends Location {

        private final RegionReason _reason;

        public CachedLocation(Location location, RegionReason reason) {
            super(location.getWorld(),
                    location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch());

            _reason = reason;
        }

        public RegionReason getReason() {
            return _reason;
        }
    }

}
