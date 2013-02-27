package org.github.alinvasile.doko.repo.memory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.github.alinvasile.doko.core.ConfigurationSetImpl;
import org.github.alinvasile.doko.core.PropertyImpl;
import org.github.alinvasile.doko.repo.core.ManagementRepoOperations;
import org.github.alinvasile.doko.repo.core.RepositoryException;
import org.github.alinvasile.doko.repo.core.StorageConfig;

class InMemoryRepoOperations implements ManagementRepoOperations {
    
    private final StorageConfig coreStorageConfig;

    private Map<String,String> props = new ConcurrentHashMap<String, String>();
    
    private Map<String,String> configSets = new ConcurrentHashMap<String, String>();
    
    private final static String ALL_ENVS = "ALL";
    
    public InMemoryRepoOperations(StorageConfig coreStorageConfig) {
        this.coreStorageConfig = coreStorageConfig;
    }

    public String getRepositoryName() {
        return coreStorageConfig.getName();
    }

    public PropertyImpl getProperty(String name, String configurationSet, String sourceSystem) {
        String baseName = computeBaseName(name,sourceSystem);
        
        boolean retrievedForSourceSystem = true;
        
        String propName = (String)props.get(baseName + ".name");
        if(StringUtils.isEmpty(propName)){
            retrievedForSourceSystem = false;
            baseName = computeBaseName(name,ALL_ENVS);
            propName = (String)props.get(baseName + ".name");
        }
        
        if(StringUtils.isEmpty(propName)){
            return null;
        }
        
        String configurationSetName = (String)props.get(baseName + ".configurationSet");
        if(!StringUtils.equals(configurationSetName, configurationSet)){
            return null;
        }
        
        String value = (String)props.get(baseName + ".value");
        String description = (String)props.get(baseName + ".description");
        String cacheable = (String)props.get(baseName + ".cacheable");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName(name);
        prop.setConfigurationSet(getConfigurationSet(configurationSet));
        prop.setApplicableSystem(retrievedForSourceSystem?sourceSystem:null);
        prop.setCacheable(Boolean.parseBoolean(cacheable));
        prop.setDescription(description);
        prop.setValue(value);
        
        return prop;   
    }

    public Set<PropertyImpl> getProperties(String configurationSet, String sourceSystem) {
        Object configSet = configSets.get(configurationSet);
        if(configSet == null){
            return new HashSet<PropertyImpl>();
        }
        
        Set<PropertyImpl> returnProps = new HashSet<PropertyImpl>();
        
        Set<String> properties = new HashSet<String>();
        Set<String> propertyNames = props.keySet();
        for(String key:propertyNames){
            if(!key.endsWith(".configurationSet")){
                continue;
            }
            
            String baseName = key.substring(0,key.lastIndexOf(".configurationSet"));
            boolean propertyApplicableSystem = baseName.endsWith(sourceSystem + ".");
            boolean propertyApplicableAll = baseName.endsWith(ALL_ENVS + ".");
            if(!propertyApplicableSystem & !propertyApplicableAll){
                continue;
            }
            
            String propertyName;
            if(propertyApplicableSystem){
                propertyName = baseName.substring(0,baseName.lastIndexOf(sourceSystem + "."));
            } else {
                propertyName = baseName.substring(0,baseName.lastIndexOf(ALL_ENVS + "."));
            }
            
            if(propertyName.equals(configurationSet)){
                properties.add(propertyName);
            }
            
        }
        
        for(String toAdd:properties){
            returnProps.add(getProperty(toAdd, configurationSet, sourceSystem));
        }
        
        return returnProps;
    }

    public ConfigurationSetImpl getConfigurationSet(String name) {
        String cfgName = (String)configSets.get(name + ".name");
        String cfgDesc = (String)configSets.get(name+".description");
        
        if(StringUtils.isEmpty(cfgName)){
            return null;
        }
        
        return new ConfigurationSetImpl(cfgName, cfgDesc);
    }

    public synchronized void insertOrUpdateProperty(PropertyImpl prop) {
        String baseName = computeBaseName(prop.getName(),prop.getApplicableSystem());
        
        props.put(baseName + ".name", prop.getName());
        props.put(baseName + ".value", prop.getValue());
        props.put(baseName + ".description", prop.getDescription());
        props.put(baseName + ".configurationSet", prop.getConfigurationSet().getName());
        props.put(baseName + ".cacheable", String.valueOf(prop.isCacheable()));
        
        //TODO check if configuration set exists
    }

    public synchronized void insertOrUpdateConfigurationSet(ConfigurationSetImpl cfgSet) {
        String baseName = cfgSet.getName();
        
        configSets.put(baseName + ".name", cfgSet.getName());
        configSets.put(baseName + ".description", cfgSet.getDescription());
        
    }

    public synchronized void deleteProperty(PropertyImpl prop) {
        String baseName = computeBaseName(prop.getName(),prop.getApplicableSystem());
        
        props.remove(baseName + ".name");
        props.remove(baseName + ".value");
        props.remove(baseName + ".description");
        props.remove(baseName + ".configurationSet");
        props.remove(baseName + ".cacheable");
        
    }

    public void deleteConfigurationSet(ConfigurationSetImpl cfgSet, boolean force) {
        Set<String> properties = new HashSet<String>();
        Set<String> propertyNames = props.keySet();
        for( String key:propertyNames){
            if(!key.endsWith(".configurationSet")){
                continue;
            }
            
            String name = key.substring(0,key.lastIndexOf(".configurationSet"));
            if(name.startsWith(cfgSet.getName() + ".")){
                //TODO ensure source names don't contain '.'
                properties.add(name);
            }
        }
        
        if(force){
            // ignore if configuration set contains properties
            for(String baseName:properties){
                props.remove(baseName + ".name");
                props.remove(baseName + ".value");
                props.remove(baseName + ".description");
                props.remove(baseName + ".configurationSet");
                props.remove(baseName + ".cacheable");
            }
            
        } else {
            if(!properties.isEmpty()){
                throw new RepositoryException("Properties are still assigned to configuration set [" + cfgSet.getName() + "]");
            }
        }
        
        configSets.remove(cfgSet.getName() + ".name");
        configSets.remove(cfgSet.getName() + ".description");
        
    }
    
    private String computeBaseName(String name, String sourceSystem){
        if(sourceSystem == null){
            sourceSystem =  ALL_ENVS;
        }
        return name + "." + sourceSystem;
    }
    
}
