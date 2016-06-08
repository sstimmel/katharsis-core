package io.katharsis.itests

import com.fasterxml.jackson.databind.ObjectMapper
import io.katharsis.dispatcher.RequestDispatcher
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder
import io.katharsis.itests.tck.Task
import io.katharsis.itests.tck.TaskRepository
import io.katharsis.locator.SampleJsonServiceLocator
import io.katharsis.queryParams.DefaultQueryParamsParser
import io.katharsis.queryParams.QueryParamsBuilder
import io.katharsis.resource.field.ResourceFieldNameTransformer
import io.katharsis.resource.information.ResourceInformationBuilder
import io.katharsis.resource.registry.ResourceRegistryBuilder
import io.katharsis.utils.parser.TypeParser
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.map.repository.config.EnableMapRepositories
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.util.*

@RunWith(value = SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(IntegrationConfig::class))
class KatharsisIntegrationTest {

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Test
    fun simpleTest() {
        var task = Task(UUID.randomUUID().toString(), "aaaa", null)
        taskRepository.save(task);

        println(taskRepository.findAll())
    }
}

@Configuration
@EnableMapRepositories("io.katharsis.itests")
open class IntegrationConfig {

    @Bean
    open fun requestDispatcher(): RequestDispatcher {

        val exceptionMapperRegistry = ExceptionMapperRegistryBuilder()
                .build("io.katharsis.itests")

        val resourceRegistry = ResourceRegistryBuilder(SampleJsonServiceLocator(),
                ResourceInformationBuilder(ResourceFieldNameTransformer()))
                .build("io.katharsis.itests", "/api")

        return RequestDispatcher(exceptionMapperRegistry, resourceRegistry,
                TypeParser(), ObjectMapper(), QueryParamsBuilder(DefaultQueryParamsParser()))
    }

}