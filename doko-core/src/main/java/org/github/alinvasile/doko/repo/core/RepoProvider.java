package org.github.alinvasile.doko.repo.core;

public interface RepoProvider {

    ReadOnlyRepoOperations readOnlyOperations();
    
    ManagementRepoOperations managementOperations();
    
}
