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

package com.jcwhatever.nucleus.providers.npc.ai;

import com.jcwhatever.nucleus.providers.npc.INpc;

import javax.annotation.Nullable;

/**
 * Abstract implementation of a behaviour pool/collection.
 *
 * <p>The behaviour pool is used to select 1 of possibly many behaviours
 * based on the cost of each behaviour or other factors determined by
 * the implementation.</p>
 */
public interface INpcBehaviourPool<T extends INpcBehaviour> {

    /**
     * Get the NPC the behaviour pool is for.
     */
    INpc getNpc();

    /**
     * Add a behaviour to the pool.
     *
     * @param behaviour  The behaviour.
     *
     * @return  Self for chaining.
     */
    INpcBehaviourPool add(T behaviour);

    /**
     * Remove a behaviour from the pool.
     *
     * @param behaviour  The behaviour to remove.
     *
     * @return  True if found and removed, otherwise false.
     */
    @Nullable
    boolean remove(T behaviour);

    /**
     * Clear all behaviours.
     *
     * @return  Self for chaining.
     */
    INpcBehaviourPool clear();

}
