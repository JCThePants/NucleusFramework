/*
 * This file is part of GenericsLib for Bukkit, licensed under the MIT License (MIT).
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

package com.jcwhatever.bukkit.generic.internal.providers;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.internal.providers.permissions.BukkitPermissionsProvider;
import com.jcwhatever.bukkit.generic.internal.providers.permissions.VaultPermissionsProvider;
import com.jcwhatever.bukkit.generic.internal.providers.storage.YamlStorageProvider;
import com.jcwhatever.bukkit.generic.providers.IPermissionsProvider;
import com.jcwhatever.bukkit.generic.providers.IProviderManager;
import com.jcwhatever.bukkit.generic.providers.IStorageProvider;
import com.jcwhatever.bukkit.generic.storage.DataPath;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.storage.YamlDataStorage;
import com.jcwhatever.bukkit.generic.utils.PreCon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Internal provider manager implementation.
 */
public class InternalProviderManager implements IProviderManager {

    private IDataNode _dataNode;

    private IPermissionsProvider _defaultPermissions;

    private IStorageProvider _defaultStorage;

    private final YamlStorageProvider _yamlStorage = new YamlStorageProvider();

    // keyed to plugin name
    private final Map<String, IStorageProvider> _pluginStorage = new HashMap<>(50);

    // keyed to provider name
    private final Map<String, IStorageProvider> _storageProviders = new HashMap<>(10);

    boolean _isProvidersLoading;

    public InternalProviderManager() {
        _storageProviders.put(_yamlStorage.getName().toLowerCase(), _yamlStorage);
    }

    @Override
    public IPermissionsProvider getPermissionsProvider() {
        if (_defaultPermissions == null) {
            _defaultPermissions = Bukkit.getPluginManager().getPlugin("Vault") != null
                    ? new VaultPermissionsProvider()
                    : new BukkitPermissionsProvider();
        }
        return _defaultPermissions;
    }

    @Override
    public void setPermissionsProvider(IPermissionsProvider permissionsProvider) {
        PreCon.notNull(permissionsProvider);
        PreCon.isValid(_isProvidersLoading, "Cannot set providers outside of provider load time.");

        _defaultPermissions = permissionsProvider;
    }

    @Override
    public IStorageProvider getStorageProvider() {
        return _defaultStorage != null ? _defaultStorage : _yamlStorage;
    }

    @Override
    public void setStorageProvider(IStorageProvider storageProvider) {
        PreCon.notNull(storageProvider);
        PreCon.isValid(_isProvidersLoading, "Cannot set providers outside of provider load time.");

        _defaultStorage = storageProvider;
    }

    @Override
    public IStorageProvider getStorageProvider(Plugin plugin) {
        PreCon.notNull(plugin);

        IStorageProvider pluginProvider = _pluginStorage.get(plugin.getName().toLowerCase());
        return pluginProvider != null ? pluginProvider : getStorageProvider();
    }

    @Override
    public void setStorageProvider(Plugin plugin, IStorageProvider storageProvider) {
        PreCon.notNull(plugin);
        PreCon.notNull(storageProvider);

        IDataNode dataNode = getDataNode().getNode("storage");
        List<String> pluginNames = dataNode.getStringList(storageProvider.getName(),
                new ArrayList<String>(5));

        assert pluginNames != null;

        pluginNames.add(plugin.getName());

        dataNode.set(storageProvider.getName(), pluginNames);
        dataNode.saveAsync(null);
    }

    @Nullable
    @Override
    public IStorageProvider getStorageProvider(String name) {
        PreCon.notNullOrEmpty(name);

        return _storageProviders.get(name.toLowerCase());
    }

    @Override
    public List<IStorageProvider> getStorageProviders() {
        return new ArrayList<>(_storageProviders.values());
    }

    @Override
    public void registerStorageProvider(IStorageProvider storageProvider) {
        PreCon.notNull(storageProvider);
        PreCon.isValid(_isProvidersLoading, "Cannot register providers outside of provider load time.");

        _storageProviders.put(storageProvider.getName().toLowerCase(), storageProvider);

        IDataNode dataNode = getDataNode().getNode("storage");

        List<String> pluginNames = dataNode.getStringList(storageProvider.getName(), null);
        if (pluginNames != null) {
            for (String pluginName : pluginNames) {
                _pluginStorage.put(pluginName.toLowerCase(), storageProvider);
            }
        }
    }

    private IDataNode getDataNode() {
        if (_dataNode == null) {
            _dataNode = new YamlDataStorage(GenericsLib.getPlugin(), new DataPath("providers"));
        }
        return _dataNode;
    }
}