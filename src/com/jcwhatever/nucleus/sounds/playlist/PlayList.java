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

package com.jcwhatever.nucleus.sounds.playlist;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.events.sounds.PlayListLoopEvent;
import com.jcwhatever.nucleus.events.sounds.PlayListTrackChangeEvent;
import com.jcwhatever.nucleus.mixins.IMeta;
import com.jcwhatever.nucleus.mixins.IPluginOwned;
import com.jcwhatever.nucleus.sounds.ResourceSound;
import com.jcwhatever.nucleus.sounds.SoundManager;
import com.jcwhatever.nucleus.sounds.SoundSettings;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.MetaStore;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Rand;
import com.jcwhatever.nucleus.utils.Scheduler;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

/**
 * Abstract implementation of a play list that plays a collection of
 * resource sounds to a player.
 */
public abstract class PlayList implements IPluginOwned {

    // static references for use by events
    private static Map<Player, Set<PlayList>> _instances = new WeakHashMap<>(100);

    /**
     * Remove player from all play lists.
     *
     * @param player  The player to remove.
     */
    public static void clearQueue(Player player) {

        Set<PlayList> playLists = _instances.get(player);
        if (playLists == null)
            return;

        for (PlayList playList : playLists) {
            playList.removePlayer(player);
        }
    }

    /**
     * Get all {@code PlayList}'s the player is currently listening to.
     *
     * @param player  The player.
     *
     * @return  A new {@code List} of {@code PlayList}.
     */
    public static List<PlayList> getAll(Player player) {
        Set<PlayList> playLists = _instances.get(player);
        if (playLists == null)
            return new ArrayList<>(0);

        return new ArrayList<>(playLists);
    }

    private final Plugin _plugin;
    private final Map<Player, PlayerSoundQueue> _playerQueues = new WeakHashMap<>(25);

    private boolean _isLoop;
    private boolean _isRandom;

    /**
     * Constructor.
     *
     * @param plugin  The owning plugin.
     */
    public PlayList(Plugin plugin) {
        PreCon.notNull(plugin);

        _plugin = plugin;
    }

    /**
     * Get the owning plugin.
     */
    @Override
    public Plugin getPlugin() {
        return _plugin;
    }

    /**
     * Determine if the play list is being
     * run in a loop.
     */
    public boolean isLoop() {
        return _isLoop;
    }

    /**
     * Set the play lists looping mode.
     *
     * @param isLoop  True to enable looping.
     */
    public void setLoop(boolean isLoop) {
        _isLoop = isLoop;
    }

    /**
     * Determine if the playlist should be played in random order.
     */
    public boolean isRandom() {
        return _isRandom;
    }

    /**
     * Set the playlist random order mode.
     *
     * @param isRandom  True to randomize order, false to play in order.
     */
    public void setRandom(boolean isRandom) {
        _isRandom = isRandom;
    }

    /**
     * Add a player to the playlist so they can listen to it.
     *
     * @param player    The player to add.
     * @param settings  The sound settings to use.
     *
     * @return  The current or new {@code PlayerSoundQueue} for the player.
     * Null if the player was not added due to there being no sounds to play.
     */
    @Nullable
    public PlayerSoundQueue addPlayer(Player player, SoundSettings settings) {
        PreCon.notNull(player);

        PlayerSoundQueue current = _playerQueues.get(player);
        if (current != null) {
            current._isRemoved = false;
            return current;
        }

        PlayerSoundQueue queue = new PlayerSoundQueue(player, settings);
        ResourceSound sound = queue.next();
        if (sound == null)
            return null;

        Set<PlayList> playLists = _instances.get(player);
        if (playLists == null) {
            playLists = new HashSet<>(10);
            _instances.put(player, playLists);
        }

        playLists.add(this);
        _playerQueues.put(player, queue);

        SoundManager.playSound(_plugin, player, sound, settings, null)
                .onFinish(new TrackChanger(player, queue));

        return queue;
    }

    /**
     * Remove a player from the playlist.
     *
     * <p>Unless the player is in a different world than the playlist, the players
     * sound queue is marked for removal and removed when the currently playing
     * sound ends. Otherwise it is removed immediately.</p>
     *
     * @param player  The player to remove.
     */
    public boolean removePlayer(Player player) {
        return removePlayer(player, false);
    }

