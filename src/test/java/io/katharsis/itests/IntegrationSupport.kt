package io.katharsis.itests

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Throwables
import io.katharsis.dispatcher.RequestDispatcher
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder
import io.katharsis.itests.tck.ProjectRepository
import io.katharsis.itests.tck.TaskRepository
import io.katharsis.jackson.JsonApiModuleBuilder
import io.katharsis.locator.NewInstanceRepositoryFactory
import io.katharsis.queryParams.DefaultQueryParamsParser
import io.katharsis.queryParams.QueryParamsBuilder
import io.katharsis.repository.RepositoryParameterProvider
import io.katharsis.resource.field.ResourceFieldNameTransformer
import io.katharsis.resource.information.ResourceInformationBuilder
import io.katharsis.resource.registry.ResourceRegistry
import io.katharsis.resource.registry.ResourceRegistryBuilder
import io.katharsis.utils.parser.TypeParser
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.map.repository.config.EnableMapRepositories
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets

@RunWith(value = SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(IntegrationConfig::class))
open class KatharsisIntegrationSupport {

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var requestDispatcher: RequestDispatcher

    @Autowired
    lateinit var paramProvider: ParamProvider


    @Autowired
    lateinit var objectMapper: ObjectMapper

    fun serialize(obj: Any?): InputStream {
        try {
            val body = objectMapper.writeValueAsString(obj)
            return ByteArrayInputStream(body?.toByteArray(StandardCharsets.UTF_8));
        } catch (e: JsonProcessingException) {
            throw Throwables.propagate(e);
        }
    }

}

@Configuration
@EnableMapRepositories("io.katharsis.itests")
open class IntegrationConfig {

    @Autowired
    lateinit var context: ApplicationContext ;

    @Bean
    open fun resourceRegistry(): ResourceRegistry {
        val resourceRegistry = ResourceRegistryBuilder(NewInstanceRepositoryFactory(),
                ResourceInformationBuilder(ResourceFieldNameTransformer()))
                .build("io.katharsis.itests.tck", "/")
        return resourceRegistry
    }

    @Bean
    open fun objectMapper(resourceRegistry: ResourceRegistry): ObjectMapper {
        val mapper = ObjectMapper();
        mapper.registerModule(JsonApiModuleBuilder().build(resourceRegistry))
        return mapper;
    }

    @Bean
    open fun paramProvider(): RepositoryParameterProvider {
        return ParamProvider(context)
    }

    @Bean
    @Autowired
    open fun requestDispatcher(objectMapper: ObjectMapper, resourceRegistry: ResourceRegistry): RequestDispatcher {
        val exceptionMapperRegistry = ExceptionMapperRegistryBuilder()
                .build("io.katharsis.itests")

        return RequestDispatcher(exceptionMapperRegistry, resourceRegistry,
                TypeParser(), objectMapper, QueryParamsBuilder(DefaultQueryParamsParser()))
    }
}

class ParamProvider(val context: ApplicationContext) : RepositoryParameterProvider {

    override fun <T> provide(method: Method?, parameterIndex: Int): T {
        val aClass = method!!.getParameterTypes()[parameterIndex]
        val bean = context.getBean(aClass);

        return bean as T;
    }

}
