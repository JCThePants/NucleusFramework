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


package com.jcwhatever.nucleus.regions.file;

import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.regions.data.RegionChunkSection;
import com.jcwhatever.nucleus.utils.EnumUtils;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.coords.ChunkBlockInfo;
import com.jcwhatever.nucleus.utils.coords.ICoords2Di;
import com.jcwhatever.nucleus.utils.file.NucleusByteReader;
import com.jcwhatever.nucleus.utils.file.SerializableBlockEntity;
import com.jcwhatever.nucleus.utils.file.SerializableFurnitureEntity;
import com.jcwhatever.nucleus.utils.observer.result.FutureResultAgent.Future;
import com.jcwhatever.nucleus.utils.performance.queued.Iteration3DTask;
import com.jcwhatever.nucleus.utils.performance.queued.QueueProject;
import com.jcwhatever.nucleus.utils.performance.queued.QueueTask;
import com.jcwhatever.nucleus.utils.performance.queued.TaskConcurrency;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Loads a regions chunk block data from a file.
 */
public final class RegionChunkFileLoader {

    public static final int COMPATIBLE_FILE_VERSION = 3;
    public static final int RESTORE_FILE_VERSION = 4;
    private Plugin _plugin;
    private IRegion _region;
    private ICoords2Di _coords;
    private boolean _isLoading;
    private final LinkedList<ChunkBlockInfo> _blockInfo = new LinkedList<>();
    private final LinkedList<SerializableBlockEntity> _blockEntities = new LinkedList<>();
    private final LinkedList<SerializableFurnitureEntity> _entities = new LinkedList<>();

    /**
     * Specifies what blocks are loaded from the file.
     */
    public enum LoadType {
        /**
         * Loads all blocks info from the file
         */
        ALL_BLOCKS,
        /**
         * Loads blocks that do no match the current chunk.
         */
        MISMATCHED
    }

    /**
     * Constructor.
     *
     * @param region  The region to load a chunk file for.
     * @param coords  The coords of the chunk that the file was created from.
     */
    public RegionChunkFileLoader (IRegion region, ICoords2Di coords) {
        _region = region;
        _coords = coords;
        _plugin = region.getPlugin();
    }

    /**
     * Get the chunk.
     */
    public ICoords2Di getChunkCoord() {
        return _coords;
    }

    /**
     * Get the region.
     */
    public IRegion getRegion() {
        return _region;
    }

    /**
     * Determine if the file is in the process
     * of being loaded.
     */
    public boolean isLoading() {
        return _isLoading;
    }

    /**
     * Get block info loaded from the file.
     *
     * <p>Do not access while loading from file.</p>
     */
    public LinkedList<ChunkBlockInfo> getBlockInfo() {
        if (_isLoading)
            throw new IllegalStateException("Cannot access block info while data " +
                    "is being loaded from file.");

        return _blockInfo;
    }

    /**
     * Get block entity data loaded from the file.
     *
     * <p>Do not access while loading from file.</p>
     */
    public LinkedList<SerializableBlockEntity> getBlockEntityInfo() {
        if (_isLoading)
            throw new IllegalStateException("Cannot access block entity info while " +
                    "data is being loaded from file.");

        return _blockEntities;
    }

    /**
     * Get entity data loaded from the file.
     *
     * <p>Do not access while loading from file.</p>
     */
    public LinkedList<SerializableFurnitureEntity> getFurnitureEntityInfo() {
        if (_isLoading)
            throw new IllegalStateException("Cannot access furniture entity " +
                    "info while data is being loaded from file.");

        return _entities;
    }

    /**
     * Load data from a specific file.
     *
     * @param file      The file.
     * @param project   The project to add the loading task to.
     * @param loadType  The callback to run when load is finished.
     */
    public Future<QueueTask> loadInProject(File file, QueueProject project, LoadType loadType) {
        PreCon.notNull(file);
        PreCon.notNull(project);

        if (isLoading())
            return project.cancel("Region cannot load because it is already loading.");

        World world = _region.getWorld();

        if (world == null) {
            return project.cancel("Failed to get world while loading region '{0}'.",
                    _region.getName());
        }

        Chunk chunk = _region.getWorld().getChunkAt(_coords.getX(), _coords.getZ());
        if (chunk == null) {
            return project.cancel("Failed to get chunk ({0}, {1}) in world '{0}' while building region '{1}'.",
                    _coords.getX(), _coords.getZ(), world.getName());
        }

        if (!chunk.isLoaded())
            chunk.load();

        _isLoading = true;

        RegionChunkSection section = new RegionChunkSection(_region, _coords);
        LoadChunkIterator iterator = new LoadChunkIterator(file, loadType, 8192,
                section.getStartChunkX(), section.getStartY(), section.getStartChunkZ(),
                section.getEndChunkX(), section.getEndY(), section.getEndChunkZ(), chunk);

        project.addTask(iterator);

        return iterator.getResult();
    }