    /**
     * Remove a player from the playlist.
     *
     * <p>Unless forced or the player is in a different world, the players sound queue
     * is marked for removal and removed when the currently playing sound ends.</p>
     *
     * <p>Forcing the immediate removal of the players sound queue does not end the
     * sound on the client.</p>
     *
     * @param player  The player to remove.
     * @param force   True to force the immediate removal of the players sound queue.
     */
    public boolean removePlayer(Player player, boolean force) {
        PreCon.notNull(player);

        PlayerSoundQueue queue = _playerQueues.get(player);
        if (queue == null)
            return false;

        if (force)
            queue.removeNow();
        else
            queue.remove();

        return true;
    }

    /**
     * Get the players current sound queue from the playlist,
     * if any.
     *
     * @param player  The player to check.
     *
     * @return  Null if the player is not listening to the playlist.
     */
    @Nullable
    public PlayerSoundQueue getSoundQueue(Player player) {
        PreCon.notNull(player);

        return _playerQueues.get(player);
    }

    /**
     * Invoked to get a list of sounds to play in a {@code PlayerSoundQueue}.
     *
     * <p>If the playlist is loop enabled, this is invoked every time the
     * playlist needs to refill its sound queue.</p>
     *
     * @param queue      The {@code PlayerSoundQueue} that will be refilled.
     * @param loopCount  The number of times the sound queue has been refilled.
     */
    protected abstract List<ResourceSound> getSounds(PlayerSoundQueue queue, int loopCount);

    /**
     * Invoked when the next sound is played from a playlist.
     *
     * <p>Allows the next sound to be changed.</p>
     *
     * <p>Calls the {@code PlayListTrackChangeEvent}.</p>
     *
     * <p>Intended for optional override by implementation.</p>
     *
     * @param queue  The {@code PlayerSoundQueue} that will be refilled.
     * @param prev   The previous sound that was playing, if any.
     * @param next   The expected next sound to be played.
     *
     * @return  The next sound to play. Null ends the queue playback.
     */
    @Nullable
    protected ResourceSound onTrackChange(PlayerSoundQueue queue,
                                                   @Nullable ResourceSound prev, ResourceSound next) {

        PlayListTrackChangeEvent event = new PlayListTrackChangeEvent(this, queue, prev, next);
        Nucleus.getEventManager().callBukkit(this, event);

        if (event.isCancelled())
            return null;

        return event.getNextSound();
    }

    /**
     * Invoked when a {@code PlayerSoundQueue} is finished and is preparing to refill so it
     * can loop.
     *
     * <p>Is also invoked on initial playback with a {@code loopCount} of 0.</p>
     *
     * <p>Calls the {@code PlayListLoopEvent}.</p>
     *
     * <p>Intended for optional override by implementation.</p>
     *
     * @param queue      The {@code PlayerSoundQueue} that is playing.
     * @param sounds     The list of {@code ResourceSound}'s that will be played during the next loop.
     * @param loopCount  The number of times the {@code PlayerSoundQueue} has already looped.
     */
    protected void onLoop(PlayerSoundQueue queue, List<ResourceSound> sounds, int loopCount) {

        PlayListLoopEvent event = new PlayListLoopEvent(this, queue, sounds, loopCount);
        Nucleus.getEventManager().callBukkit(this, event);

        if (event.isCancelled())
            sounds.clear();
    }

    /**
     * An active playlist for a specific player.
     */
    public class PlayerSoundQueue implements IMeta {

        private final WeakReference<Player> _player;
        private final LinkedList<ResourceSound> _queue = new LinkedList<>();
        private final World _world;
        private final SoundSettings _settings;
        private final MetaStore _meta = new MetaStore();

        private ResourceSound _current;
        private boolean _isRemoved;
        private int _loopCount;

        /**
         * Constructor.
         *
         * @param player  The player the sound queue is for.
         */
        PlayerSoundQueue(Player player, SoundSettings settings) {
            _player = new WeakReference<Player>(player);
            _settings = settings;
            _world = player.getWorld();
            refill();
        }

        /**
         * Get the player the sound queue is for.
         *
         * <p>Player is held by a weak reference, may return null.</p>
         */
        @Nullable
        public Player getPlayer() {
            return _player.get();
        }

