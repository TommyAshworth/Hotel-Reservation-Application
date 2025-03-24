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