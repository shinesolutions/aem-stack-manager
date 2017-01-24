package com.shinesolutions.aemmcollective.handler;

import com.shinesolutions.aemmcollective.model.EventMessage;

public interface EventHandler {

    boolean handleEvent(EventMessage message);

}
