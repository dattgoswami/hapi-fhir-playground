package com.example;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

public class CacheControlInterceptor implements IClientInterceptor {
    @Override
    public void interceptRequest(IHttpRequest theRequest) {
        theRequest.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        theRequest.addHeader("Pragma", "no-cache");
        theRequest.addHeader("Expires", "0");
    }

    @Override
    public void interceptResponse(IHttpResponse theResponse) {
        // Not used
    }
}
