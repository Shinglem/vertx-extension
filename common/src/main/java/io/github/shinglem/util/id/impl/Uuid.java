package io.github.shinglem.util.id.impl;

import io.github.shinglem.util.id.IdGenerator;

import java.util.UUID;

public class Uuid implements IdGenerator {

    private static long mixHash(String str)
    {
        long hash = str.hashCode();
        hash <<= 32;
        hash |= FNVHash1(str);
        return hash;
    }

    private static int FNVHash1(String data)
    {
        final int p = 16777619;
        int hash = (int)2166136261L;
        for(int i=0;i<data.length();i++) {
            hash = (hash ^ data.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }


    @Override
    public long nextId() {
        return mixHash(next());
    }

    @Override
    public String next() {
        return UUID.randomUUID().toString();
    }
}
