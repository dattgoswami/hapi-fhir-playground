package com.example;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

public class ResponseTimeInterceptor implements IClientInterceptor {
    private int requestCount;
    private long totalResponseTime;

    @Override
    public void interceptRequest(IHttpRequest theRequest) {
        // Not used
    }

    @Override
    public void interceptResponse(IHttpResponse response) {
        requestCount++;
        totalResponseTime += response.getRequestStopWatch().getMillis();
    }

    public double getAverageResponseTime() {
        if (requestCount > 0) {
            return (double) totalResponseTime / requestCount;
        }
        return 0;
    }
}
