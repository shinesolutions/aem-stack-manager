package com.shinesolutions.aemstackmanager.config;

import com.shinesolutions.aemstackmanager.handler.AutoscaleLaunchEventHandler;
import com.shinesolutions.aemstackmanager.handler.AutoscaleTerminateEventHandler;
import com.shinesolutions.aemstackmanager.handler.EventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MappingConfig {

    @Bean
    @SuppressWarnings("serial")
    public Map<String, EventHandler> eventTypeHandlerMappings(
        final AutoscaleTerminateEventHandler autoscaleTerminateEventHandler,
        final AutoscaleLaunchEventHandler autoscaleLaunchEventHandler) {

        return new HashMap<String, EventHandler>() {
            {
                put("autoscaling:EC2_INSTANCE_TERMINATE", autoscaleTerminateEventHandler);
                put("autoscaling:EC2_INSTANCE_LAUNCH", autoscaleLaunchEventHandler);
            }
        };
    }

}
