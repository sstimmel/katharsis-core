package io.katharsis.dispatcher;

public interface RepositoryRegistry {

    Repository get(String resource);
}
