package org.github.alinvasile.doko.core;

import org.github.alinvasile.doko.ConfigurationSet;
import org.github.alinvasile.doko.Property;

class PropertyImpl<T> implements Property<T> {
    
    private String name;
    
    private String description; 
    
    private T value;
    
    private String applicableSystem;
    
    private ConfigurationSet configurationSet;
    
    private boolean cacheable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getApplicableSystem() {
        return applicableSystem;
    }

    public void setApplicableSystem(String applicableSystem) {
        this.applicableSystem = applicableSystem;
    }

    public ConfigurationSet getConfigurationSet() {
        return configurationSet;
    }

    public void setConfigurationSet(ConfigurationSet configurationSet) {
        this.configurationSet = configurationSet;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }
    

}
