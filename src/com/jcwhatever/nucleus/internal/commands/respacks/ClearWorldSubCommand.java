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

package com.jcwhatever.nucleus.internal.commands.respacks;

import com.jcwhatever.nucleus.internal.NucLang;
import com.jcwhatever.nucleus.internal.commands.kits.AbstractKitCommand;
import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.resourcepacks.ResourcePacks;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        parent="respacks",
        command="clearworld",
        staticParams={ "worldName=" },
        description="Clear the resource pack used in a world. Uses default resource pack.",

        paramDescriptions = {
                "worldName= Optional. The name of the world to set. If omitted, "
                        + "the command senders current world is used."
        })

class ClearWorldSubCommand extends AbstractKitCommand implements IExecutableCommand {

    @Localizable static final String _WORLD_NOT_FOUND = "A world named '{0}' was not found.";
    @Localizable static final String _SUCCESS = "Resource pack set to default in world '{0}'.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        World world;

        if (args.isDefaultValue("worldName")) {

            CommandException.checkNotConsole(getRegistered(), sender);

            world = ((Player)sender).getWorld();
        }
        else {

            String worldName = args.getString("worldName");
            world = Bukkit.getWorld(worldName);
            if (world == null)
                throw new CommandException(NucLang.get(_WORLD_NOT_FOUND, worldName));
        }

        ResourcePacks.setWorld(world, ResourcePacks.getDefault());

        tellSuccess(sender, NucLang.get(_SUCCESS, world.getName()));
    }
}