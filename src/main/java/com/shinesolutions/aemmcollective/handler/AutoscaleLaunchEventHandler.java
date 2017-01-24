package com.shinesolutions.aemmcollective.handler;

import org.springframework.stereotype.Component;

import com.shinesolutions.aemmcollective.model.EventMessage;

@Component
public class AutoscaleLaunchEventHandler implements EventHandler {
    

    public boolean handleEvent(EventMessage message) {
        //Do stuff
        return false;
    }

}
