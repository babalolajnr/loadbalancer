package com.loadbalancer;

public interface ResponseMatcher {
    String getResponse();

    String getKey();

    void setResponse(String response);
}
