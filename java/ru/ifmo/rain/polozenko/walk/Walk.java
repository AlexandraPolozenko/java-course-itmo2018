package ru.ifmo.rain.polozenko.walk;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Walk {

    public static void main(String[] args) {
        Walk walk = new Walk();

        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Invalid arguments");
        } else {
            walk.go(args[0], args[1]);
        }
    }

    private void go(String r, String w) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(r), StandardCharsets.UTF_8)) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(w), StandardCharsets.UTF_8)) {
                String s;

                try {
                    while ((s = reader.readLine()) != null && s.length() > 0) {
                        try {
                            writer.write(String.format("%08x", fnv(s)) + " " + s + "\n");
                        } catch (IOException e) {
                            System.err.println("Unable to write in file");
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Unable to read the line");
                }

            } catch (NoSuchFileException e) {
                System.err.println("Output file doesn't exist");
            } catch (InvalidPathException e) {
                System.err.println("Invalid output file path");
            } catch (IOException | SecurityException e) {
                System.err.println("Output file error");
            }
        } catch (NoSuchFileException e) {
            System.err.println("Input file doesn't exist");
        } catch (InvalidPathException e) {
            System.err.println("Invalid input file path");
        } catch (IOException | SecurityException e) {
            System.err.println("Input file error");
        }
    }

    private int fnv(String s) {
        try (FileInputStream cnt = new FileInputStream(s)) {
            int hval = 0x811c9dc5;
            byte[] buf = new byte[4096];
            int n, i;

            n = cnt.read(buf);
            while (n != -1) {
                for (i = 0; i < n; i++) {
                    hval = (hval * 0x01000193) ^ (buf[i] & 0xff);
                }
                n = cnt.read(buf);
            }
            return hval;
        } catch (IOException e) {
            return 0;
        }
    }
}


/*import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class WebCrawler implements Crawler {
    private Downloader downloader;
    private int perHost;

    private ExecutorService downloaders, extractors;
    final private Map<String, HostInfo> hosts = new HashMap<>();


    public WebCrawler(Downloader downloader, int downloaderN, int extractorN, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;

        downloaders = Executors.newFixedThreadPool(downloaderN);
        extractors = Executors.newFixedThreadPool(extractorN);
    }


    @Override
    public Result download(String url, int depth) {
        ArrayList<String> downloaded = new ArrayList<>();
        HashMap<String, IOException> errors = new HashMap<>();

        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(url);
        HashSet<String> enqueued = new HashSet<>();
        enqueued.add(url);

        // Process each depth level separately
        for (int depthI = 0; depthI < depth; ++depthI) {
            int currentDepth = depthI;
            ArrayDeque<String> currentQueue = queue;
            ArrayDeque<String> nextQueue = new ArrayDeque<>();
            CountDownLatch countDown = new CountDownLatch(currentQueue.size());

            for (String nextUrl : currentQueue) {
                String host;
                try {
                    host = URLUtils.getHost(nextUrl);
                    synchronized (hosts) {
                        //noinspection Java8CollectionsApi
                        if (hosts.get(host) == null) {
                            hosts.put(host, new HostInfo());
                        }
                    }
                } catch (MalformedURLException e) {
                    //noinspection ThrowableResultOfMethodCallIgnored
                    errors.put(nextUrl, e);
                    continue;
                }

                final HostInfo hostInfo = getHost(host);

                class TaskFactory {
                    // called only while owning hostInfo's monitor
                    private Runnable produce() {
                        String currentUrl = hostInfo.queue.remove();
                        return () -> {
                            // Download document
                            Document document;
                            try {
                                document = downloader.download(currentUrl);
                                synchronized (downloaded) {
                                    downloaded.add(currentUrl);
                                }
                            } catch (IOException e) {
                                synchronized (errors) {
                                    //noinspection ThrowableResultOfMethodCallIgnored
                                    errors.put(currentUrl, e);
                                }
                                countDown.countDown();
                                return;
                            } finally {
                                synchronized (hostInfo) {
                                    --hostInfo.runningThreads;
                                    if (hostInfo.queue.size() > 0) {
                                        ++hostInfo.runningThreads;
                                        downloaders.submit(this.produce());
                                    }
                                }
                            }

                            if (currentDepth == depth - 1) {
                                countDown.countDown();
                            } else {
                                // Extract links
                                extractors.submit(() -> {
                                    List<String> links;
                                    try {
                                        links = document.extractLinks();
                                    } catch (IOException e) {
                                        synchronized (errors) {
                                            //noinspection ThrowableResultOfMethodCallIgnored
                                            errors.put(currentUrl, e);
                                        }
                                        countDown.countDown();
                                        return;
                                    }

                                    for (String link : links) {
                                        boolean flag = false;
                                        synchronized (enqueued) {
                                            flag = enqueued.add(link);
                                        }
                                        if (flag) {
                                            synchronized (nextQueue) {
                                                nextQueue.add(link);
                                            }
                                        }
                                    }

                                    countDown.countDown();
                                });
                            }
                        };
                    }
                }

                synchronized (hostInfo) {
                    hostInfo.queue.add(nextUrl);
                    if (hostInfo.runningThreads == perHost) {
                        continue;
                    }
                    ++hostInfo.runningThreads;
                    downloaders.submit(new TaskFactory().produce());
                }
            }

            try {
                countDown.await();
            } catch (InterruptedException ignored) {
            }

            queue = nextQueue;
        }

        return new Result(downloaded, errors);
    }


    @Override
    public void close() {
            downloaders.shutdownNow();
            extractors.shutdownNow();
    }

    private HostInfo getHost(String host) {
        synchronized (hosts) {
            return hosts.get(host);
        }
    }

    private class HostInfo {
        final Queue<String> queue = new ArrayDeque<>();
        volatile int runningThreads = 0;
    }
}
*/
