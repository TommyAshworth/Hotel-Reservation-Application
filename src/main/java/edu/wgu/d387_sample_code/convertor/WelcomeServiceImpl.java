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