package ru.ifmo.rain.polozenko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class IterativeParallelism implements ListIP {

    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return go(threads, list,
                x -> x.stream().filter(predicate).collect(Collectors.toList()),
                x -> x.stream().flatMap(Collection::stream).collect(Collectors.toList())
        );
    }

    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        return go(threads, list,
                x -> x.stream().map(Object::toString).collect(Collectors.joining()),
                x -> x.stream().collect(Collectors.joining())
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return go(threads, list,
                x -> x.stream().map(function).collect(Collectors.toList()),
                x -> x.stream().flatMap(Collection::stream).collect(Collectors.toList())
        );
    }

    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, list, comparator.reversed());
    }

    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator)
            throws InterruptedException {
        return go(threads, list, x -> Collections.max(x, comparator), y -> Collections.max(y, comparator));
    }

    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate)
            throws InterruptedException {
        return go(threads, list, x -> x.stream().allMatch(predicate), y -> y.stream().allMatch(z -> z));
    }

    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate)
            throws InterruptedException {
        return go(threads, list, x -> x.stream().anyMatch(predicate), y -> y.stream().anyMatch(z -> z));
    }

    private void threadsGo(Thread[] thread) throws InterruptedException {
        Exception ex = null;

        for (Thread i : thread) {
            try {
                i.join();
            } catch (InterruptedException e) {
                ex = e;
            }
        }

        if (ex != null) {
            throw new InterruptedException("");
        }
    }

    private <T> void check(int threads, List<? extends T> list) {
        if (threads <= 0 || list == null) {
            throw new IllegalArgumentException("Not enough threads or arguments");
        }
    }

    private <T, R> R go(int threads, List<? extends T> list, Function<List<? extends T>, R> func, Function<List<? extends R>, R> res)
            throws InterruptedException {
        check(threads, list);

        threads = Math.min(threads, list.size());
        final int size = list.size() / threads;
        int ost = list.size() % threads, ind = 0;
        final List<R> ans = new ArrayList<>(Collections.nCopies(threads, null));
        Thread[] thread = new Thread[threads];


        for (int i = 0; i < threads; i++) {
            final int j = i, r, l = ind;
            int tmp = Math.min(l + size, list.size());

            if (ost > 0) {
                r = tmp + 1;
                ost--;
            } else {
                r = tmp;
            }
            ind = r;

            thread[i] = new Thread(() -> ans.set(j, func.apply(list.subList(l, r))));

            thread[i].start();
        }

        threadsGo(thread);

        return res.apply(ans);
    }
}




/*import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {
    private final ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
        mapper = null;
    }

    private <T, R> R baseTask(int threads, final List<? extends T> values,
                              final Function<? super Stream<? extends T>, ? extends R> task,
                              final Function<? super Stream<? extends R>, ? extends R> ansCollector)
            throws InterruptedException {
        if (threads <= 0) {
            throw new IllegalArgumentException("Number of threads must be positive");
        }

        threads = Math.max(1, Math.min(threads, values.size()));
        final List<Stream<? extends T>> subTasks = new ArrayList<>();
        final int blockSize = values.size() / threads;
        int rest = values.size() % threads;
        int pr = 0;
        for (int i = 0; i < threads; i++) {
            final int l = pr;
            final int r = l + blockSize + (rest-- > 0 ? 1 : 0);
            pr = r;
            subTasks.add(values.subList(l, r).stream());
        }

        List<R> res;
        if (mapper != null) {
            res = mapper.map(task, subTasks);
        } else {
            final List<Thread> workers = new ArrayList<>();
            res = new ArrayList<>(Collections.nCopies(threads, null));
            for (int i = 0; i < threads; i++) {
                final int pos = i;
                addAndStart(workers, new Thread(() -> res.set(pos, task.apply(subTasks.get(pos)))));
            }
            joinThreads(workers);
        }
        return ansCollector.apply(res.stream());
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Unable to handle empty list");
        }
        final Function<Stream<? extends T>, ? extends T> streamMax = stream -> stream.max(comparator).get();
        return baseTask(threads, values, streamMax, streamMax);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return baseTask(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue)
        );
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, elem -> !predicate.test(elem));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return baseTask(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining())
        );
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return baseTask(threads, values,
                stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList())
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return baseTask(threads, values,
                stream -> stream.map(f).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList())
        );
    }

    private void addAndStart(List<Thread> workers, Thread thread) {
        workers.add(thread);
        thread.start();
    }

    private void joinThreads(final List<Thread> threads) throws InterruptedException {
        InterruptedException exception = null;
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                if (exception == null) {
                    exception = new InterruptedException("Not all threads joined");
                }
                exception.addSuppressed(e);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}*/