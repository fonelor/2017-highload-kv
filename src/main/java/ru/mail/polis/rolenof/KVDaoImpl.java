package ru.mail.polis.rolenof;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class KVDaoImpl implements KVDao {

    private static final HashFunction HASHING = Hashing.goodFastHash(12);

    @NotNull
    private final File storeDir;

    @NotNull
    @Override
    public InputStream get(@NotNull String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        Path resolvedPath = resolveKey(key);
        if (!Files.exists(resolvedPath)) {
            throw new NoSuchElementException("No such file " +
                    resolvedPath.toString());
        }
        final File file = resolvedPath.toFile();
        return new FileInputStream(file);
    }

    @NotNull
    @Override
    public OutputStream put(@NotNull String key) throws IllegalArgumentException, IOException {
        Path resolvedKey = resolveKey(key);
        return new FileOutputStream(resolvedKey.toFile(), false);
    }

    @Override
    public void delete(@NotNull String key) throws IllegalArgumentException, IOException {
        Path resolvedKey = resolveKey(key);
        if (resolvedKey.toFile().exists()) {
            Files.delete(resolvedKey);
        }
    }

    @NotNull
    private Path resolveKey(@NotNull String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "Invalid key");
        String fileKey = HASHING.hashBytes(key.getBytes()).toString();
        return storeDir.toPath().resolve(fileKey);
    }
}
