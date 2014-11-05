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


package com.jcwhatever.bukkit.generic.performance.queued;

import com.jcwhatever.bukkit.generic.utils.Scheduler;
import org.bukkit.plugin.Plugin;

/**
 * Abstract queue worker task designed to add functionality
 * to an implementation making it easier to perform iterative tasks.
 */
public abstract class IterationTask extends QueueTask {

    private int _start, _end, _increment, _segmentSize;
    private int _current, _segmentsCompleted, _iterations;

    private boolean _lessThan;
    private final Object _sync = new Object();
    private int _delay = 10;


    /**
     * Constructor. Initializes iteration variables.
     *
     * @param plugin       The owning plugin
     * @param segmentSize  The number of times to iterate before pausing
     * @param start        The number to start iteration at
     * @param end          The number+1 to end iteration at
     * @param increment    The increment per iteration. Use negative numbers if start is larger than end value
     */
    public IterationTask(Plugin plugin, TaskConcurrency concurrency,
                         int segmentSize, int start, int end, int increment) {
        super(plugin, concurrency);

        _current = _start;
        _start = start;
        _end = end;
        _increment = increment;
        _segmentSize = segmentSize;
        _lessThan = _start < _end || _start == _end && _increment > 0;
    }

    /**
     * Get the start index
     */
    public final int getStart() {
        return _start;
    }

    /**
     * Get the index that will be stopped at.
     */
    public final int getEnd() {
        return _end - 1;
    }

    /**
     * Get the increment per iteration
     */
    public final int getIncrement() {
        return _increment;
    }

    /**
     * Get the number of iteration segments completed.
     */
    public final int getSegmentsCompleted() {
        return _segmentsCompleted;
    }

    /**
     * Get the number of iterations completed.
     */
    public final int getIterations() {
        return _iterations;
    }

    @Override
    protected final void onRun() {
        onIterateBegin();
        Scheduler.runTaskLater(getPlugin(), _delay, new Worker());
    }

    /**
     * Called at each iteration.
     *
     * @param index  The current iteration index
     */
    protected abstract void onIterateItem(int index);

    /**
     * Called when the task begins.
     */
    protected void onIterateBegin() {}

    /**
     * Called before the first iteration of a segment.
     *
     * @param index  The current iteration index
     */
    protected void onSegmentStart(int index) {}

    /**
     * Called after the last iteration of a segment.
     *
     * @param index  The current iteration index
     */
    protected void onSegmentEnd(int index) {}

    /**
     * Called just before the task finishes.
     */
    protected void onPreFinish() {}



    // the worker responsible for iterating
    private class Worker implements Runnable {

        @Override
        public void run() {

            synchronized (_sync) {

                boolean isStart = true;
                int completed = 0;

                for (int i = _current; _lessThan ? i < _end : i > _end; i += _increment) {

                    // determine if this is the start of the segment
                    if (isStart) {
                        onSegmentStart(i);
                    }

                    // determine if this is the end of the segment
                    if (_segmentSize > 0 && completed >= _segmentSize) {
                        _segmentsCompleted ++;

                        _current = i;

                        onSegmentEnd(i);

                        // schedule next segment
                        Scheduler.runTaskLater(getPlugin(), _delay, this);
                        return;
                    }

                    isStart = false;

                    onIterateItem(i);

                    if (!isRunning())
                        return;

                    _iterations++;
                    completed++;

                }

                onPreFinish();
            }
            complete();

        }
    }
}
