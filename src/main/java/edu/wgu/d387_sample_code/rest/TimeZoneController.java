package edu.wgu.d387_sample_code.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.wgu.d387_sample_code.convertor.TimeZoneImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TimeZoneController {

    //Dependency for handling time zone conversions
    private final TimeZoneImpl timeZone;

    //Constructor based dependency injection for the TimeZoneImpl service
    @Autowired
    //Instructs Spring to inject an instance of TimeZoneImpl into this controller
    public TimeZoneController(TimeZoneImpl timeZone) {

        this.timeZone = timeZone;
    }

    /*
    * Handles GET requests to "/api/timezone"
    * Converts the current time to different time zones and returns the result in JSON format
     */

    @GetMapping("/timezone")
    public String timeZone() throws JsonProcessingException {

        this.timeZone.addTimeZones();
        return this.timeZone.timesToJson();
    }
}