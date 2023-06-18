package com.example;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;


import static junit.framework.TestCase.*;

public class SampleClientTest {
    private SampleClient sampleClient;
    private IGenericClient client;
    private Method performPatientSearchMethod;
    private Method readLastNamesFromFileMethod;
    private Method createFhirClientForR4;
    private Method handleFutureExecutionMethod;
    private Method printPatientInformationMethod;
    @Before
    public void setUp() {
        sampleClient = new SampleClient();
        FhirContext fhirContext = FhirContext.forR4();
        client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));
        performPatientSearchMethod = getReflectionMethod(SampleClient.class, "performPatientSearch", IGenericClient.class, String.class);
        readLastNamesFromFileMethod = getReflectionMethod(SampleClient.class, "readLastNamesFromFile", String.class);
        createFhirClientForR4 = getReflectionMethod(SampleClient.class, "createFhirClientForR4");
        handleFutureExecutionMethod = getReflectionMethod(SampleClient.class, "handleFutureExecution", CompletableFuture.class);
        printPatientInformationMethod = getReflectionMethod(SampleClient.class, "printPatientInformation", Bundle.class);
    }
    private Method getReflectionMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            fail("An error occurred while accessing the methods: " + e.getMessage());
            return null; // unreachable code, added to satisfy the compiler
        }
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testHandleFutureExecution_SuccessfulCompletion() {
        // Prepare test data
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

        // Invoke the handleFutureExecution method
        try {
            handleFutureExecutionMethod.invoke(sampleClient, future);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail("Error occurred during test execution: " + e.getMessage());
            return;
        }
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testHandleFutureExecution_InterruptedException() throws IllegalAccessException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new InterruptedException("Test exception"));

        // Verify the behavior when the future throws an InterruptedException
        try {
            handleFutureExecutionMethod.invoke(null, future);
            fail("Expected RuntimeException to be thrown");
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                String expectedErrorMessage = "Error occurred during the execution of the future";
                String actualErrorMessage = e.getCause().getMessage();
                assertTrue(actualErrorMessage.contains(expectedErrorMessage));
            } else {
                // Catch any exceptions during re-throwing and fail the test
                try {
                    throw e.getCause();
                } catch (Throwable throwable) {
                    fail("Unexpected exception occurred during re-throwing: " + throwable.getMessage());
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testHandleFutureExecution_ExecutionException() throws InvocationTargetException, IllegalAccessException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new ExecutionException("Test exception", null));

        // Verify the behavior when the future throws an ExecutionException
        try {
            handleFutureExecutionMethod.invoke(null, future);
            fail("Expected RuntimeException to be thrown");
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                String expectedErrorMessage = "Error occurred during the execution of the future";
                String actualErrorMessage = e.getCause().getMessage();
                assertTrue(actualErrorMessage.contains(expectedErrorMessage));
            } else {
                fail("Unexpected exception occurred during re-throwing: " + e.getCause().getMessage());
            }
        }
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateFhirClientForR4() {
        // Invoke the method under test
        IGenericClient client;
        try {
            client = (IGenericClient) createFhirClientForR4.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail("Error occurred during test execution: " + e.getMessage());
            return;
        }

        // Verify that the returned IGenericClient instance is not null
        assertNotNull(client);
        assertTrue(client instanceof IGenericClient);

        // Verify the behavior of the LoggingInterceptor registration
        boolean interceptorRegistered = false;
        for (Object interceptor : client.getInterceptorService().getAllRegisteredInterceptors()) {
            if (interceptor instanceof LoggingInterceptor) {
                interceptorRegistered = true;
                break;
            }
        }
        assertTrue("LoggingInterceptor should be registered", interceptorRegistered);
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testReadLastNamesFromFile() {
        // Prepare test data
        String fileName = "test_last_names.txt";
        List<String> expectedLastNames = Arrays.asList("Smith", "Johnson", "Doe");

        // Perform readLastNamesFromFile
        List<String> lastNames;
        try {
            lastNames = (List<String>) readLastNamesFromFileMethod.invoke(sampleClient, fileName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail("Error occurred during test execution: " + e.getMessage());
            return;
        }
        // Verify the result
        assertNotNull(lastNames);
        assertEquals(expectedLastNames, lastNames);
    }
    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void testReadLastNamesFromFile_FileNotFound() throws Throwable {
        String nonExistentFileName = "non_existent_file.txt";
        try {
            List<String> lastNames = (List<String>) readLastNamesFromFileMethod.invoke(sampleClient, nonExistentFileName);
        } catch (InvocationTargetException e) {
            throw e.getCause();  // Throw the cause of the InvocationTargetException (i.e., the RuntimeException)
        } catch (IllegalAccessException e) {
            fail("Error occurred during test execution: " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testPerformPatientSearch_ValidLastName() {
        // Prepare test data
        String lastName = "Smith";

        // Perform the patient search
        Bundle resultBundle;
        try {
            resultBundle = (Bundle) performPatientSearchMethod.invoke(sampleClient, client, lastName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail("Error occurred during test execution: " + e.getMessage());
            return;
        }
        assertNotNull(resultBundle);
        Patient patient = (Patient) resultBundle.getEntry().get(0).getResource();

        // Verify the result
        assertEquals("Smith", patient.getNameFirstRep().getFamily());
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testPerformPatientSearch_InvalidLastName() {
        // Prepare test data
        String lastName = "###"; //invalid last name

        // Perform the patient search
        Bundle resultBundle;
        try {
            resultBundle = (Bundle) performPatientSearchMethod.invoke(sampleClient, client, lastName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail("Error occurred during test execution: " + e.getMessage());
            return;
        }

        // Verify the returned list is empty
        assertTrue(resultBundle.getEntry().isEmpty());
    }
}
