package com.shinesolutions.aemstackmanager.handler;

import com.shinesolutions.aemstackmanager.model.EventMessage;

public interface EventHandler {

    boolean handleEvent(EventMessage message);

}
