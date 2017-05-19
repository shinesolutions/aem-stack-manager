package com.shinesolutions.aemstackmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Details {

    @JsonProperty("descriptor_file")
    private String descriptorFile;

    @JsonProperty("component")
    private String component;

    @JsonProperty("artifact")
    private String artifact;

    @JsonProperty("source_stack_prefix")
    private String sourceStackPrefix;

    @JsonProperty("package_group")
    private String packageGroup;

    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("package_datestamp")
    private String packageDatestamp;

    @JsonProperty("package_filter")
    private String packageFilter;

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

    public String getPackageGroup() {
        return packageGroup;
    }

    public void setPackageGroup(String packageGroup) {
        this.packageGroup = packageGroup;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageDatestamp() {
        return packageDatestamp;
    }

    public void setPackageDatestamp(String packageDatestamp) {
        this.packageDatestamp = packageDatestamp;
    }

    public String getPackageFilter() {
        return packageFilter;
    }

    public void setPackageFilter(String packageFilter) {
        this.packageFilter = packageFilter;
    }

    public String getSourceStackPrefix() {
        return sourceStackPrefix;
    }

    public void setSourceStackPrefix(String sourceStackPrefix) {
        this.sourceStackPrefix = sourceStackPrefix;
    }
}
