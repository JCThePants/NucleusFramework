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

package com.jcwhatever.bukkit.generic.items.serializer;

import com.jcwhatever.bukkit.generic.converters.ValueConverters;
import com.jcwhatever.bukkit.generic.items.serializer.metahandlers.ItemMetaObject;
import com.jcwhatever.bukkit.generic.items.serializer.metahandlers.MetaHandler;
import com.jcwhatever.bukkit.generic.items.serializer.metahandlers.MetaHandlerManager;
import com.jcwhatever.bukkit.generic.utils.PreCon;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Parses a string representing a single or multiple {@code ItemStacks}
 * serialized by {@code ItemStackSerializer}.
 */
public class ItemStackDeserializer {

    private final String _itemString;
    private StringBuilder _buffer;
    private int _index = 0;
    private List<ItemStack> _results = new LinkedList<>();


    /**
     * Constructor.
     *
     * @param itemString  The string to parse. The string must represent a one or more {@code ItemStack}'s
     *
     * @throws InvalidItemStackStringException
     */
    public ItemStackDeserializer(String itemString) throws InvalidItemStackStringException {
        PreCon.notNull(itemString);

        _itemString = itemString;
        _buffer = new StringBuilder(itemString.length());


        while (canParse()) {
            ItemStack item = parseItemStack();
            if (item == null)
                break;

            _results.add(item);
        }
    }

    /**
     * Get {@code ItemStack} results as a list.
     */
    public List<ItemStack> getResultList() {
        return new ArrayList<>(_results);
    }

    /**
     * Get {@code ItemStack} results as an array.
     */
    public ItemStack[] getResultArray() {
        return _results.toArray(new ItemStack[_results.size()]);
    }

    /*
     * Parse the next item stack in the string.
     * Returns null if there are no more item stacks.
     */
    @Nullable
    private ItemStack parseItemStack() throws InvalidItemStackStringException {

        _buffer.setLength(0);

        if (current() == ',')
            next();

        if (!canParse())
            return null;

        skipEmpty();

        ItemStack stack;
        Material material;
        short data = 0;
        int amount = 1;
        Potion potion;
        List<ItemMetaObject> metaObjects = new ArrayList<>(15);

        String rawMaterial = parseMaterial();
        if (rawMaterial.isEmpty())
            return null;

        material = ValueConverters.ITEM_NAME_MATERIAL.convert(rawMaterial);
        if (material == null)
            throw new InvalidItemStackStringException(_itemString);

        if (current() == ':') {
            next();
            data = parseData();
        }

        if (current() == ';') {
            next();
            amount = parseAmount();
        }

        if (current() == '{') {
            next();
            parseMeta(metaObjects);
        }

        // handle special cases
        if (material == Material.POTION && (potion = ValueConverters.POTION_ID.convert(data)) != null) {
            stack = potion.toItemStack(amount);
        }
        else if (material == Material.WOOL) {
            stack = new ItemStack(material, amount);
            stack.setDurability(data);
        }
        else {
            // set stack data
            stack = new ItemStack(material, amount);
            stack.setDurability(data);
        }

        for (ItemMetaObject meta : metaObjects) {

            MetaHandler handler = MetaHandlerManager.getHandler(meta.getName());
            if (handler == null)
                continue;

            handler.apply(stack, meta);
        }

        return stack;
    }

    /*
     * Parse the material name/id
     */
    private String parseMaterial() {

        _buffer.setLength(0);

        while (canParse()) {

            char ch = current();

            if (isEmptySpace(ch)) {
                next();
            }
            else {
                if (ch != ':' && ch != ';' && ch != '{' && ch != ',') {
                    _buffer.append(ch);
                    next();
                } else {
                    break;
                }
            }
        }

        return _buffer.toString();
    }

    /*
     * Parse string data (byte) value as a short (for use as durability)
     */
    private short parseData() throws InvalidItemStackStringException {

        _buffer.setLength(0);

        while(canParse()) {

            char ch = current();

            if (isEmptySpace(ch)) {
                next();
            } else {

                if (ch != ';' && ch != '{' && ch != ',') {
                    _buffer.append(ch);
                    next();
                } else {
                    break;
                }
            }
        }

        String rawData = _buffer.toString();

        try {
            return Short.parseShort(rawData);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            throw new InvalidItemStackStringException(_itemString);
        }
    }

    /*
     * Parse the number of items in the ItemStack.
     */
    private int parseAmount() throws InvalidItemStackStringException {

        _buffer.setLength(0);

        while(canParse()) {

            char ch = current();

            if (isEmptySpace(ch)) {
                next();
            }
            else {
                if (ch == '{' || ch == ',') {
                    break;
                } else {
                    _buffer.append(ch);
                    next();
                }
            }
        }

        String rawData = _buffer.toString();

        try {
            return Integer.parseInt(rawData);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            throw new InvalidItemStackStringException(_itemString);
        }
    }

    /*
     * Parse extra meta data
     */
    private void parseMeta(Collection<ItemMetaObject> metaObjects) throws InvalidItemStackStringException {

        while (canParse()) {

            String metaName = parseMetaName();
            if (metaName.isEmpty())
                return;

            String metaValue = parseMetaValue();

            metaObjects.add(new ItemMetaObject(metaName, metaValue));
        }
    }

    /*
     * Parse a meta data value name.
     */
    private String parseMetaName() {

        _buffer.setLength(0);

        while (canParse()) {

            char ch = current();

            if (isEmptySpace(ch) || ch == ',') {
                next();
            }
            else {
                if (ch != ':' && ch != '}') {
                    _buffer.append(ch);
                    next();
                }
                else {
                    next();
                    break;
                }
            }
        }

        return _buffer.toString();
    }

    /*
     * Parse a meta data value.
     */
    private String parseMetaValue() throws InvalidItemStackStringException {

        _buffer.setLength(0);
        boolean isParsingLiteral = false;

        while (canParse()) {

            char ch = current();

            if (isParsingLiteral) {

                if (ch == '\\') {
                    char escaped = peek(1);

                    if (escaped == '\"') {
                        _buffer.append(escaped);
                        next();
                        next();
                    }
                    else {
                        _buffer.append(ch);
                        next();
                    }
                }
                else if (ch == '"') {
                    next();
                    break;
                }
                else {
                    _buffer.append(ch);
                    next();
                }
            }
            else {

                if (isEmptySpace(ch) || ch == ':') {
                    next();
                }
                else if (ch == '"') {
                    isParsingLiteral = true;
                    next();
                }
                else {
                    throw new InvalidItemStackStringException(_itemString);
                }
            }
        }

        return _buffer.toString();
    }

    /*
     * Determine if parsing can continue.
     */
    private boolean canParse() {
        return _index < _itemString.length();
    }

    /*
     * Determine if a character is an empty space.
     */
    private boolean isEmptySpace(char ch) {
        return " \r\n\t".indexOf(ch) != -1;
    }

    /*
     * Get the current character.
     */
    private char current() {

        if (!canParse())
            return 0;

        return _itemString.charAt(_index);
    }

    /*
     * Get the next character and increment
     * the index position.
     */
    private char next() {
        char ch = _itemString.charAt(_index);
        _index++;
        return ch;
    }

    /*
     * Get a character forward from the current position
     * without incrementing the index;
     */
    private char peek(int amount) {
        return _itemString.charAt(_index + amount);
    }

    /*
     * Skip all empty characters.
     */
    private void skipEmpty() {
        while (canParse()) {
            if (isEmptySpace(current()))
                next();
            else
                break;
        }
    }

}
