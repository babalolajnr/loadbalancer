package com.loadbalancer;

public class ResponseController implements ResponseMatcher {
    private final String request;
    private final String matchKey;
    private String response;

    public ResponseController(String message) {
        this.request = message;
        this.matchKey = getKey(message);
    }

    public String getResponse() {
        return response;
    }

    public String getKey() {
        return matchKey;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getKey(String request) {
        //set the matcher key
        return request.substring(0, 13);
    }
}
