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

package com.jcwhatever.nucleus.providers.npc.ai.goals;

import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviourPool;

/**
 * Interface for an NPC's goal manager.
 */
public interface INpcGoals extends INpcBehaviourPool<INpcGoal> {

    /**
     * Add a goal.
     *
     * @param priority  The priority of the goal. A larger number is higher priority.
     * @param goal      The goal to add.
     *
     * @return  Self for chaining.
     */
    INpcGoals add(int priority, INpcGoal goal);

    /**
     * Add a goal.
     *
     * @param priority  The priority provider of the goal.
     * @param goal      The goal to add.
     *
     * @return  Self for chaining.
     */
    INpcGoals add(INpcGoalPriority priority, INpcGoal goal);

    /**
     * Determine if goals are running.
     */
    boolean isRunning();

    /**
     * Pause execution of goals.
     */
    INpcGoals pause();

    /**
     * Resume execution of goals.
     */
    INpcGoals resume();
}
