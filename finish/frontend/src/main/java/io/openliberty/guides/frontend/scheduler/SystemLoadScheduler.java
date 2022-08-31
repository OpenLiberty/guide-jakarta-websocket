// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.frontend.scheduler;

import java.net.URI;
import java.util.Random;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;

@Singleton
public class SystemLoadScheduler {

    private SystemClient client; 
    // tag::messages[]
    final static private String[] messages = new String[] {
        "loadAverage", "memoryUsage", "both" };
    // end::messages[]

    // tag::postConstruct[]
    @PostConstruct
    public void init() {
        try {
            // tag::systemClient[]
            client = new SystemClient(new URI("ws://localhost:9081/systemLoad"));
            // end::systemClient[]
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // end::postConstruct[]

    // tag::schedule[]
    @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    // end::schedule[]
    // tag::sendSystemLoad[]
    public void sendSystemLoad() {
        Random r = new Random();
        client.sendMessage(messages[r.nextInt(3)]);
    }
    // end::sendSystemLoad[]

    @PreDestroy
    public void close() {
        client.close();
    }
}
