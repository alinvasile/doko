package org.github.alinvasile.doko.repo.core;

import org.github.alinvasile.doko.core.ConfigurationSetImpl;
import org.github.alinvasile.doko.core.PropertyImpl;

public interface ManagementRepoOperations extends ReadOnlyRepoOperations {

    void insertOrUpdateProperty(PropertyImpl prop);
    
    void insertOrUpdateConfigurationSet(ConfigurationSetImpl cfgSet);
    
    void deleteProperty(PropertyImpl prop);
    
    void deleteConfigurationSet(ConfigurationSetImpl cfgSet, boolean force);
    
}
