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

package com.jcwhatever.nucleus.utils.nms;

import com.jcwhatever.nucleus.NucleusPlugin;
import com.jcwhatever.nucleus.collections.HashMapMap;
import com.jcwhatever.nucleus.mixins.IPluginOwned;
import com.jcwhatever.nucleus.utils.PreCon;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages NMS code.
 *
 * @see INmsHandler
 */
public class NmsManager implements IPluginOwned {

    private final Plugin _plugin;

    // NMS handler classes
    private final HashMapMap<String, String, Class<? extends INmsHandler>>
            _nmsHandlerClasses = new HashMapMap<>(10);

    // Instantiated NMS handlers
    private final Map<String, INmsHandler> _nmsHandlers = new HashMap<>(10);

    // The NMS version that is being used.
    private final String _nmsVersion;

    /**
     * Constructor.
     *
     * @param plugin  The owning plugin.
     */
    public NmsManager(Plugin plugin) {
        PreCon.notNull(plugin);

        _plugin = plugin;
        _nmsVersion = plugin instanceof NucleusPlugin
                ? ((NucleusPlugin) plugin).getDataNode().getString("nms-version", NmsUtils.getNmsVersion())
                : NmsUtils.getNmsVersion();
    }

    /**
     * Constructor.
     *
     * <p>Disables plugin if the current NMS version is not compatible with the
     * specified versions.</p>
     *
     * @param plugin              The owning plugin.
     * @param compatibleVersions  Compatible NMS package versions.
     */
    public NmsManager(Plugin plugin, String... compatibleVersions) {
        PreCon.notNull(plugin);
        PreCon.notNull(compatibleVersions);

        _plugin = plugin;
        _nmsVersion = plugin instanceof NucleusPlugin
                ? ((NucleusPlugin) plugin).getDataNode().getString("nms-version", NmsUtils.getNmsVersion())
                : NmsUtils.getNmsVersion();

        NmsUtils.enforceNmsVersion(plugin, compatibleVersions);
    }

    @Override
    public Plugin getPlugin() {
        return _plugin;
    }

    /**
     * Get the NMS package version the manager is operating under. This is normally
     * the current NMS package version.
     */
    public String getNmsVersion() {
        return _nmsVersion;
    }

    /**
     * Register an NMS handler for a specific NMS version.
     *
     * @param nmsVersion    The NMS package version the handler is for.
     * @param name          The name of the handler.
     * @param handlerClass  The handler class.
     */
    public synchronized void registerHandler(String nmsVersion, String name,
                                             Class<? extends INmsHandler> handlerClass) {
        _nmsHandlerClasses.put(nmsVersion, name, handlerClass);
    }

    /**
     * Get an NMS handler instance for the current NMS package version.
     *
     * @param name  The name of the handler.
     *
     * @param <T>  The handler type.
     *
     * @return  Null if not found.
     */
    @Nullable
    public synchronized <T extends INmsHandler> T getHandler(String name) {

        INmsHandler handler = _nmsHandlers.get(name);
        if (handler != null) {

            @SuppressWarnings("unchecked")
            T result = (T)handler;

            if (!result.isAvailable())
                return null;

            return result;
        }

        Class<? extends INmsHandler> handlerClass =
                _nmsHandlerClasses.get(NmsUtils.getNmsVersion(), name);

        if (handlerClass == null)
            return null;

        Constructor<? extends INmsHandler> constructor = getConstructor(handlerClass);
        if (constructor == null) {
            throw new RuntimeException("Failed to instantiate an instance of " +
                    handlerClass.getName() + " because a suitable constructor was not found.");
        }

        try {
            handler = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        _nmsHandlers.put(name, handler);

        @SuppressWarnings("unchecked")
        T result = (T)handler;

        if (!result.isAvailable())
            return null;

        return result;
    }

    @Nullable
    private Constructor<? extends INmsHandler> getConstructor(
            Class<? extends INmsHandler> handlerClass, Class<?>... parameters) {
        try {
            Constructor<? extends INmsHandler> constructor = handlerClass.getDeclaredConstructor(parameters);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }
}
