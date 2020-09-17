package org.apache.karaf.spring.boot.internal.shared;

import org.apache.karaf.spring.boot.internal.KarafLauncherLoader;

public final class ApplicationContextCapturer {
    private ApplicationContextCapturer() {
        // no-op
    }

    public static void set(final Object instance) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        while (!KarafLauncherLoader.class.isInstance(loader)) {
            loader = loader.getParent();
        }
        KarafLauncherLoader.class.cast(loader).setContextHolder(instance);
    }
}
