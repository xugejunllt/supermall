package com.lanf.rocketmq.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于时间轮的延迟任务调度器（简化版 HashedWheelTimer）
 */
public class HashedWheelTimer {

    private final long tickDuration;
    private final TimeUnit timeUnit;
    private final int wheelSize;
    private final HashedWheelBucket[] wheel;
    private final AtomicLong currentTick = new AtomicLong(0);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final Queue<HashedWheelTimeout> pendingTimeouts = new ConcurrentLinkedQueue<>();
    private final AtomicLong pendingCount = new AtomicLong(0);
    private volatile Thread workerThread;
    private volatile boolean stop = false;

    /**
     * @param tickDuration 每个时间格的时间长度
     * @param unit         时间单位
     * @param wheelSize    时间轮槽数量（建议为2的幂）
     */
    public HashedWheelTimer(long tickDuration, TimeUnit unit, int wheelSize) {
        this.tickDuration = tickDuration;
        this.timeUnit = unit;
        this.wheelSize = normalizeWheelSize(wheelSize);
        this.wheel = new HashedWheelBucket[this.wheelSize];
        for (int i = 0; i < this.wheelSize; i++) {
            wheel[i] = new HashedWheelBucket();
        }
    }

    private static int normalizeWheelSize(int size) {
        // 调整为2的幂，便于取模
        int n = 1;
        while (n < size) {
            n <<= 1;
        }
        return n;
    }

    /**
     * 提交一个延迟任务
     *
     * @param task  待执行的任务（Runnable）
     * @param delay 延迟时间
     * @param unit  时间单位
     * @return Timeout 对象，可用于取消任务
     */
    public Timeout newTimeout(Runnable task, long delay, TimeUnit unit) {
        if (task == null) throw new NullPointerException("task");
        if (delay < 0) throw new IllegalArgumentException("delay must be >= 0");

        start(); // 确保工作线程已启动

        long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;
        HashedWheelTimeout timeout = new HashedWheelTimeout(task, deadline);
        pendingTimeouts.add(timeout);
        pendingCount.incrementAndGet();
        return timeout;
    }

    private volatile long startTime;

    private void start() {
        if (started.compareAndSet(false, true)) {
            workerThread = new Thread(new Worker(), "HashedWheelTimer-Worker");
            workerThread.start();
            try {
                startLatch.await(); // 等待工作线程初始化完成
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Start interrupted", e);
            }
        }
    }

    /**
     * 停止调度器，已提交但未执行的任务将被丢弃
     */
    public void stop() {
        stop = true;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    // --- 内部类：工作线程 ---
    private final class Worker implements Runnable {
        @Override
        public void run() {
            startTime = System.nanoTime();
            startLatch.countDown();

            long nextTick = currentTick.get();
            while (!stop) {
                long deadline = startTime + (nextTick + 1) * timeUnit.toNanos(tickDuration);
                try {
                    long sleepTime = deadline - System.nanoTime();
                    if (sleepTime > 0) {
                        TimeUnit.NANOSECONDS.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    if (stop) {
                        break;
                    }
                }

                // 推进指针
                long tick = currentTick.incrementAndGet();
                // 处理新提交的任务
                transferPendingTimeouts();
                // 处理当前槽的到期任务
                HashedWheelBucket bucket = wheel[(int) (tick & (wheelSize - 1))];
                bucket.expireTimeouts(tick);
                nextTick = tick;
            }
        }

        private void transferPendingTimeouts() {
            // 每次最多处理10万条，避免长时间阻塞
            for (int i = 0; i < 100000; i++) {
                HashedWheelTimeout timeout = pendingTimeouts.poll();
                if (timeout == null) break;
                if (timeout.isCancelled()) {
                    continue;
                }
                long deadline = timeout.deadline;
                long delay = deadline - startTime;
                if (delay <= 0) {
                    // 已过期，立即执行
                    timeout.expire();
                } else {
                    long ticks = delay / timeUnit.toNanos(tickDuration);
                    timeout.remainingRounds = ticks / wheelSize;
                    long stopIndex = (currentTick.get() + ticks) & (wheelSize - 1);
                    HashedWheelBucket bucket = wheel[(int) stopIndex];
                    bucket.addTimeout(timeout);
                }
            }
        }
    }

    // --- 内部类：时间槽 ---
    private static final class HashedWheelBucket {
        private HashedWheelTimeout head;
        private HashedWheelTimeout tail;

        void addTimeout(HashedWheelTimeout timeout) {
            timeout.bucket = this;
            if (head == null) {
                head = tail = timeout;
            } else {
                tail.next = timeout;
                timeout.prev = tail;
                tail = timeout;
            }
        }

        void expireTimeouts(long tick) {
            HashedWheelTimeout timeout = head;
            while (timeout != null) {
                HashedWheelTimeout next = timeout.next;
                if (timeout.remainingRounds <= 0) {
                    timeout.remove();
                    timeout.expire();
                } else {
                    timeout.remainingRounds--;
                }
                timeout = next;
            }
        }

        void remove(HashedWheelTimeout timeout) {
            HashedWheelTimeout prev = timeout.prev;
            HashedWheelTimeout next = timeout.next;
            if (prev != null) {
                prev.next = next;
            } else {
                head = next;
            }
            if (next != null) {
                next.prev = prev;
            } else {
                tail = prev;
            }
            timeout.prev = null;
            timeout.next = null;
            timeout.bucket = null;
        }
    }

    // --- 内部类：任务句柄，同时作为链表节点 ---
    private final class HashedWheelTimeout implements Timeout {
        private final Runnable task;
        private final long deadline;
        volatile long remainingRounds;
        volatile HashedWheelBucket bucket;
        HashedWheelTimeout prev;
        HashedWheelTimeout next;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicBoolean expired = new AtomicBoolean(false);

        HashedWheelTimeout(Runnable task, long deadline) {
            this.task = task;
            this.deadline = deadline;
        }

        @Override
        public boolean cancel() {
            if (!cancelled.compareAndSet(false, true)) {
                return false;
            }
            // 如果尚未加入时间轮，直接从pending队列移除（通过标记忽略）
            // 如果已经在bucket中，则从链表中移除
            HashedWheelBucket b = bucket;
            if (b != null) {
                b.remove(this);
            }
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public boolean isExpired() {
            return expired.get();
        }

        void expire() {
            if (!expired.compareAndSet(false, true)) {
                return;
            }
            if (cancelled.get()) {
                return;
            }
            try {
                task.run();
            } catch (Throwable t) {
                // 实际使用时可以接入日志
                t.printStackTrace();
            }
        }

        void remove() {
            HashedWheelBucket b = bucket;
            if (b != null) {
                b.remove(this);
            }
        }
    }

    // --- 对外接口 ---
    public interface Timeout {
        boolean cancel();
        boolean isCancelled();
        boolean isExpired();
    }

    // --- 示例用法 ---
    public static void main(String[] args) throws Exception {
        HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

        // 提交三个延迟消息
        timer.newTimeout(() -> System.out.println("Task 1: 1 second delay"), 1, TimeUnit.SECONDS);
        Timeout timeout2 = timer.newTimeout(() -> System.out.println("Task 2: 3 seconds delay"), 3, TimeUnit.SECONDS);
        timer.newTimeout(() -> System.out.println("Task 3: 5 seconds delay"), 5, TimeUnit.SECONDS);

        // 取消任务2
        Thread.sleep(500);
        boolean cancelled = timeout2.cancel();
        System.out.println("Task 2 cancelled: " + cancelled);

        // 运行一段时间后停止
        Thread.sleep(6000);
        timer.stop();
        System.out.println("Timer stopped.");
    }
}