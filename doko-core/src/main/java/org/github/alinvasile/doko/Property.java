package org.github.alinvasile.doko;

public interface Property<T> {

    String getName();
    
    T getValue();
    
    String getDescription();
    
    ConfigurationSet getConfigurationSet();
    
}
