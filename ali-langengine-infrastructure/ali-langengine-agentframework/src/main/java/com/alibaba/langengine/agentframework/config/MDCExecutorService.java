/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.agentframework.config;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MDCExecutorService implements ExecutorService {

    private final ExecutorService delegate;

    public MDCExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(MDCTaskDecorator.decorate(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(MDCTaskDecorator.decorate(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(MDCTaskDecorator.decorate(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(tasks.stream()
                .map(MDCTaskDecorator::decorate)
                .collect(Collectors.toList()));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(tasks.stream()
                .map(MDCTaskDecorator::decorate)
                .collect(Collectors.toList()), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(tasks.stream()
                .map(MDCTaskDecorator::decorate)
                .collect(Collectors.toList()));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(tasks.stream()
                .map(MDCTaskDecorator::decorate)
                .collect(Collectors.toList()), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(MDCTaskDecorator.decorate(command));
    }
}