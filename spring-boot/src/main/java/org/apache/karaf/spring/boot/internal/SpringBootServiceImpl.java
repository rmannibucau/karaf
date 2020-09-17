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
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class SpringBootServiceImpl implements SpringBootService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpringBootServiceImpl.class);

    private File storage;

    public SpringBootServiceImpl() {
        storage = new File(new File(System.getProperty("karaf.data")), "spring-boot");
        storage.mkdirs();
    }

    @Override
    public void install(URI uri) throws Exception {
        LOGGER.info("Installing Spring Boot application located {}", uri);
        String fileName = Paths.get(uri).getFileName().toString();

        LOGGER.debug("Copying {} to storage", fileName);
        File springBootJar = new File(storage, fileName);

        StreamUtils.copy(uri.toURL().openStream(), new FileOutputStream(springBootJar));
        try (JarFile jar = new JarFile(springBootJar)) {
            Manifest manifest = jar.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            if (attributes.getValue("Spring-Boot-Version") == null) {
                springBootJar.delete();
                LOGGER.warn("Spring-Boot-Version not found in MANIFEST");
                throw new IllegalArgumentException("Invalid Spring Boot application artifact");
            }

            LOGGER.debug("Exploding Spring Boot application to {}-exploded", fileName);
            File explodedLocation = new File(storage, fileName + "-exploded");
            explodedLocation.mkdirs();
            try {
                try (InputStream inputStream = springBootJar.toURI().toURL().openStream()) {
                    try (JarInputStream jarInputStream = new JarInputStream(inputStream)) {
                        ZipEntry zipEntry = jarInputStream.getNextEntry();
                        while (zipEntry != null) {
                            String path = zipEntry.getName();
                            if (!path.contains("..")) {
                                File destFile = new File(explodedLocation, path);
                                if (zipEntry.isDirectory()) {
                                    destFile.mkdirs();
                                } else {
                                    destFile.getParentFile().mkdirs();
                                    try (FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
                                        byte[] buffer = new byte[8192];
                                        int n;
                                        while (-1 != (n = jarInputStream.read(buffer))) {
                                            fileOutputStream.write(buffer, 0, n);
                                        }
                                    }
                                }
                            }
                            zipEntry = jarInputStream.getNextEntry();
                        }
                    }
                }
            } catch (Exception e) {
                springBootJar.delete();
                explodedLocation.delete();
                throw e;
            }
        }
    }

    @Override
    public void start(String name, String[] args) throws Exception {
        LOGGER.info("Starting Spring Boot application {} with args {}", name, args);
        File springBootJar = new File(storage, name);
        File springBootJarExploded = new File(storage, name + "-exploded");
        if (!springBootJarExploded.exists()) {
            throw new IllegalArgumentException(name + " is not installed");
        }

        LOGGER.debug("Getting Spring Boot Main-Class");
        try (JarFile jar = new JarFile(springBootJar)) {
            Manifest manifest = jar.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            String startClass;
            if (attributes.getValue("Start-Class") != null) {
                startClass = attributes.getValue("Start-Class");
            } else {
                throw new IllegalStateException("No Spring Boot Start-Class found in MANIFEST");
            }
            LOGGER.debug("Spring Boot start class: {}", startClass);

            LOGGER.debug("Getting Spring Boot classes folder");
            String classesFolder;
            if (attributes.getValue("Spring-Boot-Classes") != null) {
                classesFolder = attributes.getValue("Spring-Boot-Classes");
            } else {
                throw new IllegalStateException("No Spring-Boot-Classes found in MANIFEST");
            }

            LOGGER.debug("Getting Spring Boot lib folder");
            String libFolder;
            if (attributes.getValue("Spring-Boot-Lib") != null) {
                libFolder = attributes.getValue("Spring-Boot-Lib");
            } else {
                throw new IllegalStateException("No Spring-Boot-Lib found in MANIFEST");
            }

            LOGGER.debug("Constructing Spring Boot classloader");
            List<URL> classloaderLocations = new ArrayList<>();
            classloaderLocations.add(new File(springBootJarExploded, classesFolder).toURI().toURL());
            File libFolderFile = new File(springBootJarExploded, libFolder);
            for (File jarFile : libFolderFile.listFiles()) {
                classloaderLocations.add(jarFile.toURI().toURL());
            }

            URLClassLoader classLoader = new URLClassLoader(name + "-CLASSLOADER", classloaderLocations.toArray(new URL[]{}), this.getClass().getClassLoader().getParent());

            LOGGER.debug("Launching start class");
            Class<?> mainClass = Class.forName(startClass, false, classLoader);
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            mainMethod.setAccessible(true);
            mainMethod.invoke(null, new Object[]{args});
            //classLoader.loadClass(startClass).getMethod("main", String[].class).invoke(null, new Object[]{args});
        }
    }

    @Override
    public String[] list() throws Exception {
        return storage.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (!name.contains("exploded"));
            }
        });
    }

}
