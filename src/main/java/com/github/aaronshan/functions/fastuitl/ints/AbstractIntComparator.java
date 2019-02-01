package com.github.aaronshan.functions.fastuitl.ints;

// Note: this code was forked from fastutil (http://fastutil.di.unimi.it/)
// Copyright (C) 2010-2013 Sebastiano Vigna
public abstract class AbstractIntComparator implements IntComparator {
    protected AbstractIntComparator() {
    }

    public int compare(Integer ok1, Integer ok2) {
        return this.compare(ok1.intValue(), ok2.intValue());
    }

    public abstract int compare(int var1, int var2);
}
