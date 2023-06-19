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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SampleClient {
    private static final String FILE_PATH = "src/main/data";
    private static final String FHIR_SERVER_URL_R4 = "http://hapi.fhir.org/baseR4";

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
                }
            });
            handleFutureExecution(future);

            double averageResponseTime = interceptor.getAverageResponseTime();
            System.out.println("Average Response Time for loop " + (i + 1) + (i == 2 ? " (No Caching): " : ": ") + averageResponseTime + " ms");
        }
    }
    static IGenericClient createFhirClientForR4() {
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL_R4);
        client.registerInterceptor(new LoggingInterceptor(false));
        return client;
    }
    static Bundle performPatientSearch(IGenericClient client, String lastName) {
        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty.");
        }
        return client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(lastName))
                .returnBundle(Bundle.class)
                .execute();
    }
    static void handleFutureExecution(CompletableFuture<Void> future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error occurred while waiting for the future: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error occurred during the execution of the future: " + e.getMessage(), e);
        }
    }
    static List<String> readLastNamesFromFile(String fileName) {
        List<String> lastNames = null;
        try {
            Path filePath = Paths.get(FILE_PATH, fileName);
            lastNames = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading last names from file: " + e.getMessage());
        }
        return lastNames;
    }
    static void disableCaching(IGenericClient client) {
        client.registerInterceptor(new CacheControlInterceptor());
    }
}
