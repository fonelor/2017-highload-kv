package ru.mail.polis.rolenof;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

@Slf4j
public class KVServiceImpl implements KVService {
    private final static int SERVER_STOP_DELAY = 1;
    private final static int SERVER_BACKLOG = 10;
    private static final String PREFIX = "id=";

    private final HttpServer server;
    @NotNull
    private final KVDao kvDao;

    public KVServiceImpl(final int port, @NotNull final KVDao kvDao) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), SERVER_BACKLOG);
        this.kvDao = kvDao;
        this.server.createContext("/v0/status").setHandler(http -> {
            try {
                String method = http.getRequestMethod();
                log.info("Method is {}", method);
                http.sendResponseHeaders(200, 0);
                try (OutputStream outputStream = http.getResponseBody()) {
                    outputStream.write("ONLINE".getBytes());
                }
            } finally {
                http.close();
            }
        });

        this.server.createContext("/v0/entity").setHandler(http -> {
            try {
                final String id = extractId(http.getRequestURI().getQuery());
                final String method = http.getRequestMethod();
                log.info("Method is {}", method);
                switch (method) {
                    case "GET":
                        try (InputStream inputStream = kvDao.get(id)) {
                            http.sendResponseHeaders(200, 0);
                            try (OutputStream outputStream = http.getResponseBody()) {
                                ByteStreams.copy(inputStream, outputStream);
                            }
                        } catch (NoSuchElementException nse) {
                            http.sendResponseHeaders(404, 0);
                            log.error("File with id {} not found", id);
                        }
                        break;
                    case "PUT":
                        http.sendResponseHeaders(201, 0);
                        try (OutputStream outputStream = kvDao.put(id);
                             InputStream inputStream = http.getRequestBody()) {
                            ByteStreams.copy(inputStream, outputStream);
                        }
                        break;
                    case "DELETE":
                        http.sendResponseHeaders(202, 0);
                        kvDao.delete(id);
                        break;
                    default:
                        http.sendResponseHeaders(404, 0);
                }
            } catch (IllegalArgumentException ile){
                http.sendResponseHeaders(400, 0);
            } finally {
                http.close();
            }
        });
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(SERVER_STOP_DELAY);
    }


    @NotNull
    static String extractId(@NotNull final String query) {
        Preconditions.checkArgument(query.startsWith(PREFIX),
                "Query should start with id=");
        String id = query.substring(PREFIX.length());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
                "Id should not be null or empty");
        return id;
    }
}
