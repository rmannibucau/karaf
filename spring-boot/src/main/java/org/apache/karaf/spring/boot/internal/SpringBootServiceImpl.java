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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
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
        File springJar = new File(storage, "test.jar");
        StreamUtils.copy(uri.toURL().openStream(), new FileOutputStream(springJar));
        // simple run for now, see next TODO for next step
        // creating classloader
        JarFile jar = new JarFile(springJar);
        Manifest manifest = jar.getManifest();
        Attributes attributes = manifest.getAttributes("Spring-Boot-Version");
        if (attributes.getValue("Spring-Boot-Version") == null) {
            throw new IllegalArgumentException("Invalid Spring Boot artifact");
        }
        // TODO inspect depends and Spring beans to use existing bundles/features/services
        // running main
    }

}
