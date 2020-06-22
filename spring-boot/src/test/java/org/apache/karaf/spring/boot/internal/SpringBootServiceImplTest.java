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

import org.junit.Test;

import java.io.File;

public class SpringBootServiceImplTest {

    @Test
    public void testInvalidArtifact() throws Exception {
        System.setProperty("karaf.data", "target/spring-boot");
        SpringBootServiceImpl service = new SpringBootServiceImpl();
        service.install(new File("/Users/jbonofre/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar").toURI());
    }

}
