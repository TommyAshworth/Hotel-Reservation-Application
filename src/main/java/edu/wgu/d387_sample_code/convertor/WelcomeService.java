package edu.wgu.d387_sample_code.convertor;


import com.fasterxml.jackson.core.JsonProcessingException;
/*
* The WelcomeService interface defines methods to retrieve and convert welcome messages
* from resource bundles into JSON string.
* */

public interface WelcomeService {

    public void startService();
    public String getJsonList() throws JsonProcessingException, InterruptedException;
}
