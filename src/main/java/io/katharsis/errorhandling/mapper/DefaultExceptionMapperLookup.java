package io.katharsis.errorhandling.mapper;

import io.katharsis.resource.exception.init.InvalidResourceException;

import java.util.Set;

import org.reflections.Reflections;

import java8.util.stream.Collectors;
import static java8.util.stream.StreamSupport.stream;

/**
 * Exception mapper lookup which scans the classpath for exception mappers which
 * are annotated with the {@link ExceptionMapperProvider} annotation.
 */
public class DefaultExceptionMapperLookup implements ExceptionMapperLookup {
	private String resourceSearchPackage;

	public DefaultExceptionMapperLookup(String resourceSearchPackage) {
		this.resourceSearchPackage = resourceSearchPackage;
	}

	@Override
	public Set<JsonApiExceptionMapper> getExceptionMappers() {
		Reflections reflections;
		if (resourceSearchPackage != null) {
			String[] packageNames = resourceSearchPackage.split(",");
			reflections = new Reflections(packageNames);
		} else {
			reflections = new Reflections(resourceSearchPackage);
		}
		Set<Class<?>> exceptionMapperClasses = reflections.getTypesAnnotatedWith(ExceptionMapperProvider.class);

		return stream(exceptionMapperClasses).map((exceptionMapperClazz) -> {
			if (!JsonApiExceptionMapper.class.isAssignableFrom(exceptionMapperClazz)) {
				throw new InvalidResourceException(exceptionMapperClazz.getCanonicalName() + " is not an implementation of JsonApiExceptionMapper");
			}
			try {
				return (JsonApiExceptionMapper<? extends Throwable>) exceptionMapperClazz.newInstance();
			} catch (Exception e) {
				throw new InvalidResourceException(exceptionMapperClazz.getCanonicalName() + " can not be initialized", e);
			}
		}).collect(Collectors.toSet());
	}

}