        /**
         * Get the world the playlist is playing in.
         */
        public World getWorld() {
            return _world;
        }

        /**
         * Get the current sound being played to the player.
         */
        @Nullable
        public ResourceSound getCurrent() {
            return _current;
        }

        /**
         * Determine if the sound queue is marked for removal
         * after the current sound completes.
         */
        public boolean isRemoved() {
            return _isRemoved;
        }

        /**
         * Get the sound settings to use.
         */
        public SoundSettings getSettings() {
            return _settings;
        }

        @Nullable
        @Override
        public <T> T getMeta(MetaKey<T> key) {
            return _meta.getMeta(key);
        }

        @Nullable
        @Override
        public Object getMetaObject(Object key) {
            return _meta.getMetaObject(key);
        }

        @Override
        public <T> void setMeta(MetaKey<T> key, @Nullable T value) {
            _meta.setMeta(key, value);
        }

        /**
         * Mark the sound queue for removal.
         *
         * <p>If the player is still in a world where the sound is playing, the player
         * is not removed from the queue until after the current sound ends since there
         * is no way to stop the sound. If the player moves to a different world, the sound
         * is ended on the client and the player is removed from the queue immediately.</p>
         *
         * <p>Remove operation is performed after a 1 tick delay to ensure the {@code World} reported by
         * the {@code Player} object is up-to-date.</p>
         */
        void remove() {

            Scheduler.runTaskLater(getPlugin(), new Runnable() {
                @Override
                public void run() {

                    // check if player should be removed from queue immediately or
                    // wait for current song to end.
                    Player player = getPlayer();
                    boolean removeNow = player == null;
                    if (player != null) {

                        World world = player.getWorld();
                        removeNow = world == null || !world.equals(_world);
                    }

                    if (removeNow) {
                        removeNow();
                    }

                    _isRemoved = true;
                }
            });
        }

        /**
         * Remove the player sound queue.
         *
         * <p>Does not end sound on client.</p>
         */
        void removeNow() {

            Player player = getPlayer();
            if (player == null)
                return;

            _queue.clear();
            _playerQueues.remove(player);

            Set<PlayList> playLists = _instances.get(player);
            if (playLists != null) {
                playLists.remove(PlayList.this);
            }

            _isRemoved = true;
        }

        /**
         * Get the next sound in the queue.
         *
         * @return  Null if the playlist is finished.
         */
        @Nullable
        ResourceSound next() {

            Player player = getPlayer();
            ResourceSound prev = _current;

            // check for end of queue or loop
            if (_isRemoved ||
                    player == null ||
                    (_queue.isEmpty() && !_isLoop)) {

                return _current = null;
            }
            else if (_queue.isEmpty()) {

                refill();
                if (_queue.isEmpty())
                    return _current = null;
            }

            // check for linear or random playback
            if (_isRandom) {
                int index = Rand.getInt(0, _queue.size() - 1);
                _current = _queue.remove(index);
            }
            else {
                _current = _queue.pollFirst();
            }

            return _current = onTrackChange(this, prev, _current);
        }

        // refill queue
        private void refill() {

            List<ResourceSound> sounds = getSounds(this, _loopCount);
            _queue.addAll(sounds);

            onLoop(this, _queue, _loopCount);

            _loopCount++;
        }
    }

    /**
     * Task to ensure the next song in the player queue is played.
     */
    private class TrackChanger implements Runnable {

        private final WeakReference<Player> _player;
        private final PlayerSoundQueue _soundQueue;

        TrackChanger(Player player, PlayerSoundQueue queue) {
            _player = new WeakReference<Player>(player);
            _soundQueue = queue;
        }

        @Override
        public void run() {

            Player player = _player.get();
            if (player == null)
                return;

            if (_soundQueue.isRemoved()) {
                removeNow(player);
                return;
            }

            ResourceSound sound = _soundQueue.next();
            if (sound == null) {
                removeNow(player);
                return;
            }

            SoundManager.playSound(_plugin, player, sound, _soundQueue.getSettings(), null).onFinish(this);
        }

        private void removeNow(Player player) {
            PlayerSoundQueue current = _playerQueues.get(player);
            if (current != _soundQueue)
                return;

            current.removeNow();
        }
    }
}
