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

package com.jcwhatever.nucleus.internal.commands.scripts;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.internal.NucLang;
import com.jcwhatever.nucleus.language.Localizable;
import com.jcwhatever.nucleus.messaging.ChatPaginator;
import com.jcwhatever.nucleus.scripting.IScript;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;

import org.bukkit.command.CommandSender;

import java.util.List;
import javax.script.ScriptEngine;

@CommandInfo(
        parent="scripts",
        command = "list",
        staticParams = { "page=1" },
        description = "List scripts.",

        paramDescriptions = {
                "page= {PAGE}"})

public final class ListSubCommand extends AbstractCommand {

    @Localizable
    static final String _PAGINATOR_TITLE = "Scripts";
    @Localizable static final String _LABEL_NO_ENGINE = "<!no engine!>";

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws CommandException {

        int page = args.getInteger("page");

        ChatPaginator pagin = new ChatPaginator(Nucleus.getPlugin(), 7, NucLang.get(_PAGINATOR_TITLE));

        List<IScript> scripts = Nucleus.getScriptManager().getScripts();

        for (IScript script : scripts) {
            ScriptEngine engine = Nucleus.getScriptEngineManager().getEngineByExtension(script.getType());

            pagin.add(script.getName(), engine != null
                    ? engine.getFactory().getEngineName() + ", " + engine.getFactory().getEngineVersion()
                    : NucLang.get(_LABEL_NO_ENGINE));
        }

        pagin.show(sender, page, FormatTemplate.LIST_ITEM_DESCRIPTION);
    }
}
