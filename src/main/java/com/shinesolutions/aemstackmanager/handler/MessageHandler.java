package com.shinesolutions.aemstackmanager.handler;

import javax.jms.Message;

public interface MessageHandler {
    
    boolean handleMessage(Message message);
    
}
