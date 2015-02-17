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

package com.jcwhatever.nucleus.providers.npc;

import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.mixins.INamedInsensitive;
import com.jcwhatever.nucleus.providers.npc.ai.INpcState;
import com.jcwhatever.nucleus.providers.npc.events.NpcClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByBlockEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByEntityEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDeathEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcLeftClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcRightClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcTargetedEvent;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.observer.script.IScriptUpdateSubscriber;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;

/**
 * Interface for an NPC.
 */
public interface INpc extends INpcState, INamedInsensitive, IDisposable {

    /**
     * Get the NPC's owning registry.
     */
    INpcRegistry getRegistry();

    /**
     * Get the NPC's data node.
     */
    IDataNode getDataNode();

    /**
     * Get the NPC's non-unique name. This is the name displayed
     * above the NPC entity.
     */
    String getNPCName();

    /**
     * Spawn the NPC in the specified location. If the NPC is already spawned,
     * the NPC is teleported.
     *
     * @param location  The location to spawn or teleport the NPC.
     *
     * @return  Self for chaining.
     */
    INpc spawn(Location location);

    /**
     * Despawn the NPC.
     *
     * @return  True if despawned, otherwise false.
     */
    boolean despawn();

    /**
     * Get the NPC vehicle the {@link INpc} is riding.
     *
     * @return  The {@link INpc} vehicle or null if there is no vehicle or the vehicle
     * is not an {@link INpc}.
     */
    @Nullable
    INpc getNPCVehicle();

    /**
     * Get the NPC passenger of the {@link INpc}.
     *
     * @return  The {@link INpc} passenger or null if there is no passenger or the the passenger
     * is not an {@link INpc}.
     */
    @Nullable
    INpc getNPCPassenger();

    /**
     * Mount the current {@link INpc} as a passenger of the specified {@link INpc}.
     *
     * @param vehicle  The vehicle to mount.
     *
     * @return  Self for chaining.
     */
    INpc mountNPC(INpc vehicle);

    /**
     * Position the NPC's head.
     *
     * @param yaw    The yaw angle.
     * @param pitch  The pitch angle.
     *
     * @return  Self for chaining.
     */
    INpc look(float yaw, float pitch);

    /**
     * Make the NPC face the specified {@link Entity}.
     *
     * @param entity  The entity to look at.
     *
     * @return  Self for chaining.
     */
    INpc lookAt(Entity entity);

    /**
     * Make the NPC face a location.
     *
     * @param location  The location to face.
     *
     * @return  Self for chaining.
     */
    INpc lookTowards(Location location);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is spawned.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcSpawn(IScriptUpdateSubscriber<NpcSpawnEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is despawned.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcDespawn(IScriptUpdateSubscriber<NpcDespawnEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is clicked.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcClick(IScriptUpdateSubscriber<NpcClickEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is right clicked.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcRightClick(IScriptUpdateSubscriber<NpcRightClickEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is left click.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcLeftClick(IScriptUpdateSubscriber<NpcLeftClickEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is targeted by another entity.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcEntityTarget(IScriptUpdateSubscriber<NpcTargetedEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is damaged.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcDamage(IScriptUpdateSubscriber<NpcDamageEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is damaged by a block.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcDamageByBlock(IScriptUpdateSubscriber<NpcDamageByBlockEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * is damaged by an entity.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcDamageByEntity(IScriptUpdateSubscriber<NpcDamageByEntityEvent> subscriber);

    /**
     * Attach a subscriber to be updated when the {@link INpc}
     * dies.
     *
     * @param subscriber  The subscriber.
     *
     * @return  Self for chaining.
     */
    INpc onNpcDeath(IScriptUpdateSubscriber<NpcDeathEvent> subscriber);
}
