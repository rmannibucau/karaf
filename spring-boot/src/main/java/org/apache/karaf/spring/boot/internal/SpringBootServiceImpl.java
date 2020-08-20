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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class SpringBootServiceImpl implements SpringBootService {

    private File storage;

    public SpringBootServiceImpl() {
        storage = new File(new File(System.getProperty("karaf.data")), "spring-boot");
        storage.mkdirs();
    }

    @Override
    public void install(URI uri) throws Exception {
        // copy jar
        String fileName = Paths.get(uri).getFileName().toString();

        File springBootJar = new File(storage, fileName);

        StreamUtils.copy(uri.toURL().openStream(), new FileOutputStream(springBootJar));
        JarFile jar = new JarFile(springBootJar);
        Manifest manifest = jar.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        if (attributes.getValue("Spring-Boot-Version") == null) {
            springBootJar.delete();
            throw new IllegalArgumentException("Invalid Spring Boot artifact");
        }
    }

    @Override
    public void start(String name, String[] args) throws Exception {

        File springBootJar = new File(storage, name);
        if (!springBootJar.exists()) {
            throw new IllegalArgumentException(name + " is not installed");
        }
        URLClassLoader classLoader = new URLClassLoader(name + "-CLASSLOADER", new URL[]{springBootJar.toURI().toURL()}, this.getClass().getClassLoader().getParent());
        JarFile jar = new JarFile(springBootJar);
        Manifest manifest = jar.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        String mainClass;
        if (attributes.getValue("Main-Class") != null) {
            mainClass = attributes.getValue("Main-Class");
        } else {
            mainClass = "org.springframework.boot.loader.JarLauncher";
        }
        classLoader.loadClass(mainClass).getMethod("main", String[].class).invoke(null, new Object[]{args});
    }

    @Override
    public String[] list() throws Exception {
        return storage.list();
    }

}
