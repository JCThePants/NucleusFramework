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

package com.jcwhatever.nucleus.managed.entity.meta;

import com.jcwhatever.nucleus.mixins.IPluginOwned;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Interface for entity meta data manager.
 *
 * <p>Implementation should guarantee no memory leaks if an entity no longer exists.</p>
 *
 * <p>Meta data is transient.</p>
 *
 * <p>Player meta data is cleared when the player logs out.</p>
 */
public interface IEntityMetaContext extends IPluginOwned {

    /**
     * Set an entities meta data.
     *
     * @param entity  The entity.
     * @param key     The meta key.
     * @param value
     */
    void set(Entity entity, String key, @Nullable Object value);

    /**
     * Get meta data from an entity.
     *
     * @param entity  The entity.
     * @param key     The meta key.
     */
    @Nullable
    <T> T get(Entity entity, String key);

    /**
     * Determine if an entity has a meta key.
     *
     * @param entity  The entity.
     * @param key     The meta key.
     */
    boolean has(Entity entity, String key);

    /**
     * Copy meta from one entity to another.
     *
     * <p>Existing meta in the target entity is not touched unless there is a matching
     * key in the source entity, in which case, the source entity meta will overwrite
     * the target entity meta.</p>
     *
     * @param source  The entity to copy meta data from.
     * @param target  The entity to copy meta data to.
     */
    void copy(Entity source, Entity target);

    /**
     * Get a new map of meta data stored in an entity.
     *
     * @param entity  The entity to get meta data from.
     *
     * @return  A new map of meta data.
     */
    Map<String, Object> getAll(Entity entity);
}
