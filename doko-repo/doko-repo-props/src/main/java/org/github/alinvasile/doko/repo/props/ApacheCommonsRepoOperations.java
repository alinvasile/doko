package org.github.alinvasile.doko.repo.props;

import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.github.alinvasile.doko.core.ConfigurationSetImpl;
import org.github.alinvasile.doko.core.PropertyImpl;
import org.github.alinvasile.doko.repo.core.ManagementRepoOperations;
import org.github.alinvasile.doko.repo.core.RepositoryException;
import org.github.alinvasile.doko.repo.core.StorageConfig;

public class ApacheCommonsRepoOperations implements ManagementRepoOperations {
    
    private StorageConfig coreStorageConfig;
    
    private final PropertiesConfiguration props;
    
    private final PropertiesConfiguration configSets;
    
    private final static String ALL_ENVS = "ALL";
    
    public ApacheCommonsRepoOperations(StorageConfig coreStorageConfig){
        this.coreStorageConfig = coreStorageConfig;
        try {
            props = new PropertiesConfiguration(coreStorageConfig.getUrl() +  "props.properties");
            props.setReloadingStrategy(new FileChangedReloadingStrategy());
            
            configSets = new PropertiesConfiguration(coreStorageConfig.getUrl() +  "cfgs.properties");
            configSets.setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (ConfigurationException e) {
            throw new RepositoryException(e);
        }
    }

    public String getRepositoryName() {
        return coreStorageConfig.getName();
    }

    public  PropertyImpl getProperty(String name, String configurationSet, String sourceSystem) {
        String baseName = computeBaseName(name,sourceSystem);
        
        boolean retrievedForSourceSystem = true;
        
        String propName = (String)props.getProperty(baseName + ".name");
        if(StringUtils.isEmpty(propName)){
            retrievedForSourceSystem = false;
            propName = (String)props.getProperty(computeBaseName(name,ALL_ENVS) + ".name");
        }
        
        if(StringUtils.isEmpty(propName)){
            return null;
        }
        
        String value = (String)props.getProperty(baseName + ".value");
        String description = (String)props.getProperty(baseName + ".description");
        String cacheable = (String)props.getProperty(baseName + ".cacheable");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName(name);
        prop.setConfigurationSet(getConfigurationSet(configurationSet));
        prop.setApplicableSystem(retrievedForSourceSystem?sourceSystem:null);
        prop.setCacheable(Boolean.parseBoolean(cacheable));
        prop.setDescription(description);
        prop.setValue(value);
        
        return prop;        
    }

    public  Set<PropertyImpl> getProperties(String configurationSet) {
        // TODO Auto-generated method stub
        return null;
    }

    public synchronized void insertOrUpdateProperty(PropertyImpl prop) {
        String baseName = computeBaseName(prop.getName(),prop.getApplicableSystem());
        
        props.setProperty(baseName + ".name", prop.getName());
        props.setProperty(baseName + ".value", prop.getValue());
        props.setProperty(baseName + ".description", prop.getDescription());
        props.setProperty(baseName + ".configurationSet", prop.getConfigurationSet().getName());
        props.setProperty(baseName + ".cacheable", prop.isCacheable());
        
        try {
            props.save();
        } catch (ConfigurationException e) {
            throw new RepositoryException(e);
        }
    }

    public synchronized void insertOrUpdateConfigurationSet(ConfigurationSetImpl cfgSet) {
        String baseName = cfgSet.getName();
                
        configSets.setProperty(baseName + ".name", cfgSet.getName());
        configSets.setProperty(baseName + ".description", cfgSet.getDescription());
        
        try {
            configSets.save();
        } catch (ConfigurationException e) {
            throw new RepositoryException(e);
        }
    }

    public synchronized void deleteProperty(PropertyImpl prop) {
        String baseName = computeBaseName(prop.getName(),prop.getApplicableSystem());
        
        props.clearProperty(baseName + ".name");
        props.clearProperty(baseName + ".value");
        props.clearProperty(baseName + ".description");
        props.clearProperty(baseName + ".configurationSet");
        props.clearProperty(baseName + ".cacheable");
        
        try {
            props.save();
        } catch (ConfigurationException e) {
            throw new RepositoryException(e);
        }
        
    }

    public  void deleteConfigurationSet(ConfigurationSetImpl cfgSet, boolean force) {
        // TODO Auto-generated method stub
        
    }

    public ConfigurationSetImpl getConfigurationSet(String name) {
        String cfgName = (String)configSets.getProperty(name + ".name");
        String cfgDesc = (String)configSets.getProperty(name+".description");
        
        if(StringUtils.isEmpty(cfgName)){
            return null;
        }
        
        return new ConfigurationSetImpl(cfgName, cfgDesc);
    }
    
    private String computeBaseName(String name, String sourceSystem){
        return name + "." + sourceSystem;
    }
    
    public void destroy(){
        
    }

}
