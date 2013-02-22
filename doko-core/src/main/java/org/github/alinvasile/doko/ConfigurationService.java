package org.github.alinvasile.doko;

import java.util.Set;

public interface ConfigurationService {

    Property getProperty(String name, String configurationSet);
    
    Set<Property> getProperties(String configurationSet);
    
}
