package io.katharsis.itests.registry

import io.katharsis.dispatcher.RepositoryRegistryImpl
import io.katharsis.errorhandling.exception.KatharsisInitializationException
import io.katharsis.itests.registry.fixtures1.Task
import io.katharsis.itests.registry.fixtures1.TaskRestRepo
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class RepositoryRegistryTest {

    val goodFixtures = "io.katharsis.itests.registry.fixtures1";
    val repoWithoutResourceFixtures = "io.katharsis.itests.registry.fixtures2";

    @Test
    @Ignore
    fun testResourceDiscovery() {
        val registry = RepositoryRegistryImpl(goodFixtures, "/api/");
        val resources: Map<String, Any> = registry.getResources();

        assertFalse(resources.isEmpty())

        assertTrue(resources.containsKey("tasks"))
        assertEquals(Task::class.toString(), resources.get("tasks").toString(), "Classes do not match")
    }

    @Test
    @Ignore
    fun testRepositoryDiscovery() {
        val registry = RepositoryRegistryImpl(goodFixtures, "/api/");
        val repos: Map<String, Any> = registry.getRepositories();

        assertFalse(repos.isEmpty())

        assertTrue(repos.containsKey("tasks"))
        assertEquals(TaskRestRepo::class.toString(), repos.get("tasks").toString(), "Classes do not match")
    }

    @Test
    @Ignore
    fun testResourceDiscoveryForRepositoryWithoutResource() {
        try {
            val registry = RepositoryRegistryImpl(repoWithoutResourceFixtures, "/api/");
        } catch (e: KatharsisInitializationException) {
            val msg = e.message
            if (msg != null) {
                assertTrue(msg.contains("Required annotation interface io.katharsis.resource.annotations.JsonApiResource is missing from class java.lang.Object", true))
            }

        }
    }

}
