package org.github.alinvasile.doko.repo.props;

import org.github.alinvasile.doko.repo.core.ManagementRepoOperations;
import org.github.alinvasile.doko.repo.core.ReadOnlyRepoOperations;
import org.github.alinvasile.doko.repo.core.RepoProvider;
import org.github.alinvasile.doko.repo.core.StorageConfig;

public class JavaPropertiesRepoProvider implements RepoProvider {

    private final JavaPropertiesRepoOperations repo;
    
    public JavaPropertiesRepoProvider(StorageConfig coreStorageConfig){
        repo = new JavaPropertiesRepoOperations(coreStorageConfig);
    }

    public ReadOnlyRepoOperations readOnlyOperations() {
        return repo;
    }

    public ManagementRepoOperations managementOperations() {
        return repo;
    }
    
}
