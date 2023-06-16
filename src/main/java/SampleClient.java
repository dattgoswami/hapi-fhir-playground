import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SampleClient {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();

        printPatientInformation(response);
    }
    private static void printPatientInformation(Bundle response) {
        List<Bundle.BundleEntryComponent> entries = response.getEntry();

        // Sort the response by first name
        entries.sort(Comparator.comparing(entry -> getFirstName(entry)));

        // Print Patient information
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

}
