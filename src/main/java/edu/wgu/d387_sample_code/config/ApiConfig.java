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
