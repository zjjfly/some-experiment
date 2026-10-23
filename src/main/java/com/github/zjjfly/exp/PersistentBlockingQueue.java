package com.github.zjjfly.exp;

import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.StoreFileListener;
import net.openhft.chronicle.wire.DocumentContext;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author <a href="https://github.com/zjjfly"/>zjjfly</a>
 */
@Slf4j
public class PersistentBlockingQueue<T> {

    private final ArrayBlockingQueue<T> memoryQueue;

    private final ChronicleQueue chronicleQueue;

    private final ExcerptAppender appender;

    private final ExcerptTailer tailer;

    private volatile boolean recovering = false;

    private final String name;

    public PersistentBlockingQueue(String name, String path, int capacity) {
        this.name = name;
        this.memoryQueue = new ArrayBlockingQueue<>(capacity);
        this.chronicleQueue = ChronicleQueue.singleBuilder(path)
                .rollCycle(RollCycles.FAST_DAILY)
                .storeFileListener(new StoreFileListener() {
                    @Override
                    public void onReleased(int cycle, File file) {
                        log.info("File for persistent queue {} released: {}", name, file.getAbsolutePath());
                        try {
                            if (file.exists()) {
                                Files.delete(file.toPath());
                                log.info("File for persistent queue {} deleted: {}", name, file.getAbsolutePath());
                            }
                        } catch (Exception e) {
                            log.error("File for persistent queue " + name + " cannot be deleted: " + file.getAbsolutePath(), e);
                        }
                    }

                    @Override
                    public void onAcquired(int cycle, File file) {
                        log.info("File for persistent queue {} acquired: {}", name, file.getAbsolutePath());
                    }
                })
                .build();
        this.appender = chronicleQueue.createAppender();
        this.tailer = chronicleQueue.createTailer(name);
        recoverFromDisk();
    }

    public boolean put(T item) {
        if (recovering) {
            log.warn("Persistent queue {} is recovering,cannot put now", name);
            return false;
        }
        // 持久化到 Chronicle Queue
        try {
            memoryQueue.add(item);
        } catch (IllegalStateException e) {
            log.warn("Memory queue in persistent queue {} is full, cannot put now", this.name);
            return false;
        }
        try {
            appender.writeDocument(wire -> {
                wire.write().object(item);
            });
        } catch (Exception e) {
            memoryQueue.poll();
            return false;
        }
        return true;
    }

    public T poll() {
        if (recovering) {
            log.warn("Persistent queue {} is recovering, cannot poll now", name);
            return null;
        }
        T peek = memoryQueue.poll();
        if (peek != null) {
            tailer.readDocument(wire -> {
            });
        }
        return peek;
    }

    public void recoverFromDisk() {
        log.info("Recovering persistent queue {} from disk...", this.name);
        this.recovering = true;
        // 从 Chronicle Queue 恢复数据到内存队列
        long currentIndex = tailer.index();
        while (true) {
            DocumentContext documentContext = tailer.readingDocument();
            documentContext.rollbackOnClose();
            boolean read = tailer.readDocument(wire -> {
                Object object = wire.read().object();
                T t = (T) object;
                memoryQueue.add(t);
            });
            if (!read) {
                break;
            }
        }
        tailer.moveToIndex(currentIndex);
        this.recovering = false;
        log.info("Persistent queue {} recovered.", this.name);
    }

    public void close() {
        this.chronicleQueue.close();
        log.info("Persistent queue {} closed", this.name);
    }


}
