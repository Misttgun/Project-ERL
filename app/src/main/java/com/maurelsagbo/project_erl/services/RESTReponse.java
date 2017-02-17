package com.maurelsagbo.project_erl.services;

public interface RESTReponse {

    /**
     * Called upon successful completion of a HTTP request with the JSON that the server responded with.
     *
     * @param json
     */
    void success(String json);

    /**
     * Called if the HTTP request fails for any reason.
     *
     * @param ex
     */
    void fail(Exception ex);
}
