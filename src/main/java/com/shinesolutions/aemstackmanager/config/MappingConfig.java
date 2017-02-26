package com.shinesolutions.aemstackmanager.config;

import com.shinesolutions.aemstackmanager.handler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MappingConfig {

    @Bean
    @SuppressWarnings("serial")
    public Map<String, TaskHandler> taskHandlerMappings(
            final PromoteAuthorTaskHandler promoteAuthorTaskHandler,
            final DeployArtifactsTaskHandler deployArtifactsTaskHandler,
            final DeployArtifactTaskHandler deployArtifactTaskHandler,
            final ExportPackageTaskHandler exportPackageTaskHandler,
            final ImportPackageTaskHandler importPackageTaskHandler
            ) {

        return new HashMap<String, TaskHandler>() {
            {
                put("promote-author", promoteAuthorTaskHandler);
                put("deploy-artifacts", deployArtifactsTaskHandler);
                put("deploy-artifact", deployArtifactTaskHandler);
                put("export-package", exportPackageTaskHandler);
                put("import-package", importPackageTaskHandler);
            }
        };
    }

}
