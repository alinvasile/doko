package org.github.alinvasile.doko.repo.core;

import java.util.Set;

import org.github.alinvasile.doko.core.ConfigurationSetImpl;
import org.github.alinvasile.doko.core.PropertyImpl;

public interface ReadOnlyRepoOperations {
    
    String getRepositoryName();

    PropertyImpl getProperty(String name, String configurationSet, String sourceSystem);
    
    Set<PropertyImpl> getProperties(String configurationSet);
    
    ConfigurationSetImpl getConfigurationSet(String name);
    
}
