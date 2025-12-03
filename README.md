# Task B1
-Created two resource bundles with welcome messages, one in English and one in French
`translation.properties` empty
`translation_en_US.properties`
Line 1:
```
welcome=Welcome to Landon Hotel

```
`translation_fr_CA.properties`
Line 1:

```
welcome=Bienvenue à l'Hôtel Landon

```
-Created a class with a method that gets the message from the properties files. Added those messages to a JSON 
array or String array that I can parse later when I want to print to the front-end.

In the edu.wgu.d387_sample_code > convertor package
-Created `WelcomeService.java` interface with method for converting a string array to json. 

```
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


```
-Created WelcomeServiceImpl.java to handle concurrent loading of welcome messages from properties files in different
languages, using an ExecutorService for threading and a CountDownLatch to synchronize the completion of tasks before
returning the results as a JSON string."

```
package edu.wgu.d387_sample_code.convertor;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Service
public class WelcomeServiceImpl extends Thread implements WelcomeService {

    //List to store welcome messages 
    private List<String> messages;
    //Executor service to manage threads for loading property files
    private final ExecutorService executor = newFixedThreadPool(2);
    //Countdownlatch to ensure that both threads complete before returning the response
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public String getJsonList() throws JsonProcessingException, InterruptedException {

        //Wait for the latch to reach zero(indicating both tasks are complete)
        latch.await();
        synchronized (this) {
            //Convert the list of welcome messages to a JSON string and return it 
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this.messages);
        }
    }

    //Start the service by creating and starting a new thread
    public void startService() {

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {

        //Creates a properties object to load property files
        Properties properties = new Properties();
        //Intialize the messages list that will hold the welcome messages 
        this.messages = new ArrayList<>();

        synchronized (this) {
            
            //Execute the first task to load the English welcome message
            executor.execute(() -> {
                try {
                    InputStream stream = new ClassPathResource("translation_en_US.properties").getInputStream();
                    properties.load(stream);
                    //Add the english welcome message to the list 
                    this.messages.add(properties.getProperty("welcome"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            //Execute the second task to load the french welcome message 
            executor.execute(() -> {
                try {
                    InputStream stream = new ClassPathResource("translation_fr_CA.properties").getInputStream();
                    properties.load(stream);
                    //Add the french welcome message to the list 
                    this.messages.add(properties.getProperty("welcome"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            //Decrement the latch count, signaling that both tasks are initiated. 
            latch.countDown();
        }
    }
}

```

-Created a `WelcomeController.java` to create a mapping to retrieve the welcome data
```
package edu.wgu.d387_sample_code.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.wgu.d387_sample_code.convertor.WelcomeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
//Defines base URL mapping for controller
@RequestMapping("/api")
public class WelcomeController {

    //Injected service to handle welcome message logic
    private final WelcomeServiceImpl welcomeService;

    @Autowired
    public WelcomeController(WelcomeServiceImpl welcomeService) {

        //Initialize welcome service
        this.welcomeService = welcomeService;
    }

    //Mapping for GET requests to /api/welcome
    @GetMapping("/welcome")
    public String welcome() throws JsonProcessingException, InterruptedException {

        //Starts the service to fetch messages in a separate thread
        this.welcomeService.startService();
        // Returns the JSON List of welcome messages 
        return welcomeService.getJsonList();
    }
}

```

-Created ApiConfig.java 

```
package edu.wgu.d387_sample_code.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


//Marks this class as a configuration class for Spring
@Configuration
public class ApiConfig implements  WebMvcConfigurer{

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //Configures Cross-Origin Resource sharing (CORS) settings for the API
        registry.addMapping("/api/**") //Applies to any endpoint starting with /api
                //Allows requests from this front end address
                .allowedOrigins("http://localhost:4200")
                // Permits GET and POST HTTP methods
                .allowedMethods("GET", "POST");
    }

}



```

-Modified `app.component.ts`
Line 24 - 25: Added string to hold the URL for the welcome get mapping

```
//Defined a string variable 'welcomeUrl' that holds the complete URL for the 'welcome' GET mapping API endpoint.
  private welcomeUrl:string = this.baseURL + '/api/welcome';
```

