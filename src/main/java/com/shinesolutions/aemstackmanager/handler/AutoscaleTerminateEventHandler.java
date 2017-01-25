package com.shinesolutions.aemstackmanager.handler;

import org.springframework.stereotype.Component;

import com.shinesolutions.aemstackmanager.model.EventMessage;

@Component
public class AutoscaleTerminateEventHandler implements EventHandler {

    public boolean handleEvent(EventMessage message) {
        // Do stuff
        return false;
    }

}
