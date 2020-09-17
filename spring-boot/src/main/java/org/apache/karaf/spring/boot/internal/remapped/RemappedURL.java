package org.apache.karaf.spring.boot.internal.remapped;

import java.net.URLStreamHandlerFactory;

public final class RemappedURL {
    public static void setURLStreamHandlerFactory(final URLStreamHandlerFactory fac) {
        // todo: rewire it on karaf handler (/!\ it must be per spring boot app)
    }
}
