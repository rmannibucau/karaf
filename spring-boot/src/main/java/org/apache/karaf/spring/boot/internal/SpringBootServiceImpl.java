/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.spring.boot.internal;

import org.apache.karaf.spring.boot.SpringBootService;
import org.apache.karaf.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.util.Optional.ofNullable;

public class SpringBootServiceImpl implements SpringBootService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpringBootServiceImpl.class);

    private File storage;
    private final ConcurrentMap<String, KarafLauncherLoader> loaders = new ConcurrentHashMap<>();

    public SpringBootServiceImpl() {
        storage = new File(new File(System.getProperty("karaf.data")), "spring-boot");
        storage.mkdirs();

        // todo: configadmin?
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "org.apache.karaf.spring.boot.embed.pax.logging.PaxLoggingSystem");
    }

    @Override
    public void install(final URI uri) throws Exception {
        LOGGER.info("Installing Spring Boot application located {}", uri);
        final Path source = Paths.get(uri);
        String fileName = source.getFileName().toString();

        LOGGER.debug("Copying {} to storage", fileName);
        File springBootJar = new File(storage, fileName);

        if (!Files.exists(source)) {
            throw new IllegalArgumentException(source + " does not exist");
        }
        // todo: digest?
        StreamUtils.copy(uri.toURL().openStream(), new FileOutputStream(springBootJar));
        getAttributes(springBootJar); // validates it is a spring boot app
        // todo: read ars from the install url and dump them in a properties next to the jar?
    }

    @Override
    public void start(final String name, final String[] args) throws Exception {
        LOGGER.info("Starting Spring Boot application {} with args {}", name, args);
        File springBootJar = new File(storage, name);
        if (!springBootJar.exists()) {
            throw new IllegalArgumentException(name + " is not installed");
        }

        final Attributes attributes = getAttributes(springBootJar);
        final String main = attributes.getValue(Attributes.Name.MAIN_CLASS);
        LOGGER.debug("Got Spring Boot Main-Class {}", main);
        if (main == null) {
            throw new IllegalArgumentException("No main in " + springBootJar);
        }
        final KarafLauncherLoader loader = new KarafLauncherLoader(springBootJar, getClass().getClassLoader());
        final Thread thread = Thread.currentThread();
        final ClassLoader old = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        loaders.put(name, loader);
        try {
            loader.launch(main, args);
        } catch (final Exception e) {
            loaders.remove(name);
            try {
                loader.close();
            } catch (final IOException ioe) {
                // no-op
            }
            throw e;
        } finally {
            thread.setContextClassLoader(old);
        }
    }

    @Override
    public void stop(final String name) {
        ofNullable(loaders.remove(name)).ifPresent(KarafLauncherLoader::destroy);
    }

    @Override
    public void stopAll() {
        loaders.keySet().forEach(this::stop);
        loaders.clear();
    }

    @Override
    public String[] list() {
        return storage.list();
    }

    private Attributes getAttributes(File source) throws IOException {
        try (JarFile jar = new JarFile(source)) {
            Manifest manifest = jar.getManifest();
            final Attributes attributes = manifest.getMainAttributes();
            if (attributes.getValue("Spring-Boot-Version") == null) {
                LOGGER.warn("Spring-Boot-Version not found in MANIFEST");
                throw new IllegalArgumentException("Invalid Spring Boot application artifact");
            }
            return attributes;
        } catch (Exception e) {
            throw e;
        }
    }
}
