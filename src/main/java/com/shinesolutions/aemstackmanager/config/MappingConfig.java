package com.shinesolutions.aemstackmanager.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shinesolutions.aemstackmanager.handler.AutoscaleLaunchEventHandler;
import com.shinesolutions.aemstackmanager.handler.AutoscaleTerminateEventHandler;
import com.shinesolutions.aemstackmanager.handler.EventHandler;

@Configuration
public class MappingConfig {

    @Bean
    @SuppressWarnings("serial")
    public Map<String, EventHandler> eventTypeHandlerMappings(
        final AutoscaleTerminateEventHandler autoscaleTerminateEventHandler,
        final AutoscaleLaunchEventHandler autoscaleLaunchEventHandler) {

        Map<String, EventHandler> mappings = new HashMap<String, EventHandler>() {
            {
                put("autoscaling:EC2_INSTANCE_TERMINATE", autoscaleTerminateEventHandler);
                put("autoscaling:EC2_INSTANCE_LAUNCH", autoscaleLaunchEventHandler);
            }
        };

        return mappings;
    }

}
