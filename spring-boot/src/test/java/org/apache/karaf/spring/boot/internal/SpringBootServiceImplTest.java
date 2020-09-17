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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.impl.SimpleLogger;

import javax.swing.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

public class SpringBootServiceImplTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
        System.setProperty("karaf.data", temporaryFolder.getRoot().getAbsolutePath());
        new File(temporaryFolder.getRoot(), "spring-boot").mkdirs();
    }

    @Test
    public void testInstallInvalidArtifact() throws Exception {
        SpringBootServiceImpl service = new SpringBootServiceImpl();
        try {
            service.install(new File("target/commons-lang-2.6.jar").toURI());
        } catch (IllegalArgumentException e) {
            // good
            return;
        }
        Assert.fail("IllegalArgumentException expected");
    }

    @Test
    public void testInstallValidArtifact() throws Exception {
        SpringBootServiceImpl service = new SpringBootServiceImpl();
        service.install(new File("target/test-classes/rest-service-0.0.1-SNAPSHOT.jar").toURI());

        Assert.assertEquals(1, service.list().length);
        Assert.assertEquals("rest-service-0.0.1-SNAPSHOT.jar", service.list()[0]);
    }

    @Test
    public void testStart() throws Exception {
        SpringBootServiceImpl service = new SpringBootServiceImpl();
        service.install(new File("target/test-classes/rest-service-0.0.1-SNAPSHOT.jar").toURI());
        service.start("rest-service-0.0.1-SNAPSHOT.jar", new String[0]);
        service.stop("rest-service-0.0.1-SNAPSHOT.jar"); // cleanup the classloader (avoids to leak)
    }

}
