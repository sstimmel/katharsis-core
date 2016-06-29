package io.katharsis.itests.registry

import io.katharsis.dispatcher.registry.DefaultResourceLookup
import io.katharsis.errorhandling.exception.KatharsisInitializationException
import io.katharsis.itests.registry.fixtures.simple.Task
import io.katharsis.itests.registry.fixtures.simple.TaskRestRepo
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ResourceLookupTest {

    val goodFixtures = "io.katharsis.itests.registry.fixtures.simple";
    val repoWithoutResourceFixtures = "io.katharsis.itests.registry.fixtures.noresource";
    val repoWithRelationships = "io.katharsis.itests.registry.fixtures.relationships";

    @Test
    fun testResourceDiscovery() {
        val registry = DefaultResourceLookup(goodFixtures);
        val resources: Map<String, Any> = registry.getResources();

        assertFalse(resources.isEmpty())

        assertTrue(resources.containsKey("tasks"))
        assertEquals(Task::class.toString(), resources.get("tasks").toString(), "Classes do not match")
    }

    @Test
    fun testRepositoryDiscovery() {
        val registry = DefaultResourceLookup(goodFixtures);
        val repos: Map<String, Any> = registry.getRepositories();

        assertFalse(repos.isEmpty())

        assertTrue(repos.containsKey("tasks"))
        assertEquals(TaskRestRepo::class.toString(), repos.get("tasks").toString(), "Classes do not match")
    }

    @Test
    fun testResourceDiscoveryForRepositoryWithoutResource() {
        try {
            DefaultResourceLookup(repoWithoutResourceFixtures);
        } catch (e: KatharsisInitializationException) {
            val msg = e.message
            if (msg != null) {
                assertTrue(msg.contains("Required annotation interface io.katharsis.resource.annotations.JsonApiResource is missing from class java.lang.Object", true))
            }

        }
    }

    @Test
    fun testRelationshipRepostiroiesAreFound() {
        val registry = DefaultResourceLookup(repoWithRelationships);
        val repos: Map<String, Map<String, Any>> = registry.getRelationships()

        assertFalse(repos.isEmpty())

        assertTrue(repos.containsKey("projects"))
        assertEquals(io.katharsis.itests.registry.fixtures.relationships.Task::class.toString(), repos.get("projects")?.get("tasks").toString(), "Classes do not match")
    }
}
