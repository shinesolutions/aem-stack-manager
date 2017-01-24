/*
 * Copyright 2017 Shine Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shinesolutions.aemmcollective;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class AemMCollectiveRunner implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(AemMCollectiveRunner.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting AEM mCollective Runner");
        SpringApplication.run(AemMCollectiveRunner.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            //Keep main thread open while listening to message queue
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
    }

}
