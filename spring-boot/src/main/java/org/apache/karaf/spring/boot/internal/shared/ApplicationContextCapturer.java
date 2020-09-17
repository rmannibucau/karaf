package org.apache.karaf.spring.boot.internal.shared;

import org.apache.karaf.spring.boot.internal.KarafLauncherLoader;

public final class ApplicationContextCapturer {
    private ApplicationContextCapturer() {
        // no-op
    }

    public static void set(final Object instance) {
        KarafLauncherLoader.class.cast(Thread.currentThread().getContextClassLoader().getParent()).setContextHolder(instance);
    }
}
