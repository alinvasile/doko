package org.github.alinvasile.doko.repo.props;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.github.alinvasile.doko.core.ConfigurationSetImpl;
import org.github.alinvasile.doko.core.PropertyImpl;
import org.github.alinvasile.doko.repo.core.ManagementRepoOperations;
import org.github.alinvasile.doko.repo.core.RepositoryException;
import org.github.alinvasile.doko.repo.core.StorageConfig;

class JavaPropertiesRepoOperations implements ManagementRepoOperations {
    
    private final StorageConfig coreStorageConfig;
    
    private final Properties props;
    
    private final Properties configSets;
    
    private final static String ALL_ENVS = "ALL";
    
    public JavaPropertiesRepoOperations(StorageConfig coreStorageConfig) {
        this.coreStorageConfig = coreStorageConfig;
        
        props = new Properties();
        configSets = new Properties();
        
        try {
            props.load(new FileInputStream(getPropertiesFilePath()));
        } catch (FileNotFoundException e) {
            // ignore
        } catch(IOException e){
            throw new RepositoryException(e);
        } 
        
        try {
            configSets.load(new FileInputStream(geConfigSetFilePath()));
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
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

    public  Set<PropertyImpl> getProperties(String configurationSet, String sourceSystem) {
        // check if configuration set exists
        Object configSet = configSets.getProperty(configurationSet);
        if(configSet == null){
            return new HashSet<PropertyImpl>();
        }
        
        Set<PropertyImpl> returnProps = new HashSet<PropertyImpl>();
        
        Set<String> properties = new HashSet<String>();
        Enumeration<?> propertyNames = props.propertyNames();
        while(propertyNames.hasMoreElements()){
            String key = (String) propertyNames.nextElement();
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

    public synchronized void insertOrUpdateProperty(PropertyImpl prop) {
        String baseName = computeBaseName(prop.getName(),prop.getApplicableSystem());
        
        props.setProperty(baseName + ".name", prop.getName());
        props.setProperty(baseName + ".value", prop.getValue());
        props.setProperty(baseName + ".description", prop.getDescription());
        props.setProperty(baseName + ".configurationSet", prop.getConfigurationSet().getName());
        props.setProperty(baseName + ".cacheable", String.valueOf(prop.isCacheable()));
        
        persistProperties();
    }

    public synchronized void insertOrUpdateConfigurationSet(ConfigurationSetImpl cfgSet) {
        String baseName = cfgSet.getName();
                
        configSets.setProperty(baseName + ".name", cfgSet.getName());
        configSets.setProperty(baseName + ".description", cfgSet.getDescription());
        
        persistConfigurationSets();
    }

    public synchronized void deleteProperty(PropertyImpl prop) {
        String baseName = computeBaseName(prop.getName(),prop.getApplicableSystem());
        
        props.remove(baseName + ".name");
        props.remove(baseName + ".value");
        props.remove(baseName + ".description");
        props.remove(baseName + ".configurationSet");
        props.remove(baseName + ".cacheable");
        
        persistProperties();
        
    }

    public synchronized void deleteConfigurationSet(ConfigurationSetImpl cfgSet, boolean force) {
        
        Set<String> properties = new HashSet<String>();
        Enumeration<?> propertyNames = props.propertyNames();
        while(propertyNames.hasMoreElements()){
            String key = (String) propertyNames.nextElement();
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
            
            persistProperties();
        } else {
            if(!properties.isEmpty()){
                throw new RepositoryException("Properties are still assigned to configuration set [" + cfgSet.getName() + "]");
            }
        }
        
        configSets.remove(cfgSet.getName() + ".name");
        configSets.remove(cfgSet.getName() + ".description");
        
        persistConfigurationSets();
        
    }

    public ConfigurationSetImpl getConfigurationSet(String name) {
        String cfgName = (String)configSets.getProperty(name + ".name");
        String cfgDesc = (String)configSets.getProperty(name+".description");
        
        if(StringUtils.isEmpty(cfgName)){
            return null;
        }
        
        return new ConfigurationSetImpl(cfgName, cfgDesc);
    }
    
    
    
    private synchronized void persistProperties(){
        try {
            props.store(new FileWriter(getPropertiesFilePath()), null);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
    
    private synchronized void persistConfigurationSets(){
        try {
            configSets.store(new FileWriter(geConfigSetFilePath()), null);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
    
    private String computeBaseName(String name, String sourceSystem){
        if(sourceSystem == null){
            sourceSystem =  ALL_ENVS;
        }
        return name + "." + sourceSystem;
    }
    
    
    private String getPropertiesFilePath(){
        return getBasePath() +  "props.properties";
    }
    
    private String geConfigSetFilePath(){
        return getBasePath() +  "cfgs.properties";
    }
    
    private String getBasePath(){
        return coreStorageConfig.getUrl() + System.getProperty("file.separator") + getRepositoryName() + System.getProperty("file.separator");
    }
    
    public synchronized void destroy(){
        
    }

}