    /**
     * Load data from a specific file.
     *
     * @param file      The file.
     * @param loadType  The block load type.
     *
     * @return  A future to receive the load results.
     */
    public Future<QueueTask> load(File file, LoadType loadType) {

        QueueProject project = new QueueProject(_plugin);

        Future<QueueTask> future = loadInProject(file, project, loadType);

        project.run();

        return future;
    }

    /*
     * Iteration worker for loading region chunk data from a file.
     */
    private final class LoadChunkIterator extends Iteration3DTask {

        private NucleusByteReader reader;
        private final ChunkSnapshot snapshot;
        private final File file;
        private final LoadType loadType;

        /**
         * Constructor
         */
        LoadChunkIterator (File file, LoadType loadType, long segmentSize, int xStart, int yStart, int zStart,
                           int xEnd, int yEnd, int zEnd, Chunk chunk) {

            super(_plugin, TaskConcurrency.ASYNC, segmentSize, xStart, yStart, zStart, xEnd, yEnd, zEnd);

            this.snapshot = chunk.getChunkSnapshot();
            this.file = file;
            this.loadType = loadType;
        }

        /**
         * Read file header
         */
        @Override
        protected void onIterateBegin() {
            _blockInfo.clear();
            _blockEntities.clear();

            try {

                reader = new NucleusByteReader(new FileInputStream(file));

                // Read restore file version
                int restoreFileVersion = reader.getInteger();

                // make sure the file version is correct
                if (restoreFileVersion != RESTORE_FILE_VERSION &&
                        restoreFileVersion != COMPATIBLE_FILE_VERSION) {
                    cancel("Invalid region file. File is an old version and is no longer valid.");
                    _isLoading = false;
                    return;
                }

                // get name of the region associated with the restore file
                reader.getString();

                // get the name of the world the region was saved from.
                reader.getString();


                if (restoreFileVersion == COMPATIBLE_FILE_VERSION) {
                    // get the coordinates of the region.
                    reader.getLocation();
                    reader.getLocation();
                }
                else {
                    // get the chunk section info
                    reader.getBinarySerializable(RegionChunkSection.class);
                }

                // get the volume of the region
                long volume = reader.getLong();

                // Make sure the volume matches expected volume
                if (volume != getVolume()) {
                    cancel("Invalid region file. Volume mismatch.");
                    _isLoading =  false;
                }
            }
            catch (IOException | InstantiationException e) {
                e.printStackTrace();
                fail("Failed to read file header.");
            }
        }

        /**
         * Iterate block items from file
         */
        @Override
        protected void onIterateItem(int x, int y, int z) {

            Material type;
            int data;
            int light;
            int skylight;

            try {

                String typeName = reader.getSmallString();
                if (typeName == null) {
                    fail("Failed to read from file. Found a null block type in file.");
                    return;
                }

                type = EnumUtils.getEnum(typeName, Material.class);
                if (type == null) {
                    fail("Failed to read from file. Found a block type in file that is not a valid type: " + typeName);
                    return;
                }

                data = reader.getShort();

                int ls = reader.getByte();

                light = ls >> 4;
                skylight = (ls & 0x0F);
            }
            catch (IOException io) {
                io.printStackTrace();
                fail("Failed to read from file.");
                return;
            }

            if (loadType == LoadType.ALL_BLOCKS || !isBlockMatch(x, y, z, type, data)) {
                _blockInfo.add(new ChunkBlockInfo(x, y, z, type, data, light, skylight));
            }
        }

        /**
         * Read block entities and entities from file on
         * successful completion of loading blocks.
         */
        @Override
        protected void onPreComplete() {
            // Read block entities
            try {
                int totalEntities = reader.getInteger();

                for (int i=0; i < totalEntities; i++) {

                    SerializableBlockEntity state = reader.getBinarySerializable(SerializableBlockEntity.class);
                    if (state == null)
                        continue;

                    _blockEntities.push(state);
                }
            }
            catch (IOException | IllegalArgumentException | InstantiationException e) {
                e.printStackTrace();
                fail("Failed to read Block Entities from file.");
            }

            // Read entities
            try {
                int totalEntities = reader.getInteger();

                for (int i=0; i < totalEntities; i++) {

                    SerializableFurnitureEntity state = reader.getBinarySerializable(SerializableFurnitureEntity.class);
                    if (state == null)
                        continue;

                    _entities.push(state);
                }
            }
            catch (IOException | IllegalArgumentException | InstantiationException e) {
                e.printStackTrace();
                fail("Failed to read Entities from file.");
            }
        }

        @Override
        protected void onEnd() {
            if (reader != null) {
                try {
                    // close the file
                    reader.close();
                }
                catch (IOException io) {
                    cancel("Failed to close region data file.");
                    io.printStackTrace();
                }
            }

            _isLoading = false;
        }

        /*
         * Determine if the block at the specified coordinates of the chunk snapshot
         * matches the specified material type and data.
         */
        private boolean isBlockMatch(int x, int y, int z, Material type, int data) {
            Material chunkType = Material.getMaterial(snapshot.getBlockTypeId(x, y, z));
            int chunkData = snapshot.getBlockData(x, y, z);

            return chunkType == type && chunkData == data;
        }

    }
}