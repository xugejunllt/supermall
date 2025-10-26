package com.lanf.sms.service.manager.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训算法
 *
 */
public class RoundRobinStrategy<T> {
    private final AtomicInteger counter = new AtomicInteger(-1);
    private final List<T> items;

    public RoundRobinStrategy(List<T> items) {
        this.items = items;
    }

    public T next() {
        while (true) {
            int current = counter.incrementAndGet();
            int size = items.size();
            if (current >= size) {
                counter.set(0);
            }
            T item = items.get(current % size);
            if (item != null) {
                return item;
            }
        }
    }


}

