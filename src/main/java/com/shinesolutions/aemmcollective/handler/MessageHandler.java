package com.shinesolutions.aemmcollective.handler;

import javax.jms.Message;

public interface MessageHandler {
    
    boolean handleMessage(Message message);
    
}
