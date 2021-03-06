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


package com.jcwhatever.nucleus.utils;

import com.jcwhatever.nucleus.utils.text.TextUtils;

import javax.annotation.Nullable;

/**
 * Enum helper utilities.
 */
public final class EnumUtils {

    private EnumUtils() {}

    /**
     * Get an enum by its constant name. The constant name is case sensitive.
     *
     * @param constantName  The enum constant name. Other valid values are ".random" to choose a
     *                      random constant or ".oneOf: constantName1,constantName2" to randomly
     *                      choose on of the specified constants.
     * @param enumClass     The enum class.
     *
     * @param <T>  The enum type.
     *
     * @return Null if the enum does not have a constant with the specified name.
     */
    @Nullable
    public static <T extends Enum<T>> T getEnum(String constantName, Class<T> enumClass) {
        PreCon.notNull(constantName);
        PreCon.notNull(enumClass);

        return getEnum(constantName, enumClass, null);
    }

    /**
     * Get an enum by its constant name. The constant name is case sensitive.
     *
     * @param constantName  The enum constant name. Other valid values are ".random" to choose a
     *                      random constant or ".oneOf: constantName1,constantName2" to randomly
     *                      choose on of the specified constants.
     * @param enumClass     The enum class.
     * @param def           The default value to return if the constant is not found.
     *
     * @param <T>  The enum type.
     *
     * @return  Default value if the enum does not have a constant with the specified name.
     */
    @Nullable
    public static <T extends Enum<T>> T getEnum(
            String constantName, Class<T> enumClass, @Nullable T def) {

        PreCon.notNull(constantName);
        PreCon.notNull(enumClass);

        if (!constantName.isEmpty()) {

            if (constantName.startsWith("."))
                return getAlternateEnum(constantName, enumClass, def);

            T value;
            try {
                value = Enum.valueOf(enumClass, constantName);
            }
            catch (Exception e) {
                return def;
            }
            return value;
        }
        return def;
    }

    /**
     * Get an enum by its constant name. The constant name is case insensitive.
     *
     * @param constantName  The enum constant name. Other valid values are ".random" to choose a
     *                      random constant or ".oneOf: constantName1,constantName2" to randomly
     *                      choose on of the specified constants.
     * @param enumClass     The enum class.
     *
     * @param <T>  The enum type.
     *
     * @return Null if the enum does not have a constant with the specified name.
     */
    @Nullable
    public static <T extends Enum<T>> T searchEnum(String constantName, Class<T> enumClass) {
        PreCon.notNull(constantName);
        PreCon.notNull(enumClass);

        return searchEnum(constantName, enumClass, null);
    }

    /**
     * Get an enum by its constant name. The constant name is case insensitive.
     *
     * @param constantName  The enum constant name. Other valid values are ".random" to choose a
     *                      random constant or ".oneOf: constantName1,constantName2" to randomly
     *                      choose on of the specified constants.
     * @param enumClass     The enum class.
     * @param def           The default value to return if the constant is not found.
     *
     * @param <T>  The enum type.
     *
     * @return  Default value if the enum does not have a constant with the specified name.
     */
    @Nullable
    public static <T extends Enum<T>> T searchEnum(
            String constantName, Class<T> enumClass, @Nullable T def) {

        PreCon.notNull(constantName);
        PreCon.notNull(enumClass);

        if (!constantName.isEmpty()) {

            if (constantName.startsWith("."))
                return getAlternateEnum(constantName, enumClass, def);

            T[] constants = enumClass.getEnumConstants();

            for (T constant : constants) {
                if (constant.name().equalsIgnoreCase(constantName))
                    return constant;
            }
        }
        return def;
    }

    /**
     * Get an enum of an unknown type. Constant name is case sensitive.
     *
     * @param constantName  The name of the enum constant.
     * @param enumClass     The enum class.
     * @param def           The default value to return if the constant is not found.
     *
     * @return  The default value if the enum constant is not found.
     */
    @Nullable
    public static Enum<?> getGenericEnum(
            String constantName, Class<? extends Enum<?>> enumClass, @Nullable Enum<?> def) {
        PreCon.notNull(constantName);
        PreCon.notNull(enumClass);

        if (!constantName.isEmpty()) {

            Enum<?>[] constants = enumClass.getEnumConstants();

            for (Enum<?> constant : constants) {
                if (constant.name().equals(constantName))
                    return constant;
            }
        }
        return def;
    }

