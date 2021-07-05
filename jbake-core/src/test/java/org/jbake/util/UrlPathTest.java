package org.jbake.util;

import org.junit.jupiter.api.Test;

import java.net.URI;

class UrlPathTest {

    @Test
    void uri() {
        URI uri = URI.create("/").normalize();
        URI uri1 = URI.create("/././").normalize();
        URI uri2 = URI.create("/232/../").normalize();
        URI uri3 = URI.create("/abs/../").normalize();
        System.out.println(uri);
        System.out.println(uri1);
        System.out.println(uri2);
        System.out.println(uri3);
    }
}
