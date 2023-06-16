package com.example;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import static junit.framework.TestCase.*;

public class SampleClientTest {
    private SampleClient sampleClient;
    private IGenericClient client;
    @BeforeEach
    public void setUp() {
        sampleClient = new SampleClient();
        FhirContext fhirContext = FhirContext.forR4();
        client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(true));
    }

    @Test
    public void testReadLastNamesFromFile() {
        // Prepare test data
        String fileName = "test_last_names.txt";
        List<String> expectedLastNames = List.of("Smith", "Johnson", "Doe");

        // Perform readLastNamesFromFile
        List<String> lastNames = sampleClient.readLastNamesFromFile(fileName);

        // Verify the result
        assertNotNull(lastNames);
        assertEquals(expectedLastNames, lastNames);
    }

    @Test
    public void testPerformPatientSearch_HappyPath() {
        // Prepare test data
        String lastName = "Smith";

        // Perform the patient search
        Bundle resultBundle = sampleClient.performPatientSearch(client, lastName);
        Patient patient = (Patient) resultBundle.getEntry().get(0).getResource();

        // Verify the result
        assertEquals("Smith", patient.getNameFirstRep().getFamily());
    }
    @Test
    public void testPerformPatientSearch_UnhappyPath() {
        // Prepare test data
        String lastName = "###"; //invalid last name

        // Perform the patient search
        Bundle resultBundle = sampleClient.performPatientSearch(client, lastName);

        // Verify the returned list is empty
        assertTrue(resultBundle.getEntry().isEmpty());
    }
}
