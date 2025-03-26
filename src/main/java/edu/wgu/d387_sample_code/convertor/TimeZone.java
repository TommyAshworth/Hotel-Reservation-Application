package edu.wgu.d387_sample_code.convertor;

//Imports exception handling for JSON processing
import com.fasterxml.jackson.core.JsonProcessingException;


/**
 * TimeZone interface defines methods for managing time zones
 * and converting time zone data into JSON format.
 */
public interface TimeZone {


    public void addTimeZones();

    //Converts the stored time zone data into a JSON-formatted string.
    public String timesToJson() throws JsonProcessingException;
}
