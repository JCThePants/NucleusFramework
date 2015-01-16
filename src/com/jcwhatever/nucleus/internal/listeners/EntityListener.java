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


package com.jcwhatever.nucleus.internal.listeners;

import com.jcwhatever.nucleus.Nucleus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SlimeSplitEvent;

public final class EntityListener implements Listener {

    @EventHandler
    private void onCreatureSpawn(CreatureSpawnEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onCreeperPower(CreeperPowerEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityBreakDoor(EntityBreakDoorEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityChangeBlock(EntityChangeBlockEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityCombustByBlock(EntityCombustByBlockEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityCombustByEntity(EntityCombustByEntityEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityCombust(EntityCombustEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityCreatePortal(EntityCreatePortalEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityDamageByBlock(EntityDamageByBlockEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityExplode(EntityExplodeEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityInteract(EntityInteractEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityPortalEnter(EntityPortalEnterEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityPortal(EntityPortalEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityPortalExit(EntityPortalExitEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityRegainHealth(EntityRegainHealthEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityShootBow(EntityShootBowEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityTame(EntityTameEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityTarget(EntityTargetEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityTeleport(EntityTeleportEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onEntityUnleash(EntityUnleashEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onExpBottle(ExpBottleEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onExplosionPrime(ExplosionPrimeEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onHorseJump(HorseJumpEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onItemDespawn(ItemDespawnEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onItemSpawn(ItemSpawnEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onPigZap(PigZapEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onPlayerLeashEntity(PlayerLeashEntityEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onPotionSplash(PotionSplashEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onProjectileHit(ProjectileHitEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onProjectileLaunch(ProjectileLaunchEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onSheepDyeWool(SheepDyeWoolEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

    @EventHandler
    private void onSlimeSplit(SlimeSplitEvent event) {

        Nucleus.getEventManager().call(this, event);
    }

}
