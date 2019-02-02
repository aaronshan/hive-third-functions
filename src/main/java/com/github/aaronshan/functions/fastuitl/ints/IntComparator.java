package com.github.aaronshan.functions.fastuitl.ints;

import java.util.Comparator;

// Note: this code was forked from fastutil (http://fastutil.di.unimi.it/)
// Copyright (C) 2010-2013 Sebastiano Vigna
public interface IntComparator extends Comparator<Integer> {
    int compare(int var1, int var2);
}
