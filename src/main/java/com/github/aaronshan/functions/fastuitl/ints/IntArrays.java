package com.github.aaronshan.functions.fastuitl.ints;

// Note: this code was forked from fastutil (http://fastutil.di.unimi.it/)
// Copyright (C) 2010-2013 Sebastiano Vigna
public class IntArrays {
    private static void selectionSort(int[] a, int from, int to, IntComparator comp) {
        for(int i = from; i < to - 1; ++i) {
            int m = i;

            int u;
            for(u = i + 1; u < to; ++u) {
                if(comp.compare(a[u], a[m]) < 0) {
                    m = u;
                }
            }

            if(m != i) {
                u = a[i];
                a[i] = a[m];
                a[m] = u;
            }
        }
    }

    private static void swap(int[] x, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void vecSwap(int[] x, int a, int b, int n) {
        for(int i = 0; i < n; ++b) {
            swap(x, a, b);
            ++i;
            ++a;
        }

    }

    private static int med3(int[] x, int a, int b, int c, IntComparator comp) {
        int ab = comp.compare(x[a], x[b]);
        int ac = comp.compare(x[a], x[c]);
        int bc = comp.compare(x[b], x[c]);
        return ab < 0?(bc < 0?b:(ac < 0?c:a)):(bc > 0?b:(ac > 0?c:a));
    }

    public static void quickSort(int[] x, int from, int to, IntComparator comp) {
        int len = to - from;
        if(len < 7) {
            selectionSort(x, from, to, comp);
        } else {
            int m = from + len / 2;
            int v;
            int a;
            int b;
            if(len > 7) {
                v = from;
                a = to - 1;
                if(len > 50) {
                    b = len / 8;
                    v = med3(x, from, from + b, from + 2 * b, comp);
                    m = med3(x, m - b, m, m + b, comp);
                    a = med3(x, a - 2 * b, a - b, a, comp);
                }

                m = med3(x, v, m, a, comp);
            }

            v = x[m];
            a = from;
            b = from;
            int c = to - 1;
            int d = c;

            while(true) {
                int s;
                while(b > c || (s = comp.compare(x[b], v)) > 0) {
                    for(; c >= b && (s = comp.compare(x[c], v)) >= 0; --c) {
                        if(s == 0) {
                            swap(x, c, d--);
                        }
                    }

                    if(b > c) {
                        s = Math.min(a - from, b - a);
                        vecSwap(x, from, b - s, s);
                        s = Math.min(d - c, to - d - 1);
                        vecSwap(x, b, to - s, s);
                        if((s = b - a) > 1) {
                            quickSort(x, from, from + s, comp);
                        }

                        if((s = d - c) > 1) {
                            quickSort(x, to - s, to, comp);
                        }

                        return;
                    }

                    swap(x, b++, c--);
                }

                if(s == 0) {
                    swap(x, a++, b);
                }

                ++b;
            }
        }
    }
}
