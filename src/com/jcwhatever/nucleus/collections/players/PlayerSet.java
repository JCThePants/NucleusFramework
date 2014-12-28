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

import com.jcwhatever.nucleus.collections.wrappers.AbstractConversionIterator;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A {@code HashSet} of {@code Player} objects.
 *
 * <p>
 *     {@code Player} object is automatically removed when the player logs out.
 * </p>
 */
public class PlayerSet extends AbstractPlayerCollection implements Set<Player> {

    private final Set<PlayerEntry> _players;
    private boolean _isDisposed;

    /**
     * Constructor.
     */
    public PlayerSet(Plugin plugin) {
        this(plugin, 10);
    }

    /**
     * Constructor.
     *
     * @param size  The initial capacity.
     */
    public PlayerSet(Plugin plugin, int size) {
        super(plugin);
        _players = new HashSet<>(size);
    }

    @Override
    public boolean add(Player p) {
        PreCon.notNull(p);

        synchronized (_sync) {

            if (_players.add(new PlayerEntry(p))) {
                notifyPlayerAdded(p.getUniqueId());
                return true;
            }

            return false;
        }
    }

    @Override
    public boolean addAll(Collection<? extends Player> collection) {
        PreCon.notNull(collection);

        synchronized (_sync) {
            boolean isChanged = false;

            for (Player p : collection) {
                if (_players.add(new PlayerEntry(p))) {
                    notifyPlayerAdded(p.getUniqueId());
                    isChanged = true;
                }
            }

            return isChanged;
        }
    }

    @Override
    public void clear() {

        synchronized (_sync) {
            for (PlayerEntry p : _players) {
                notifyPlayerRemoved(p.getUniqueId());
            }

            _players.clear();
        }
    }

    @Override
    public boolean contains(Object o) {
        PreCon.notNull(o);

        synchronized (_sync) {
            return o instanceof Player
                    ? _players.contains(new PlayerEntry((Player) o))
                    : _players.contains(o);
        }
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        PreCon.notNull(collection);

        synchronized (_sync) {
            if (collection.isEmpty())
                return false;

            for (Object obj : collection) {
                if (!contains(obj))
                    return false;
            }

            return true;
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (_sync) {
            return _players.isEmpty();
        }
    }

    @Override
    public Iterator<Player> iterator() {
        return new PlayerIterator();
    }

    @Override
    public boolean remove(Object o) {
        PreCon.notNull(o);

        synchronized (_sync) {
            boolean isRemoved = o instanceof Player
                    ? _players.remove(new PlayerEntry((Player) o))
                    : _players.remove(o);

            if (isRemoved) {
                if (o instanceof Player) {
                    notifyPlayerRemoved(((Player) o).getUniqueId());
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        PreCon.notNull(collection);

        synchronized (_sync) {

            boolean isChanged = false;

            for (Object obj : collection) {
                isChanged = isChanged | remove(obj);
            }

            return isChanged;
        }
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        PreCon.notNull(collection);

        synchronized (_sync) {

            Set<PlayerEntry> temp = new HashSet<>(_players);
            //noinspection SuspiciousMethodCalls

            for (Object obj : collection) {
                if (obj instanceof Player) {
                    //noinspection SuspiciousMethodCalls
                    temp.remove(new PlayerEntry((Player) obj));
                }
            }

            boolean isChanged = false;

            for (PlayerEntry p : temp) {
                isChanged = isChanged | remove(p);
            }

            return isChanged;
        }
    }

    @Override
    public int size() {
        synchronized (_sync) {
            return _players.size();
        }
    }

    @Override
    public Object[] toArray() {
        synchronized (_sync) {
            return _players.toArray();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        synchronized (_sync) {
            //noinspection SuspiciousToArrayCall
            return _players.toArray(a);
        }
    }

    @Override
    public void removePlayer(Player p) {
        remove(p);
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    /**
     * Call to remove references that prevent
     * the garbage collector from collecting
     * the instance after it is no longer needed.
     */
    @Override
    public void dispose() {
        clear();
        _isDisposed = true;
    }

    private final class PlayerIterator extends AbstractConversionIterator<Player, PlayerEntry> {

        Iterator<PlayerEntry> iterator = _players.iterator();

        @Override
        public void remove() {
            synchronized (_sync) {
                notifyPlayerRemoved(_current.getUniqueId());
                iterator.remove();
            }
        }

        @Override
        protected Player getElement(PlayerEntry trueElement) {
            return trueElement.getPlayer();
        }

        @Override
        protected Iterator<PlayerEntry> getIterator() {
            return iterator;
        }
    }
}