    /**
     * Get an enum of an unknown type. Constant name is case insensitive.
     *
     * @param constantName  The name of the enum constant.
     * @param enumClass     The enum class.
     * @param def           The default value to return if the constant is not found.
     *
     * @return  The default value if the enum constant is not found.
     */
    @Nullable
    public static Enum<?> searchGenericEnum(
            String constantName, Class<? extends Enum<?>> enumClass, @Nullable Enum<?> def) {
        PreCon.notNull(constantName);
        PreCon.notNull(enumClass);

        if (!constantName.isEmpty()) {

            Enum<?>[] constants = enumClass.getEnumConstants();

            for (Enum<?> constant : constants) {
                if (constant.name().equalsIgnoreCase(constantName))
                    return constant;
            }
        }
        return def;
    }

    /**
     * Get a raw enum from the enum constant name. Constant name is case sensitive.
     *
     * @param constantName  The name of the enum constant.
     * @param enumClass     The enum class.
     * @param def           The default value to return if the constant is not found.
     *
     * @return  The default value if the constant name is not found.
     */
    @Nullable
    public static Enum getRawEnum(String constantName, Class enumClass, @Nullable Enum def) {
        PreCon.notNull(constantName);
        PreCon.notNull(enumClass);
        PreCon.isValid(enumClass.isEnum(), "enumClass must be an enum class.");

        if (!constantName.isEmpty()) {

            for (Object constant : enumClass.getEnumConstants()) {
                if (constant instanceof Enum) {
                    if (((Enum) constant).name().equals(constantName)) {
                        return (Enum)constant;
                    }
                }
            }
        }
        return def;
    }

    /**
     * Get a raw enum from the enum constant name. Constant name is case insensitive.
     *
     * @param constantName  The name of the enum constant.
     * @param enumClass     The enum class.
     * @param def           The default value to return if the constant is not found.
     *
     * @return  The default value if the constant name is not found.
     */
    @Nullable
    public static Enum searchRawEnum(String constantName, Class enumClass, @Nullable Enum def) {
        PreCon.notNull(constantName);
        PreCon.notNull(enumClass);
        PreCon.isValid(enumClass.isEnum(), "enumClass must be an enum class.");

        if (!constantName.isEmpty()) {

            for (Object constant : enumClass.getEnumConstants()) {
                if (constant instanceof Enum) {
                    if (((Enum) constant).name().equalsIgnoreCase(constantName)) {
                        return (Enum)constant;
                    }
                }
            }
        }
        return def;
    }

    /**
     * Get an enum from an object.
     *
     * <p>The object must be an instance of the enum or the name of the enum.</p>
     *
     * @param name       The enum constant name. Other valid values are ".random" to choose a
     *                   random constant or ".oneOf: constantName1,constantName2" to randomly
     *                   choose on of the specified constants.
     * @param enumClass  The enum class.
     *
     * @param <T>  The enum type.
     *
     * @return  The enum constant.
     *
     * @throws IllegalArgumentException if the object cannot be converted to an enum constant.
     */
    public static <T extends Enum<T>> T getEnum(Object name, Class<T> enumClass) {

        if (name instanceof String) {

            T result = searchEnum((String)name, enumClass);
            if (result == null) {
                throw new IllegalArgumentException("Invalid enum constant name for type: " +
                        enumClass.getName() +
                        "\nValid values are: " +
                        TextUtils.concat(enumClass.getEnumConstants(), ", "));
            }
            return result;
        }
        else if (enumClass.isInstance(name)) {
            return enumClass.cast(name);
        }
        else {
            throw new IllegalArgumentException("Invalid type provided. Unable to convert to type: "
                    + enumClass.getName());
        }
    }

    private static <T extends Enum<T>> T getAlternateEnum(
            String alternateName, Class<T> enumClass, @Nullable T def) {

        if (alternateName.equals(".random")) {
            T[] constants = enumClass.getEnumConstants();
            return Rand.get(constants);
        } else if (alternateName.startsWith(".oneOf:")) {

            alternateName = alternateName.substring(7);

            String[] options = TextUtils.PATTERN_COMMA.split(alternateName);
            alternateName = Rand.get(options).trim();

            return searchEnum(alternateName, enumClass, def);
        }

        return def;
    }
}
