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


package com.jcwhatever.nucleus.regions;

import com.jcwhatever.nucleus.regions.data.RegionChunkSection;

import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Biome;
import org.bukkit.material.MaterialData;

import javax.annotation.Nullable;

/**
 * A chunk snapshot used for building in a region.
 */
public class BuildChunkSnapshot implements ChunkSnapshot {

    private final MaterialData[][][] _regionMap;
    private final RegionChunkSection _s;
    private final int _xStart;
    private final int _zStart;

    /**
     * Constructor.
     *
     * @param regionMap  A 3D array representing the region blocks.
     * @param section    The info about the section of the chunk in the region snapshot.
     */
    public BuildChunkSnapshot(MaterialData[][][] regionMap, RegionChunkSection section) {
        _regionMap = regionMap;
        _s = section;

        int chunkX = section.getChunkX() * 16;
        int regionStartX = section.getRegionStartX();

        _xStart = chunkX - regionStartX;

        int chunkZ = section.getChunkZ() * 16;
        int regionStartZ = section.getRegionStartZ();

        _zStart = chunkZ - regionStartZ;
    }

    /**
     * Unsupported.
     */
    @Override
    public Biome getBiome(int arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the byte data for a block at the specified coordinates.
     * Does not return actual data for blocks outside the region
     * boundaries.
     *
     * @param x  The X coordinates relative to the chunk.
     * @param y  The Y coordinates relative to the chunk.
     * @param z  The Z coordinates relative to the chunk.
     */
    @Override
    public int getBlockData(int x, int y, int z) {
        MaterialData material = getMaterial(x, y, z);
        if (material == null)
            return 0;

        return material.getData();
    }

    /**
     * Unsupported.
     */
    @Override
    public int getBlockEmittedLight(int arg0, int arg1, int arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     */
    @Override
    public int getBlockSkyLight(int arg0, int arg1, int arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the block id at the specified coordinates.
     * Does not return actual data for blocks outside the region
     * boundaries.
     *
     * @param x  The X coordinates relative to the chunk.
     * @param y  The Y coordinates relative to the chunk.
     * @param z  The Z coordinates relative to the chunk.
     */
    @Override
    public int getBlockTypeId(int x, int y, int z) {
        MaterialData material = getMaterial(x, y, z);
        if (material == null)
            return 0;

        return material.getItemTypeId();
    }

    /**
     * Unsupported
     */
    @Override
    public long getCaptureFullTime() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
    public int getHighestBlockYAt(int arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
    public double getRawBiomeRainfall(int arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
    public double getRawBiomeTemperature(int arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the world the snapshot is in.
     */
    @Override
    public String getWorldName() {
        return _s.getP1().getWorld().getName();
    }

    /**
     * Get the chunk X coordinates.
     */
    @Override
    public int getX() {
        return _s.getChunkX();
    }

    /**
     * Get the chunk Z coordinates.
     */
    @Override
    public int getZ() {
        return _s.getChunkZ();
    }

    @Override
    public boolean isSectionEmpty(int arg0) {
        return false;
    }

    @Nullable
    private MaterialData getMaterial(int x, int y, int z) {
        x = _xStart + x;
        z = _zStart + z;
        y = y - _s.getStartY();

        if (x >= _regionMap.length)
            return null;

        if (y >= _regionMap[x].length)
            return null;

        if (z >= _regionMap[x][y].length)
            return null;

        return _regionMap[x][y][z];
    }
}
