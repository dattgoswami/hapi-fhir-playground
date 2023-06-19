package com.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

public class SampleClientTest {
    private SampleClient sampleClient;
    private IGenericClient client;
    @Before
    public void setUp() {
        sampleClient = new SampleClient();
        FhirContext fhirContext = FhirContext.forR4();
        client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));
    }
    @Test
    public void testHandleFutureExecution_SuccessfulCompletion() {
        // Prepare test data
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

        // Invoke the handleFutureExecution method
        sampleClient.handleFutureExecution(future);
    }
    @Test
    public void testHandleFutureExecution_InterruptedException() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new InterruptedException("Simulated InterruptedException"));

        // Verify the behavior when the future throws an InterruptedException
        try {
            sampleClient.handleFutureExecution(future);
        } catch (RuntimeException e) {
            String expectedErrorMessage = "Simulated InterruptedException";
            String actualErrorMessage = e.getCause().getMessage();
            assertTrue(actualErrorMessage.contains(expectedErrorMessage));
        }
    }
    @Test
    public void testHandleFutureExecution_ExecutionException() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new ExecutionException("Simulated ExecutionException", new RuntimeException()));

        // Verify the behavior when the future throws an ExecutionException
        try {
            sampleClient.handleFutureExecution(future);
        } catch (RuntimeException e) {
            String expectedErrorMessage = "Simulated ExecutionException";
            String actualErrorMessage = e.getCause().getMessage();
            assertTrue(actualErrorMessage.contains(expectedErrorMessage));
        }
    }
    @Test
    public void testCreateFhirClientForR4() {
        // Invoke the method under test
        IGenericClient client = sampleClient.createFhirClientForR4();

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
    @Test
    public void testReadLastNamesFromFile() {
        // Prepare test data
        String fileName = "test_last_names.txt";
        List<String> expectedLastNames = Arrays.asList("Smith", "Johnson", "Doe");

        // Perform readLastNamesFromFile
        List<String> lastNames = sampleClient.readLastNamesFromFile(fileName);

        // Verify the result
        assertNotNull(lastNames);
        assertEquals(expectedLastNames, lastNames);
    }
    @Test(expected = RuntimeException.class)
    public void testReadLastNamesFromFile_FileNotFound() {
        String nonExistentFileName = "non_existent_file.txt";

        try {
            List<String> lastNames = sampleClient.readLastNamesFromFile(nonExistentFileName);
            fail("Expected exception not thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Error reading last names from file: "));
            throw e;
        }
    }
    @Test
    public void testPerformPatientSearch_ValidLastName() {
        // Prepare test data
        String lastName = "Smith";

        // Perform the patient search
        Bundle resultBundle = sampleClient.performPatientSearch( client, lastName);

        assertNotNull(resultBundle);
        Patient patient = (Patient) resultBundle.getEntry().get(0).getResource();

        // Verify the result
        assertEquals("Smith", patient.getNameFirstRep().getFamily());
    }
    @Test
    public void testPerformPatientSearch_InvalidLastName() {
        // Prepare test data
        String lastName = "###"; //invalid last name

        // Perform the patient search
        Bundle resultBundle = sampleClient.performPatientSearch( client, lastName);

        // Verify the returned list is empty
        assertTrue(resultBundle.getEntry().isEmpty());
    }
    @Test
    public void testDisableCaching_RegisterCacheControlInterceptor() {
        // Create a mock IGenericClient
        IGenericClient mockedClient = mock(IGenericClient.class);

        // Invoke the method under test
        sampleClient.disableCaching(mockedClient);

        // Verify that the CacheControlInterceptor is registered
        verify(mockedClient).registerInterceptor(any(CacheControlInterceptor.class));
    }
    @Test
    public void testDisableCaching_RegisterCacheControlInterceptor_Failure() {
        // Create a mock IGenericClient
        IGenericClient mockedClient = mock(IGenericClient.class);

        // Throw an exception when trying to register the interceptor
        doThrow(new RuntimeException("Failed to register interceptor")).when(mockedClient).registerInterceptor(any(CacheControlInterceptor.class));

        // Invoke the method under test
        try {
            sampleClient.disableCaching(mockedClient);
            fail("Expected exception not thrown");
        } catch (RuntimeException e) {
            assertEquals("Failed to register interceptor", e.getMessage());
        }
    }
}
