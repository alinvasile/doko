package org.github.alinvasile.doko.core;

import org.github.alinvasile.doko.ConfigurationSet;

public class ConfigurationSetImpl implements ConfigurationSet {

    private final String name;
    
    private final String description; 
    
    public ConfigurationSetImpl(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }


}
