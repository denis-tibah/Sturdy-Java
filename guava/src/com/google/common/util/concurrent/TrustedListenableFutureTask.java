/*
 * Copyright (C) 2014 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.util.concurrent;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

import javax.annotation.Nullable;

/**
 * A {@link RunnableFuture} that also implements the {@link ListenableFuture}
 * interface.
 * 
 * <p>This should be used in preference to {@link ListenableFutureTask} when possible for 
 * performance reasons.
 */
@GwtCompatible
class TrustedListenableFutureTask<V> extends AbstractFuture.TrustedFuture<V>
    implements RunnableFuture<V> {

  /**
   * Creates a {@code ListenableFutureTask} that will upon running, execute the
   * given {@code Callable}.
   *
   * @param callable the callable task
   */
  static <V> TrustedListenableFutureTask<V> create(Callable<V> callable) {
    return new TrustedListenableFutureTask<V>(callable);
  }

  /**
   * Creates a {@code ListenableFutureTask} that will upon running, execute the
   * given {@code Runnable}, and arrange that {@code get} will return the
   * given result on successful completion.
   *
   * @param runnable the runnable task
   * @param result the result to return on successful completion. If you don't
   *     need a particular result, consider using constructions of the form:
   *     {@code ListenableFuture<?> f = ListenableFutureTask.create(runnable,
   *     null)}
   */
  static <V> TrustedListenableFutureTask<V> create(
      Runnable runnable, @Nullable V result) {
    return new TrustedListenableFutureTask<V>(Executors.callable(runnable, result));
  }

  private TrutestedFutureInterruptibleTask task;

  TrustedListenableFutureTask(Callable<V> callable) {
    this.task = new TrutestedFutureInterruptibleTask(callable);
  }

  @Override public void run() {
    TrutestedFutureInterruptibleTask localTask = task;
    if (localTask != null) {
      localTask.run();
    }
  }

  @Override void done() {
    super.done();

    // Free all resources associated with the running task
    this.task = null;
  }

  @GwtIncompatible("Interruption not supported")
  @Override protected final void interruptTask() {
    TrutestedFutureInterruptibleTask localTask = task;
    if (localTask != null) {
      localTask.interruptTask();
    }
  }

  /**
   * Template method for calculating and setting the value. Guaranteed to be called at most once.
   *
   * <p>Extracted as an extension point for subclasses that wish to modify behavior.
   * See Futures.combine (which has specialized exception handling).
   */
  void doRun(Callable<V> localTask) throws Exception {
    set(localTask.call());
  }

  final class TrutestedFutureInterruptibleTask extends InterruptibleTask {
    private final Callable<V> callable;

    TrutestedFutureInterruptibleTask(Callable<V> callable) {
      this.callable = checkNotNull(callable);
    }

    @Override void runInterruptibly() {
      // Ensure we haven't been cancelled or already run.
      if (!isDone()) {
        try {
          doRun(callable);
        } catch (Throwable t) {
          setException(t);
        } 
      }
    }

    @Override boolean wasInterrupted() {
      return TrustedListenableFutureTask.this.wasInterrupted();
    }
  }
}
