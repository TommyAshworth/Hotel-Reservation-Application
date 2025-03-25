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






