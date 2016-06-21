package io.katharsis.resource.registry.repository;

import io.katharsis.locator.RepositoryFactory;
import io.katharsis.locator.NewInstanceRepositoryFactory;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class AnnotatedRelationshipEntryBuilderTest {

    @Test
    public void onInstanceOfAnnotatedRelationshipRepositoryShouldReturnTargetClass() {

        // GIVEN
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(
            new RepositoryInstanceBuilder(new NewInstanceRepositoryFactory(), SimpleRelationshipRepository.class));


        // WHEN
        final Class<?> targetClass = builder.getTargetAffiliation();

        // THEN
        assertThat(targetClass).isEqualTo(Character.class);
    }

    @Test
    public void onInstanceOfAnonymousDescendantOfAnnotatedRelationshipRepositoryShouldReturnTargetClass() {

        // GIVEN
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(
            new RepositoryInstanceBuilder(new RepositoryFactory() {
                @Override
                public <T> T getInstance(Class<T> clazz) {
                    return (T) new SimpleRelationshipRepository() {};
                }

                @Override
                public <R> R build(Class<R> clazz) {
                    return null;
                }
            }, SimpleRelationshipRepository.class)
        );

        // WHEN
        final Class<?> targetClass = builder.getTargetAffiliation();

        // THEN
        assertThat(targetClass).isEqualTo(Character.class);

    }

    @Test
    public void onInstanceOfNonAnnotatedClassShouldThrowIllegalArgumentException() {

        // GIVEN
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(
            new RepositoryInstanceBuilder(new RepositoryFactory() {
                @Override
                public <T> T getInstance(Class<T> clazz) {
                    return (T) new Object();
                }

                @Override
                public <R> R build(Class<R> clazz) {
                    return null;
                }
            }, SimpleRelationshipRepository.class)
        );

        // WHEN
        try {
            builder.getTargetAffiliation();
        } catch (Exception e) {
            // THEN
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @JsonApiRelationshipRepository(source = String.class, target = Character.class)
    public static class SimpleRelationshipRepository {

    }

}
