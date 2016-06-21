package io.katharsis.locator;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

public class SampleRepositoryFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onValidClassShouldReturnInstance() {
        // GIVEN
        NewInstanceRepositoryFactory sut = new NewInstanceRepositoryFactory();

        // WHEN
        Object object = sut.getInstance(Object.class);

        // THEN
        Assert.assertNotNull(object);
    }

    @Test
    public void onClassWithPrivateConstructorShouldThrowException() {
        // GIVEN
        NewInstanceRepositoryFactory sut = new NewInstanceRepositoryFactory();

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.getInstance(Arrays.class);
    }
}
