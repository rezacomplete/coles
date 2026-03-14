package com.example.coles.api;

public class ProcessRequest {
    private boolean simulateFailure = false;

    public ProcessRequest() {
    }

    public boolean isSimulateFailure() {
        return simulateFailure;
    }

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }
}
