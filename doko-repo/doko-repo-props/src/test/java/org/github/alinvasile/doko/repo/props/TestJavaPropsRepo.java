package org.github.alinvasile.doko.repo.props;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.github.alinvasile.doko.core.ConfigurationSetImpl;
import org.github.alinvasile.doko.core.PropertyImpl;
import org.github.alinvasile.doko.repo.core.ManagementRepoOperations;
import org.github.alinvasile.doko.repo.core.RepositoryException;
import org.github.alinvasile.doko.repo.core.StorageConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class TestJavaPropsRepo {

    private ManagementRepoOperations repo;
    private File myTempDir;
    
    @Before
    public void before() throws IOException{
        StorageConfig config = new StorageConfig();
        config.setName("test");
        config.setDescription("test repo");
        
        myTempDir = Files.createTempDir();
        File repositoryFolder = new File(myTempDir.getAbsolutePath(),"test");
        repositoryFolder.mkdir();
        config.setUrl(myTempDir.getPath());
        JavaPropertiesRepoProvider provider = new JavaPropertiesRepoProvider(config);
        
        repo = provider.managementOperations();
    }
    
    @After
    public void after(){
        repo = null;
        myTempDir.delete();
    }
    
    @Test
    public void testDifferentConfigurationSetName(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        assertNull(repo.getProperty("mastercard.username", "visa", null));
    }
    
    @Test
    public void addedPropertyShouldExist(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        PropertyImpl retrievedProperty = repo.getProperty("mastercard.username", "mastercard", null);
        assertNotNull(retrievedProperty);
        assertEquals("mastercard.username",retrievedProperty.getName());
        assertEquals("XGDTEJ93",retrievedProperty.getValue());
        assertTrue(retrievedProperty.isCacheable());
    }
    
    @Test
    public void addedConfigurationSetShouldExist(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","X");
        
        repo.insertOrUpdateConfigurationSet(cfg);
        
        ConfigurationSetImpl retrievedCfgSet = repo.getConfigurationSet("mastercard");
        assertNotNull(retrievedCfgSet);
        assertEquals("mastercard",retrievedCfgSet.getName());
        assertEquals("X",retrievedCfgSet.getDescription());
    }
    
    @Test
    public void nonExistingConfigurationSetShouldReturnNull(){
        ConfigurationSetImpl configurationSet = repo.getConfigurationSet("missing");
        assertNull(configurationSet);
    }
    
    @Test
    public void nonExistingPropertyShouldReturnNull(){
        PropertyImpl prop = repo.getProperty("missing","x",null);
        assertNull(prop);
    }
    
    @Test
    public void testDeleteProperty(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        assertNotNull(repo.getProperty("mastercard.username", "mastercard", null));
        
        repo.deleteProperty(prop);
        
        assertNull(repo.getProperty("mastercard.username", "mastercard", null));
    }
    
    @Test
    public void testDeleteConfigSetNoProps(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","X");
        
        repo.insertOrUpdateConfigurationSet(cfg);
        assertNotNull(repo.getConfigurationSet("mastercard"));
        
        repo.deleteConfigurationSet(cfg, false);
        assertNull(repo.getConfigurationSet("mastercard"));
    }
    
    @Test
    public void testDeleteConfigSetPropsNoForce(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","X");
        
        repo.insertOrUpdateConfigurationSet(cfg);
        assertNotNull(repo.getConfigurationSet("mastercard"));
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        try{
            repo.deleteConfigurationSet(cfg, false);
            fail("deleteConfigurationSet should have failed");
        } catch(RepositoryException e){
            
        }
        
        assertNotNull(repo.getProperty("mastercard.username", "mastercard", null));
        
    }
    
    @Test
    public void testDeleteConfigSetPropsForce(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","X");
        
        repo.insertOrUpdateConfigurationSet(cfg);
        assertNotNull(repo.getConfigurationSet("mastercard"));
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        try{
            repo.deleteConfigurationSet(cfg, true);
        } catch(RepositoryException e){
            fail("deleteConfigurationSet should have not failed");
        }
        
        assertNull(repo.getConfigurationSet("mastercard"));
        assertNull(repo.getProperty("mastercard.username", "mastercard", null));
    }
    
    @Test
    public void addedSystemSpecificPropertyShouldExist(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setApplicableSystem("eemea.example.org");
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        PropertyImpl retrievedProperty = repo.getProperty("mastercard.username", "mastercard", "eemea.example.org");
        assertNotNull(retrievedProperty);
        assertEquals("mastercard.username",retrievedProperty.getName());
        assertEquals("XGDTEJ93",retrievedProperty.getValue());
        assertTrue(retrievedProperty.isCacheable());
        
        assertNull(repo.getProperty("mastercard.username", "mastercard", null));
    }
    
    @Test
    public void addedSystemAndGlobalSpecificPropertyShouldExist(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setApplicableSystem("eemea.example.org");
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        PropertyImpl global = new PropertyImpl();
        global.setName("mastercard.username");
        global.setValue("UUU87");
        global.setCacheable(true);
        global.setConfigurationSet(cfg);
        global.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(global);
        
        PropertyImpl retrievedProperty = repo.getProperty("mastercard.username", "mastercard", "eemea.example.org");
        assertNotNull(retrievedProperty);
        assertEquals("mastercard.username",retrievedProperty.getName());
        assertEquals("XGDTEJ93",retrievedProperty.getValue());
        assertTrue(retrievedProperty.isCacheable());
        
        retrievedProperty = repo.getProperty("mastercard.username", "mastercard", null);
        assertEquals("mastercard.username",retrievedProperty.getName());
        assertEquals("UUU87",retrievedProperty.getValue());
        assertTrue(retrievedProperty.isCacheable());
    }
    
    @Test
    public void testGlobalPropertyOnly(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        PropertyImpl retrievedProperty = repo.getProperty("mastercard.username", "mastercard", "eemea.example.org");
        assertNotNull(retrievedProperty);
        assertEquals("mastercard.username",retrievedProperty.getName());
        assertEquals("XGDTEJ93",retrievedProperty.getValue());
        assertTrue(retrievedProperty.isCacheable());
    }
    
    @Test
    public void testGlobalPropertyNullSystem(){
        ConfigurationSetImpl cfg = new ConfigurationSetImpl("mastercard","");
        
        PropertyImpl prop = new PropertyImpl();
        prop.setName("mastercard.username");
        prop.setValue("XGDTEJ93");
        prop.setCacheable(true);
        prop.setConfigurationSet(cfg);
        prop.setDescription("Username for Mastercard Payment Server");
        
        repo.insertOrUpdateProperty(prop);
        
        PropertyImpl retrievedProperty = repo.getProperty("mastercard.username", "mastercard", null);
        assertNotNull(retrievedProperty);
        assertEquals("mastercard.username",retrievedProperty.getName());
        assertEquals("XGDTEJ93",retrievedProperty.getValue());
        assertTrue(retrievedProperty.isCacheable());
    }
    
}
