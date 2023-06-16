package com.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SampleClient {

    public static void main(String[] theArgs) {
        try {
            List<String> lastNames = readLastNamesFromFile("last_names.txt");

            FhirContext fhirContext = FhirContext.forR4();
            IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
            client.registerInterceptor(new LoggingInterceptor(false));

            ResponseTimeInterceptor interceptor = new ResponseTimeInterceptor();
            client.registerInterceptor(interceptor);

            for (int i = 0; i < 3; i++) {
                if(i == 2){
                    disableCaching(client);
                }
                for (String lastName : lastNames) {
                    Bundle response = performPatientSearch(client, lastName);
                    printPatientInformation(response); //it can be removed if we only want the average response time for each loop
                }
                double averageResponseTime = interceptor.getAverageResponseTime();
                System.out.println("Average Response Time for loop " + (i + 1) + (i == 2 ? " (No Caching): " : ": ") + averageResponseTime + " ms");
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    public static Bundle performPatientSearch(IGenericClient client, String lastName) {
        return client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(lastName))
                .returnBundle(Bundle.class)
                .execute();
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
    public static List<String> readLastNamesFromFile(String fileName) {
        List<String> lastNames = null;
        try {
            Path filePath = Paths.get("src/main/data", fileName);
            lastNames = Files.readAllLines(filePath);
        } catch (Exception e) {
            System.err.println("Error reading last names from file: " + e.getMessage());
        }
        return lastNames;
    }

    public static void disableCaching(IGenericClient client) {
        client.registerInterceptor(new CacheControlInterceptor());
    }

}
