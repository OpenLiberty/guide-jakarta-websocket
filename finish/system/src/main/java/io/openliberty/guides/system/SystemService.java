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
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

// tag::serverEndpoint[]
@ServerEndpoint( value = "/systemLoad",
                 decoders = { SystemLoadDecoder.class },
                 encoders = { SystemLoadEncoder.class } )
// end::serverEndpoint[]                 
public class SystemService {

    private static Set<Session> sessions = new HashSet<>();

    private static final OperatingSystemMXBean osBean =
        ManagementFactory.getOperatingSystemMXBean();

    private static final MemoryMXBean memBean =
        ManagementFactory.getMemoryMXBean();

    // tag::sendToAllSessionseMethod[]
    public static void sendToAllSessions(JsonObject systemLoad) {
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendObject(systemLoad);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // end::sendToAllSessionseMethod[]

    // tag::onOpenMethod[]
    // tag::onOpen[]
    @OnOpen
    // end::onOpen[]
    public void onOpen(Session session) {
        System.out.println("Server connected to session: " + session.getId());
        sessions.add(session);
    }
    // end::onOpenMethod[]
    
    // tag::onMessageMethod[]
    // tag::onMessage[]
    @OnMessage
    // end::onMessage[]
    public void onMessage(String option, Session session) {
        System.out.println("Server received message \"" + option + "\" " +
                           "from session: " + session.getId());
        try {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("time", Calendar.getInstance().getTime().toString());
            // tag::loadAverage[]
            if (option.equalsIgnoreCase("loadAverage") ||
            // end::loadAverage[]
                option.equalsIgnoreCase("both")) {
                builder.add("loadAverage", Double.valueOf(osBean.getSystemLoadAverage()));
            }
            // tag::memoryUsageOrBoth[]
            if (option.equalsIgnoreCase("memoryUsage") ||
                option.equalsIgnoreCase("both")) {
            // end::memoryUsageOrBoth[]
                long heapMax = memBean.getHeapMemoryUsage().getMax();
                long heapUsed = memBean.getHeapMemoryUsage().getUsed();
                builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax ));
            }
            // tag::sendToAllSessions[]
            JsonObject systemLoad = builder.build();
            sendToAllSessions(systemLoad);
            // end::sendToAllSessions[]
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // end::onMessageMethod[]

    // tag::onCloseMethod[]
    // tag::onClose[]
    @OnClose
    // end::onClose[]
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Session " + session.getId() +
                           " was closed with reason " + closeReason.getCloseCode());
        sessions.remove(session);
    }
    // end::onCloseMethod[]

    // tag::onError[]
    @OnError
    // end::onError[]
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error for " + session.getId() + " " +
                           throwable.getMessage());
    }
}
