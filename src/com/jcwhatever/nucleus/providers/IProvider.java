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

package com.jcwhatever.nucleus.providers;

import com.jcwhatever.nucleus.mixins.INamed;

/**
 * Basic interface for a NucleusFramework service provider.
 */
public interface IProvider extends INamed {

    /**
     * Get the providers name.
     */
    @Override
    String getName();

    /**
     * Get the providers version.
     */
    String getVersion();

    /**
     * Get the providers logical version.
     */
    int getLogicalVersion();

    /**
     * Invoked when it is time for the provider to
     * register any types that may be used by other plugins
     * and/or providers.
     */
    void onRegister();

    /**
     * Invoked when the provider is enabled.
     */
    void onEnable();

    /**
     * Invoked when the provider is disabled.
     */
    void onDisable();
}
