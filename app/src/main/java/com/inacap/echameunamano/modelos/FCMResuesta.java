package com.inacap.echameunamano.modelos;

import java.util.ArrayList;

public class FCMResuesta {
    private double multicast_id;
    private double success;
    private double failure;
    private double canonical_ids;
    ArrayList<Object> results = new ArrayList<Object>();

    public FCMResuesta(double multicast_id, double success, double failure, double canonical_ids, ArrayList<Object> results) {
        this.multicast_id = multicast_id;
        this.success = success;
        this.failure = failure;
        this.canonical_ids = canonical_ids;
        this.results = results;
    }

    public double getMulticast_id() {
        return multicast_id;
    }

    public void setMulticast_id(double multicast_id) {
        this.multicast_id = multicast_id;
    }

    public double getSuccess() {
        return success;
    }

    public void setSuccess(double success) {
        this.success = success;
    }

    public double getFailure() {
        return failure;
    }

    public void setFailure(double failure) {
        this.failure = failure;
    }

    public double getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(double canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public ArrayList<Object> getResults() {
        return results;
    }

    public void setResults(ArrayList<Object> results) {
        this.results = results;
    }
}
