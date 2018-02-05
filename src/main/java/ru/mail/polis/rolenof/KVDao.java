package ru.mail.polis.rolenof;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;

interface KVDao {
    @NotNull
    InputStream get(@NotNull String key) throws NoSuchElementException, IllegalArgumentException, IOException;

    @NotNull
    OutputStream put(@NotNull String key) throws IllegalArgumentException, IOException;

    void delete(@NotNull String key) throws IllegalArgumentException, IOException;

}
