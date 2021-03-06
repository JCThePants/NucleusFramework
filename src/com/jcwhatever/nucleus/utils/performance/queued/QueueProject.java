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


package com.jcwhatever.nucleus.utils.performance.queued;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.managed.scheduler.Scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A project that accepts {@link QueueTask}'s to be completed in synchronous order.
 *
 * <p>A project is a {@link QueueTask} and therefore can be added to other projects.</p>
 *
 * @see QueueTask
 * @see QueueWorker
 */
public class QueueProject extends QueueTask {

    private QueueTask _currentTask;
    private final ProjectManager _managerTask;

    // tasks to be completed
    private final Deque<QueueTask> _tasks = new ArrayDeque<>(10);

    // finished tasks
    protected Set<QueueTask> _completed;

    // cancelled tasks
    protected Set<QueueTask> _cancelled;

    // failed tasks
    protected Set<QueueTask> _failed;

    /**
     * Constructor.
     *
     * @param plugin  The owning plugin.
     */
    public QueueProject(Plugin plugin) {
        super(plugin, TaskConcurrency.CURRENT_THREAD);

        _managerTask = new ProjectManager();
    }

    /**
     * Add a task to the project.
     *
     * @param task  The task to add.
     */
    public void addTask(QueueTask task) {
        PreCon.notNull(task);

        task.setParentProject(this);

        synchronized (_tasks) {
            _tasks.add(task);
        }
    }

    /**
     * Add a collection of tasks.
     *
     * @param tasks  The tasks to add.
     *
     * @throws IllegalStateException if the project is running.
     */
    public void addTasks(Collection<? extends QueueTask> tasks) {
        PreCon.notNull(tasks);

        for (QueueTask task : tasks)
            addTask(task);
    }

    /**
     * Get all tasks in the project.
     *
     * @throws java.lang.IllegalStateException if the project is running.
     */
    public List<QueueTask> getTasks() {

        synchronized (_tasks) {
            return new ArrayList<QueueTask>(_tasks);
        }
    }

    /**
     * Run all tasks immediately.
     */
    public void runFast() {
        synchronized (_tasks) {
            while (!_tasks.isEmpty()) {
                QueueTask task = _tasks.removeFirst();
                task.run();
            }
        }
    }

    @Override
    protected void onRun() {

        _managerTask.run();
    }

    /**
     * Invoked by child tasks to notify project that the state of the
     * child has changed.
     */
    void update(QueueTask task) {

        if (task.isCancelled()) {
            if (_cancelled == null)
                _cancelled = new HashSet<>(10);

            _cancelled.add(task);
        }

        if (task.isFailed()) {
            if (_failed == null)
                _failed = new HashSet<>(10);

            _failed.add(task);
        }

        if (task.isComplete()) {
            if (_completed == null) {
                synchronized (_tasks) {
                    _completed = new HashSet<>(_tasks.size() + 1);
                }
            }

            _completed.add(task);
        }

        // ensure tasks executed independently are removed.
        if (task.isEnded()) {
            synchronized (_tasks) {
                _tasks.remove(task);
            }
        }

        _managerTask.run();
    }

    /*
     * Runnable implementation responsible for running project tasks.
     */
    private class ProjectManager implements Runnable {

        @Override
        public void run() {

            // make sure the current task is finished before
            // starting the next one
            if (_currentTask != null && !_currentTask.isEnded())
                return;

            // check if all tasks are completed
            synchronized (_tasks) {
                if (_tasks.isEmpty()) {
                    complete();
                    _currentTask = null;
                    return;
                }

                // get next item in queue
                _currentTask = _tasks.removeFirst();
            }

            // make sure the task project hasn't been cancelled
            if (_currentTask.isCancelled()) {

                _currentTask = null;
                run();
            }
            else {

                switch (_currentTask.getConcurrency()) {
                    case MAIN_THREAD:
                        if (!Bukkit.isPrimaryThread()) {

                            Scheduler.runTaskSync(_currentTask.getPlugin(), new Runnable() {

                                @Override
                                public void run() {
                                    _currentTask.run();
                                }
                            });
                            break;
                        }
                        // fall through

                    case CURRENT_THREAD:
                        _currentTask.run();
                        break;

                    case ASYNC:
                        if (Bukkit.isPrimaryThread()) {
                            Scheduler.runTaskLaterAsync(_currentTask.getPlugin(), 1, new Runnable() {
                                @Override
                                public void run() {
                                    _currentTask.run();
                                }
                            });
                        }
                        else {
                            _currentTask.run();
                        }
                        break;
                }
            }

        }
    }
}
