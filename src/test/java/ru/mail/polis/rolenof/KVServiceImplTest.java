package ru.mail.polis.rolenof;

import org.junit.Test;

import static org.junit.Assert.*;

public class KVServiceImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void extractIdIncorrectArg() {
        KVServiceImpl.extractId("111?id=");
    }

    @Test
    public void extractId() {
        assertEquals("1", KVServiceImpl.extractId("id=1"));
    }
}