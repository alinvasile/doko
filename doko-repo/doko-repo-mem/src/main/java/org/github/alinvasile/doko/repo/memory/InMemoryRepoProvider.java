package org.github.alinvasile.doko.repo.memory;

import org.github.alinvasile.doko.repo.core.ManagementRepoOperations;
import org.github.alinvasile.doko.repo.core.ReadOnlyRepoOperations;
import org.github.alinvasile.doko.repo.core.RepoProvider;
import org.github.alinvasile.doko.repo.core.StorageConfig;

public class InMemoryRepoProvider implements RepoProvider {

    private InMemoryRepoOperations repo;
    
    public InMemoryRepoProvider(StorageConfig coreStorageConfig){
        repo = new InMemoryRepoOperations(coreStorageConfig);
    }
    
    public ReadOnlyRepoOperations readOnlyOperations() {
        return repo;
    }

    public ManagementRepoOperations managementOperations() {
        return repo;
    }
    
}
