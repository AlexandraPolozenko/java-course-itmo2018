package ru.ifmo.rain.polozenko.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.*;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private int perHost;

    private ExecutorService downloadersPool, extractorsPool;
    private ConcurrentHashMap<String, Semaphore> hosts;
    private ConcurrentHashMap<String, Document> downloadedPages;
    private ConcurrentHashMap<String, List<String>> parsedPages;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        downloadersPool = Executors.newFixedThreadPool(downloaders);
        extractorsPool = Executors.newFixedThreadPool(extractors);
        hosts = new ConcurrentHashMap<>();
        downloadedPages = new ConcurrentHashMap<>();
        parsedPages = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.out.println("5 arguments expected");
            return;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                System.out.println("non-null arguments expected");
            }
        }

        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(), Integer.parseInt(args[2]),
                Integer.parseInt(args[3]), Integer.parseInt(args[4]))) {
            crawler.download(args[0], Integer.parseInt(args[1]));
        } catch (IOException e) {
            System.out.println("Unable to create instance of CachingDownloader: " + e.getMessage());
        }
    }


    @Override
    public Result download(String url, int depth) {
        Set<String> pos = Collections.newSetFromMap(new ConcurrentHashMap<>());
        ConcurrentHashMap<String, IOException> neg = new ConcurrentHashMap<>();
        HashSet<String> visited = new HashSet<>();
        ArrayDeque<String> tmp = new ArrayDeque<>(depth);
        ArrayDeque<String> que = new ArrayDeque<>(depth);
        que.add(url);
        visited.add(url);
        int curDepth = 1;

        while (!que.isEmpty() && curDepth < depth) {
            que.stream()
                    .map(link -> toCallableLinks(link, downloadersPool.submit(() -> getPage(link, pos, neg))))
                    .map(extractorsPool::submit)
                    .collect(Collectors.toList()).stream()
                    .map(WebCrawler::safeGetLinks)
                    .flatMap(Collection::stream)
                    .forEach(link -> {
                        if (!visited.contains(link)) {
                            tmp.add(link);
                            visited.add(link);
                        }
                    });
            que.clear();
            que.addAll(tmp);
            tmp.clear();
            curDepth++;
        }
        if (!que.isEmpty()) {
            que.stream()
                    .map(link -> toCallablePage(link, pos, neg))
                    .map(downloadersPool::submit)
                    .collect(Collectors.toList())
                    .forEach((elem) -> {
                        try {
                            elem.get();
                        } catch (ExecutionException | InterruptedException ignored) {
                        }
                    });
        }
        return new Result(new ArrayList<>(pos), neg);
    }

    @Override
    public void close() {
        downloadersPool.shutdown();
        extractorsPool.shutdown();
    }

    private Document downloadPage(final String url) throws IOException {
        Semaphore semaphore = null;

        try {
            String host = URLUtils.getHost(url);
            hosts.putIfAbsent(host, new Semaphore(perHost));
            semaphore = hosts.get(host);
            semaphore.acquireUninterruptibly();
            Document res = downloader.download(url);
            downloadedPages.putIfAbsent(url, res);
            return res;
        } finally {
            if (semaphore != null) {
                semaphore.release();
            }
        }
    }

    private Optional<Document> getPage(String url, Set<String> pos, Map<String, IOException> neg) {
        Document res = null;

        if (!downloadedPages.containsKey(url)) {
            try {
                res = downloadPage(url);
                pos.add(url);
            } catch (IOException e) {
                neg.put(url, e);
            }
        } else {
            res = downloadedPages.get(url);
            pos.add(url);
        }
        return Optional.ofNullable(res);
    }

    private List<String> getLinks(String url, Future<Optional<Document>> page) {
        if (!parsedPages.containsKey(url)) {
            try {
                return page.get().map((doc) -> {
                    try {
                        return doc.extractLinks();
                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(Collections.emptyList());
            } catch (InterruptedException | ExecutionException e) {
                return Collections.emptyList();
            }
        } else {
            return parsedPages.get(url);
        }
    }

    private Callable<List<String>> toCallableLinks(final String url, final Future<Optional<Document>> page) {
        return () -> getLinks(url, page);
    }

    private Callable<Optional<Document>> toCallablePage(final String url, final Set<String> good, final Map<String, IOException> bad) {
        return () -> getPage(url, good, bad);
    }

    private static List<String> safeGetLinks(Future<List<String>> elem) {
        try {
            return elem.get();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
