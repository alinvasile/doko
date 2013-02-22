package org.github.alinvasile.doko;

import java.util.Set;

public interface ConfigurationService {

    <T> Property<T> getProperty(String name, String configurationSet);
    
    <T> Set<Property<T>> getProperties(String configurationSet);
    
}
