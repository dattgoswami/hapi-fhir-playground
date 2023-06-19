# HAPI FHIR Playground: Basic

This project is a skeleton project for using [HAPI FHIR](https://hapifhir.io) to access a public [FHIR](http://hl7.org/fhir/) server hosted at [http://hapi.fhir.org/baseR4](http://hapi.fhir.org/baseR4).

### Getting Started:

* [x] Take a few minutes to familiarize yourself with the [FHIR Standard](http://hl7.org/fhir/) for health data exchange. In particular you might want to read the [Executive Summary](http://hl7.org/fhir/summary.html) and the [Developer Introduction](http://hl7.org/fhir/overview-dev.html)

* [x] Try clicking on the link below. It is a FHIR *Search* operation used to look for patients with the name "Smith". (This is a publically accessible test server used by people all over the world, so we don't control what data is on it. Sometimes you may find unexpected or weird data there.) 

  http://hapi.fhir.org/baseR4/Patient?family=SMITH
  
* [x] Create your own GitHub project and copy the contents of this repository into your own project (please don't fork this repository)

* [x] Locate the class `SampleClient` and run it. This class runs the same search shown above.

* [x] **Please, do not fork this repo.** Create your own private GitHub repository to do your work in.

### Basic Tasks:

* [x] Modify `SampleClient` so that it prints the first and last name, and birth date of each Patient to the screen

* [x] Sort the output so that the results are ordered by the patient's first name

* [x] Commit your work

### Intermediate Tasks:

* [x] Create a text file containing 20 different last names

* [x] Modify 'SampleClient' so that instead of searching for patients with last name 'SMITH',
      it reads in the contents of this file and for each last name queries for patients with that last name

* [x] Print the average response time for these 20 searches by implementing an IClientInterceptor that uses
      the requestStopWatch to determine the response time of each request.

* [x] Run this loop three times, printing the average response time for each loop.  The first two times the loop should
      run as described above.  The third time the loop of 20 searches is run, the searches should be performed with
      caching disabled.

* [x] If there is enough time between runs, you should expect to see loop 2 with a shorter average response time than loop 1 and 3.

* [x] Please include unit tests for your work

* [x] Commit your work

### Further enhancements:
1. The entry point for the application(main method) can be separated from the SampleClient.
2. Batching can be used to reduce the number of API calls made to the server. It could be done by changing the type of 
    Bundle and then using BundleEntryComponent to accumulate the last names from that batch to query. 
3. performPatientSearch method can be made asynchronous to return CompletableFuture<Bundle> instead of Bundle.

Note: Caching can also be disabled by sleeping for > 61s duration before the next batch of requests and we'd get a cache miss
  Reference: https://smilecdr.com/docs/fhir_storage_relational/performance_and_caching.html