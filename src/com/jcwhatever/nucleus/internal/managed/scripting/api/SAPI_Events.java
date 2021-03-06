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


package com.jcwhatever.nucleus.internal.managed.scripting.api;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.collections.observer.subscriber.SubscriberLinkedList;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.ISubscriber;
import com.jcwhatever.nucleus.utils.observer.event.EventSubscriberPriority;
import com.jcwhatever.nucleus.utils.observer.script.IScriptEventSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptEventSubscriber;
import com.jcwhatever.nucleus.utils.text.TextUtils;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Provide scripts with an event registration API
 */
public class SAPI_Events implements IDisposable {

    private final Plugin _plugin;
    private final List<RegisteredBukkitEvent> _registeredBukkit = new ArrayList<>(10);
    private final Deque<ISubscriber> _subscribers = new SubscriberLinkedList<>();
    private boolean _isDisposed;

    public SAPI_Events(Plugin plugin) {
        _plugin = plugin;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        // unregister Bukkit event handlers
        for (RegisteredBukkitEvent registered : _registeredBukkit) {
            registered._handlerList.unregister(registered._registeredListener);
        }
        _registeredBukkit.clear();

        // unregister nucleus event handlers
        while (!_subscribers.isEmpty()) {
            ISubscriber subscriber = _subscribers.remove();
            subscriber.dispose();
        }

        _isDisposed = true;
    }

    /**
     * Registers an event handler.
     *
     * @param eventName  The event class name.
     * @param priority   The event priority as a string. Can specify "invokeForCancelled".
     *                   ie. "NORMAL:invokeForCancelled". By default, handlers are not invoked if
     *                   event is cancelled by another handler.
     * @param handler    The event handler.
     *
     * @return True if successfully registered.
     */
    public boolean on(String eventName, String priority, final IScriptEventSubscriber handler) {
        PreCon.notNullOrEmpty(eventName);
        PreCon.notNullOrEmpty(priority);
        PreCon.notNull(handler);

        String[] priorityComp = TextUtils.PATTERN_COLON.split(priority);
        boolean ignoreCancelled = true;

        if (priorityComp.length == 2) {
            if (priorityComp[1].equalsIgnoreCase("invokeForCancelled")) {
                ignoreCancelled = false;
            }
        }

        Class<?> eventClass;

        try {
            eventClass = Class.forName(eventName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        // check for and register bukkit events
        if (Event.class.isAssignableFrom(eventClass)) {
            Class<? extends Event> bukkitEventClass = eventClass.asSubclass(Event.class);
            return registerBukkitEvent(bukkitEventClass, priorityComp[0], ignoreCancelled, handler);
        }
        else {
            return registerNucleusEvent(eventClass, priorityComp[0], ignoreCancelled, handler);
        }
    }

    /*
     * Register NucleusFramework event.
     */
    private boolean registerNucleusEvent(Class<?> event, String priority, boolean ignoreCancelled,
                                         final IScriptEventSubscriber<?> handler) {

        EventSubscriberPriority eventPriority = EventSubscriberPriority.NORMAL;

        try {
            eventPriority = EventSubscriberPriority.valueOf(priority.toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ScriptEventSubscriber subscriber = new ScriptEventSubscriber<>(handler);
        subscriber.setPriority(eventPriority);
        subscriber.setInvokedForCancelled(!ignoreCancelled);

        //noinspection unchecked
        Nucleus.getEventManager().register(_plugin, event, subscriber);

        _subscribers.add(subscriber);

        return true;
    }

    /*
     * Register Bukkit event
     */
    private boolean registerBukkitEvent(Class<? extends Event> event, String priority,
                                        boolean ignoreCancelled,
                                        final IScriptEventSubscriber handler) {
        EventPriority eventPriority = EventPriority.NORMAL;

        try {
            eventPriority = EventPriority.valueOf(priority.toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object result;

        try {
            result = event.getMethod("getHandlerList").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }

        if (!(result instanceof HandlerList))
            return false;

        final HandlerList handlerList = (HandlerList)result;
        final Listener dummyListener = new Listener() {};

        final EventExecutor eventExecutor = new EventExecutor() {

            boolean isDisposed = false;

            @Override
            public void execute(Listener listener, Event event) throws EventException {

                try {
                    //noinspection unchecked
                    handler.onEvent(event, new IDisposable() {

                        @Override
                        public boolean isDisposed() {
                            return isDisposed;
                        }

                        @Override
                        public void dispose() {
                            isDisposed = true;
                            handlerList.unregister(dummyListener);
                        }
                    });

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };

        RegisteredListener registeredListener = new RegisteredListener(dummyListener,
                eventExecutor, eventPriority, _plugin, ignoreCancelled);

        handlerList.register(registeredListener);

        _registeredBukkit.add(new RegisteredBukkitEvent(handlerList, registeredListener));

        return true;
    }



    private static class RegisteredBukkitEvent {
        HandlerList _handlerList;
        RegisteredListener _registeredListener;

        RegisteredBukkitEvent(HandlerList handlerList, RegisteredListener listener) {
            _handlerList = handlerList;
            _registeredListener = listener;
        }
    }
}
