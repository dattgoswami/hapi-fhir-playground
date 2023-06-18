package com.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SampleClient {

    public static void main(String[] args) {
        List<String> lastNames = readLastNamesFromFile("last_names.txt");

        IGenericClient client = createFhirClientForR4();

        ResponseTimeInterceptor interceptor = new ResponseTimeInterceptor();
        client.registerInterceptor(interceptor);

        for (int i = 0; i < 3; i++) {
            if (i == 2) {
                disableCaching(client);
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (String lastName : lastNames) { // here parallel streams with a sleep timer of >~250ms was tried but it gives http 429
                    Bundle response = performPatientSearch(client, lastName);
//                        printPatientInformation(response);
                }
            });
            handleFutureExecution(future);

            double averageResponseTime = interceptor.getAverageResponseTime();
            System.out.println("Average Response Time for loop " + (i + 1) + (i == 2 ? " (No Caching): " : ": ") + averageResponseTime + " ms");
        }
    }
    private static IGenericClient createFhirClientForR4() {
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));
        return client;
    }
    private static Bundle performPatientSearch(IGenericClient client, String lastName) {
        return client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(lastName))
                .returnBundle(Bundle.class)
                .execute();
    }
    private static void handleFutureExecution(CompletableFuture<Void> future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error occurred while waiting for the future: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error occurred during the execution of the future: " + e.getMessage(), e);
        }
    }
    private static void printPatientInformation(Bundle response) {
        List<Bundle.BundleEntryComponent> entries = response.getEntry();
        entries.sort(Comparator.comparing(entry -> getFirstName(entry)));

        for (Bundle.BundleEntryComponent entry : entries) {
            Patient patient = extractPatient(entry);
            String firstName = getFirstName(patient);
            String lastName = getLastName(patient);
            Date birthDate = patient.getBirthDate();
            System.out.println("First Name: " + firstName);
            System.out.println("Last Name: " + lastName);
            System.out.println("Birth Date: " + birthDate);
        }
    }
    private static String getFirstName(Bundle.BundleEntryComponent entry) {
        Patient patient = extractPatient(entry);
        return getFirstName(patient);
    }
    private static String getFirstName(Patient patient) {
        return patient.getNameFirstRep().getGivenAsSingleString();
    }
    private static String getLastName(Patient patient) {
        return patient.getNameFirstRep().getFamily();
    }
    private static Patient extractPatient(Bundle.BundleEntryComponent entry) {
        if (entry.hasResource() && entry.getResource() instanceof Patient) {
            return (Patient) entry.getResource();
        } else {
            throw new IllegalArgumentException("Invalid entry or resource type");
        }
    }
    private static List<String> readLastNamesFromFile(String fileName) {
        List<String> lastNames = null;
        try {
            Path filePath = Paths.get("src/main/data", fileName);
            lastNames = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading last names from file: " + e.getMessage());
        }
        return lastNames;
    }
    private static void disableCaching(IGenericClient client) {
        client.registerInterceptor(new CacheControlInterceptor());
    }

}
