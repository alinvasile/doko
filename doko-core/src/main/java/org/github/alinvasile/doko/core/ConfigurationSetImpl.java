package org.github.alinvasile.doko.core;

import org.github.alinvasile.doko.ConfigurationSet;

class ConfigurationSetImpl implements ConfigurationSet {

    private final String name;
    
    private final String description; 
    
    private final String repository;
    
    public ConfigurationSetImpl(String name, String description, String repository) {
        this.name = name;
        this.description = description;
        this.repository = repository;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    protected String getRepository() {
        return repository;
    }
    

}