Line 32-30: String array holds the data passed from the get request 

```
 //'welcomeMessages' is a string array that will hold the welcome messages returned from the GET request.
  // The 'getWelcome()' method sends a GET request to the 'welcomeUrl' and expects a response of type string in JSON format.

  welcomeMessages!:string[];

  getWelcome(): Observable<string> {
    return this.httpClient.get<string>(this.welcomeUrl,{responseType:'text' as 'json'});
  }

```
Lines 57-64: Parse Json string from get request into a javascript array 

```
    // Subscribing to the 'getWelcome()' method to get the response from the GET request.
    // The response (a JSON string) is parsed into a JavaScript array and assigned to 'welcomeMessages'.

      this.getWelcome().subscribe(
        (response) => {
          this.welcomeMessages = JSON.parse(response);
        }
      );

```
-Modified `app.component.html`
Line 18: Use ngFor to iterate through messages array and display each message 

```
<div class="welcome"><span class="welcome-message" *ngFor="let message of welcomeMessages">{{message}}</span></div>

```
# TASK B2:

-Created a display for Canadian Dollars (C$) and Euro(€)
-Modified `app.component.html`

```
<strong>Price: {{room.price | currency:'C'}}</strong>
<strong>Price: {{room.price | currency:'€'}}</strong>

```
# Task B3:

-Created `TimeZone.java` interface and a `TimeZoneImpl.java` file

`TimeZone.java`

```
package edu.wgu.d387_sample_code.convertor;

public interface TimeZone {
    String convertTimeZones();
}


```
`TimeZoneImpl.java`

```
package edu.wgu.d387_sample_code.convertor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

@SpringBootApplication
public class TimeZoneImpl implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TimeZone.class, args);
    }

    public void run(String... args) throws Exception {
        // Time Zones
        ZoneId zEastern=ZoneId.of("America/New_York");
        ZoneId zMountain=ZoneId.of("America/Cheyenne");
        ZoneId zUTC=ZoneId.of("UTC");
        ZoneId zoneId=ZoneId.systemDefault();

        //Local Time
        LocalDateTime localDateTime=LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        String timeEastern, timeMountain, timeUTC;
        // Eastern Time
        ZonedDateTime zonedDateTimeEastern=zonedDateTime.withZoneSameInstant(zEastern);
        timeEastern = "Eastern time (ET): " + zonedDateTimeEastern.toLocalDateTime().toString();

        // Mountain Time
        ZonedDateTime zonedDateTimeMountain=zonedDateTime.withZoneSameInstant(zMountain);
        timeMountain = "Mountain time (MT): " + zonedDateTimeMountain.toLocalDateTime().toString();

        ZonedDateTime zonedDateTimeUTC=zonedDateTime.withZoneSameInstant(zUTC);
        timeUTC = "UTC time (UT): " + zonedDateTimeUTC.toLocalDateTime().toString();

        String result = timeEastern + "\n" + timeMountain + "\n" + timeUTC;

        System.out.println(result);
    }

}


```
-Modified and corrected `app.component.html`
Added <br/> elements to each line 
```
<strong>Room #: {{room.roomNumber}}</strong><br/>
                  <strong>Price: ${{room.price}}</strong><br/>
                  <strong>Price: {{room.price | currency:'CAD'}}</strong><br/>
                  <strong>Price: {{room.price | currency:'EUR'}}</strong><br/>
```

-Modified and made changes to `TimeZoneImpl`
Deleted line 45: 

```
this.times.add(localDateTime);

```

-Changed line 35 `TimeZoneImpl`
Was not displaying appropriate time zone for Mountain Time, changed the City
```
ZoneId mountain = ZoneId.of("America/Denver");
```
# Task C1:
-Created Dockerfile, created image and tested the docker file. It is running.

