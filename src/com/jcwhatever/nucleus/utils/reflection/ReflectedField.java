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

package com.jcwhatever.nucleus.utils.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.annotation.Nullable;

/**
 * An encapsulated field.
 *
 * <p>Encapsulated fields are made accessible.</p>
 *
 * <p>An instance of this type can be obtained from {@link ReflectedInstance}.</p>
 *
 * @see ReflectedType
 * @see Reflection
 * @see ReflectionUtils
 */
public class ReflectedField {

    private Field _field;
    private CachedReflectedType _type;
    private int _modifiers;

    /**
     * Constructor.
     *
     * @param type   The cached reflected type.
     * @param field  The field to encapsulate.
     */
    ReflectedField(CachedReflectedType type, Field field) {
        _type = type;
        _field = field;
        _modifiers = field.getModifiers();

        field.setAccessible(true);
    }

    /**
     * Get the field name.
     */
    public String getName() {
        return _field.getName();
    }

    /**
     * Get the field modifiers.
     */
    public int getModifiers() {
        return _modifiers;
    }

    /**
     * Get the field modifiers as they are now.
     */
    public int getCurrentModifiers() {
        return _field.getModifiers();
    }

    /**
     * Determine if the field is static.
     */
    public boolean isStatic() {
        return Modifier.isStatic(_modifiers);
    }

    /**
     * Determine if the field is final.
     */
    public boolean isFinal() {
        return Modifier.isFinal(_modifiers);
    }

    /**
     * Determine if the field is private.
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(_modifiers);
    }

    /**
     * Determine if the field is native.
     */
    public boolean isNative() {
        return Modifier.isNative(_modifiers);
    }

    /**
     * Determine if the field is protected.
     */
    public boolean isProtected() {
        return Modifier.isProtected(_modifiers);
    }

    /**
     * Determine if the field is public.
     */
    public boolean isPublic() {
        return Modifier.isPublic(_modifiers);
    }

    /**
     * Determine if the field is strict.
     */
    public boolean isStrict() {
        return Modifier.isStrict(_modifiers);
    }

    /**
     * Determine if the field is synchronized.
     */
    public boolean isSynchronized() {
        return Modifier.isSynchronized(_modifiers);
    }

    /**
     * Determine if the field is transient.
     */
    public boolean isTransient() {
        return Modifier.isTransient(_modifiers);
    }

    /**
     * Determine if the field is volatile.
     */
    public boolean isVolatile() {
        return Modifier.isVolatile(_modifiers);
    }

    /**
     * Get the type the field is in.
     */
    public ReflectedType getOwnerType() {
        return new ReflectedType(_type);
    }

    /**
     * Get the field type.
     */
    public ReflectedType getType() {

        CachedReflectedType type = Reflection._typeCache.get(_field.getType());
        if (type == null) {

            type = new CachedReflectedType(_field.getType());
            Reflection._typeCache.put(_field.getType(), type);
        }

        return new ReflectedType(type);
    }

    /**
     * Get the field value.
     *
     * @param instance  The instance to get the value from. Null for static.
     */
    public Object get(@Nullable Object instance) {
        try {
            return _field.get(instance);
        } catch (IllegalAccessException | NullPointerException e) {
            e.printStackTrace();
            if (instance != null) {
                throw new RuntimeException("Failed to get field value. The field might be static.");
            }
            else {
                throw new RuntimeException("Failed to get field value. The field might not be static.");
            }
        }
    }

    /**
     * Set the field value.
     *
     * @param instance  The instance to set the value on. Null for static.
     * @param value     The value to set.
     */
    public void set(@Nullable Object instance, @Nullable Object value) {
        try {
            _field.set(instance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set field value.");
        }
    }

    /**
     * Get the {@link java.lang.reflect.Field} object.
     */
    public Field getField() {
        try {
            return _type.getHandle().getField(_field.getName());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get field.");
        }
    }
}
