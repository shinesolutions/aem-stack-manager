package com.shinesolutions.aemstackmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Details {

    @JsonProperty("descriptor")
    private String descriptor;

    @JsonProperty("component")
    private String component;

    @JsonProperty("artifact")
    private String artifact;

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
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