```
2025-03-26 21:01:08 
2025-03-26 21:01:08   .   ____          _            __ _ _
2025-03-26 21:01:08  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
2025-03-26 21:01:08 ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
2025-03-26 21:01:08  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
2025-03-26 21:01:08   '  |____| .__|_| |_|_| |_\__, | / / / /
2025-03-26 21:01:08  =========|_|==============|___/=/_/_/_/
2025-03-26 21:01:08  :: Spring Boot ::                (v2.7.2)
2025-03-26 21:01:08 
2025-03-26 21:01:08 2025-03-27 01:01:08.264  INFO 1 --- [           main] e.w.d.D387SampleCodeApplication          : Starting D387SampleCodeApplication v0.0.2-SNAPSHOT using Java 17.0.2 on 1eb0afee3f90 with PID 1 (/app/app.jar started by root in /app)
2025-03-26 21:01:08 2025-03-27 01:01:08.267  INFO 1 --- [           main] e.w.d.D387SampleCodeApplication          : No active profile set, falling back to 1 default profile: "default"
2025-03-26 21:01:09 2025-03-27 01:01:09.141  INFO 1 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-03-26 21:01:09 2025-03-27 01:01:09.219  INFO 1 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 66 ms. Found 2 JPA repository interfaces.
2025-03-26 21:01:09 2025-03-27 01:01:09.997  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2025-03-26 21:01:10 2025-03-27 01:01:10.012  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-03-26 21:01:10 2025-03-27 01:01:10.012  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.65]
2025-03-26 21:01:10 2025-03-27 01:01:10.116  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-03-26 21:01:10 2025-03-27 01:01:10.116  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1795 ms
2025-03-26 21:01:10 2025-03-27 01:01:10.164  INFO 1 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-03-26 21:01:10 2025-03-27 01:01:10.509  INFO 1 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-03-26 21:01:10 2025-03-27 01:01:10.521  INFO 1 --- [           main] o.s.b.a.h2.H2ConsoleAutoConfiguration    : H2 console available at '/h2-console'. Database available at 'jdbc:h2:file:~/spring-boot-h2-d387F'
2025-03-26 21:01:10 2025-03-27 01:01:10.671  INFO 1 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-03-26 21:01:10 2025-03-27 01:01:10.779  INFO 1 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 5.6.10.Final
2025-03-26 21:01:11 2025-03-27 01:01:11.025  INFO 1 --- [           main] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.1.2.Final}
2025-03-26 21:01:11 2025-03-27 01:01:11.189  INFO 1 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.H2Dialect
2025-03-26 21:01:11 Hibernate: create table reservation (id bigint not null, checkin date not null, checkout date not null, room_id bigint not null, primary key (id))
2025-03-26 21:01:11 Hibernate: create table room (id bigint not null, price varchar(255), room_number integer, primary key (id))
2025-03-26 21:01:11 Hibernate: create table room_reservation_entity_list (room_entity_id bigint not null, reservation_entity_list_id bigint not null)
2025-03-26 21:01:11 Hibernate: alter table room_reservation_entity_list drop constraint if exists UK_h6i55s733sb2tdxct0osbolge
2025-03-26 21:01:11 Hibernate: alter table room_reservation_entity_list add constraint UK_h6i55s733sb2tdxct0osbolge unique (reservation_entity_list_id)
2025-03-26 21:01:11 Hibernate: create sequence hibernate_sequence start with 1 increment by 1
2025-03-26 21:01:11 Hibernate: alter table reservation add constraint FKm8xumi0g23038cw32oiva2ymw foreign key (room_id) references room
2025-03-26 21:01:11 Hibernate: alter table room_reservation_entity_list add constraint FKc3rrge7c3u2skn2x054yfun3j foreign key (reservation_entity_list_id) references reservation
2025-03-26 21:01:11 Hibernate: alter table room_reservation_entity_list add constraint FKk8erroom3f2oqbvkasu2iytke foreign key (room_entity_id) references room
2025-03-26 21:01:11 2025-03-27 01:01:11.920  INFO 1 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
2025-03-26 21:01:11 2025-03-27 01:01:11.927  INFO 1 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-03-26 21:01:12 2025-03-27 01:01:12.279  WARN 1 --- [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2025-03-26 21:01:12 2025-03-27 01:01:12.510  INFO 1 --- [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page: class path resource [static/index.html]
2025-03-26 21:01:12 2025-03-27 01:01:12.811  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2025-03-26 21:01:12 2025-03-27 01:01:12.826  INFO 1 --- [           main] e.w.d.D387SampleCodeApplication          : Started D387SampleCodeApplication in 5.048 seconds (JVM running for 6.004)
2025-03-26 21:01:12 Bootstrapping data: 
2025-03-26 21:01:12 Hibernate: select roomentity0_.id as id1_1_0_, roomentity0_.price as price2_1_0_, roomentity0_.room_number as room_num3_1_0_ from room roomentity0_ where roomentity0_.id=?
2025-03-26 21:01:12 Hibernate: call next value for hibernate_sequence
2025-03-26 21:01:12 Hibernate: insert into room (price, room_number, id) values (?, ?, ?)
2025-03-26 21:01:12 Hibernate: select roomentity0_.id as id1_1_0_, roomentity0_.price as price2_1_0_, roomentity0_.room_number as room_num3_1_0_ from room roomentity0_ where roomentity0_.id=?
2025-03-26 21:01:12 Hibernate: call next value for hibernate_sequence
2025-03-26 21:01:12 Hibernate: insert into room (price, room_number, id) values (?, ?, ?)
2025-03-26 21:01:12 Hibernate: select roomentity0_.id as id1_1_0_, roomentity0_.price as price2_1_0_, roomentity0_.room_number as room_num3_1_0_ from room roomentity0_ where roomentity0_.id=?
2025-03-26 21:01:12 Hibernate: call next value for hibernate_sequence
2025-03-26 21:01:12 Hibernate: insert into room (price, room_number, id) values (?, ?, ?)
2025-03-26 21:01:13 Hibernate: select roomentity0_.id as id1_1_, roomentity0_.price as price2_1_, roomentity0_.room_number as room_num3_1_ from room roomentity0_
2025-03-26 21:01:13 Hibernate: select reservatio0_.room_entity_id as room_ent1_2_0_, reservatio0_.reservation_entity_list_id as reservat2_2_0_, reservatio1_.id as id1_0_1_, reservatio1_.checkin as checkin2_0_1_, reservatio1_.checkout as checkout3_0_1_, reservatio1_.room_id as room_id4_0_1_, roomentity2_.id as id1_1_2_, roomentity2_.price as price2_1_2_, roomentity2_.room_number as room_num3_1_2_ from room_reservation_entity_list reservatio0_ inner join reservation reservatio1_ on reservatio0_.reservation_entity_list_id=reservatio1_.id inner join room roomentity2_ on reservatio1_.room_id=roomentity2_.id where reservatio0_.room_entity_id=?
2025-03-26 21:01:13 Hibernate: select reservatio0_.room_entity_id as room_ent1_2_0_, reservatio0_.reservation_entity_list_id as reservat2_2_0_, reservatio1_.id as id1_0_1_, reservatio1_.checkin as checkin2_0_1_, reservatio1_.checkout as checkout3_0_1_, reservatio1_.room_id as room_id4_0_1_, roomentity2_.id as id1_1_2_, roomentity2_.price as price2_1_2_, roomentity2_.room_number as room_num3_1_2_ from room_reservation_entity_list reservatio0_ inner join reservation reservatio1_ on reservatio0_.reservation_entity_list_id=reservatio1_.id inner join room roomentity2_ on reservatio1_.room_id=roomentity2_.id where reservatio0_.room_entity_id=?
2025-03-26 21:01:13 Hibernate: select reservatio0_.room_entity_id as room_ent1_2_0_, reservatio0_.reservation_entity_list_id as reservat2_2_0_, reservatio1_.id as id1_0_1_, reservatio1_.checkin as checkin2_0_1_, reservatio1_.checkout as checkout3_0_1_, reservatio1_.room_id as room_id4_0_1_, roomentity2_.id as id1_1_2_, roomentity2_.price as price2_1_2_, roomentity2_.room_number as room_num3_1_2_ from room_reservation_entity_list reservatio0_ inner join reservation reservatio1_ on reservatio0_.reservation_entity_list_id=reservatio1_.id inner join room roomentity2_ on reservatio1_.room_id=roomentity2_.id where reservatio0_.room_entity_id=?
2025-03-26 21:01:13 Printing out data: 
2025-03-26 21:01:13 405
2025-03-26 21:01:13 406
2025-03-26 21:01:13 407

```

