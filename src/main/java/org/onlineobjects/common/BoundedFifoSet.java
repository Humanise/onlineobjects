package org.onlineobjects.common;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public final class BoundedFifoSet<E> {
    private final int maxSize;
    private final LinkedHashSet<E> set = new LinkedHashSet<>();
    private final ReentrantLock lock = new ReentrantLock();

    public BoundedFifoSet(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.maxSize = maxSize;
    }

    public boolean add(E value) {
        lock.lock();
        try {
            // A duplicate does not change insertion order or evict anything.
            if (!set.add(value)) {
                return false;
            }

            if (set.size() > maxSize) {
                var iterator = set.iterator();
                iterator.next();
                iterator.remove();
            }

            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(E value) {
        lock.lock();
        try {
            return set.remove(value);
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(E value) {
        lock.lock();
        try {
            return set.contains(value);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return set.size();
        } finally {
            lock.unlock();
        }
    }

    public Set<E> snapshot() {
        lock.lock();
        try {
            return Collections.unmodifiableSet(new LinkedHashSet<>(set));
        } finally {
            lock.unlock();
        }
    }
}
