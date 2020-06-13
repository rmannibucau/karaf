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

import java.io.File;
import java.net.URI;

public class SpringBootServiceImpl implements SpringBootService {

    private File storage;

    public SpringBootServiceImpl() {
        storage = new File(new File(System.getProperty("karaf.data")), "spring-boot");
        storage.mkdirs();
    }

    @Override
    public void start(URI uri) throws Exception {

    }

}
