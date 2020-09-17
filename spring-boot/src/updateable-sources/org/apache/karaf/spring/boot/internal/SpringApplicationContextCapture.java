package org.apache.karaf.spring.boot.internal;

import org.apache.karaf.spring.boot.internal.shared.ApplicationContextCapturer;
import org.objectweb.asm.util.ASMifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class SpringApplicationContextCapture implements SpringApplicationRunListener {
    private final Object application;
    private final String[] args;
    private ConfigurableApplicationContext context;

    public SpringApplicationContextCapture(final SpringApplication application, final String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void started(final ConfigurableApplicationContext context) {
        this.context = context;
        ApplicationContextCapturer.set(this);
    }

    public static class Dumper {
        public static void main(String[] args) throws IOException {
            ASMifier.main(new String[]{
                    "target/test-classes/org/apache/karaf/spring/boot/internal/SpringApplicationContextCapture.class"});
        }
    }
}
