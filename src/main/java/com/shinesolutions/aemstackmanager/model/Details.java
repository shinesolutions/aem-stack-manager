package com.shinesolutions.aemstackmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Details {

    @JsonProperty("descriptor_file")
    private String descriptorFile;

    @JsonProperty("component")
    private String component;

    @JsonProperty("artifact")
    private String artifact;

    public String getDescriptorFile() {
        return descriptorFile;
    }

    public void setDescriptorFile(String descriptorFile) {
        this.descriptorFile = descriptorFile;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }
}
