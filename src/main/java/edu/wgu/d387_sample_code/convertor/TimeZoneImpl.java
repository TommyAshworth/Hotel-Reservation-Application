package edu.wgu.d387_sample_code.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Setter
@Getter
public class TimeZoneImpl implements TimeZone {
    //List to store the converted time zone values as LocalDateTime objects.
    private List<LocalDateTime> times;

    @Override
    public void addTimeZones() {

        //Initialize the list before adding new values.
        this.times = new ArrayList<>();

        //Define time zones for conversion

        //Easter time
        ZoneId eastern = ZoneId.of("America/New_York");
        //Mountain Time
        ZoneId mountain = ZoneId.of("America/Phoenix");
        // Coordinated Universal Time
        ZoneId utc = ZoneId.of("UTC");
        //Systems current time zone
        ZoneId zoneId = ZoneId.systemDefault();

        //Get the current local date and time
        LocalDateTime localDateTime = LocalDateTime.now();
        //Convert the local time into system's default time zone.
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        this.times.add(localDateTime);

        //Convert to Eastern time and add to the list.
        ZonedDateTime easternZone = zonedDateTime.withZoneSameInstant(eastern);
        LocalDateTime easternLocal = easternZone.toLocalDateTime();
        this.times.add(easternLocal);

        //Convert to Mountain time and add to the list
        ZonedDateTime mountainZone = zonedDateTime.withZoneSameInstant(mountain);
        LocalDateTime mountainLocal = mountainZone.toLocalDateTime();
        this.times.add(mountainLocal);

        //Convert to UTC and add to the list
        ZonedDateTime utcZone = zonedDateTime.withZoneSameInstant(utc);
        LocalDateTime utcLocal = utcZone.toLocalDateTime();
        this.times.add(utcLocal);
    }

    //Converts the list of time zone values into a JSON-formatted string.

    @Override
    public String timesToJson() throws JsonProcessingException {

        //Create an ObjectMapper to convert objects into JSON
        ObjectMapper mapper = new ObjectMapper();

        //Register the JavaTimeModule properly handle LocalDateTime serialization.
        mapper.registerModule(new JavaTimeModule());

        //Disable writing dates as timestamps to ensure human-readable output.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        //Convert the times list into a JSON string and return it
        return mapper.writeValueAsString(this.times);
    }
}