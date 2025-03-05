package com.proxiserve.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class HealthCheckController {

    @GetMapping("/ping")
    public String ping() {
        return "API is alive! kabil is here";
    }
}




























/*@RestController
public class HealthCheckController {
    
    @GetMapping("/ping")
    public String ping() {
        return "API is alive! kabil is here";
    }

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test-db")
    public String testDB() {
        User user = new User();
        user.setEmail("test1@hebil1.com");
        user.setPassword("123456");
        user.setRole("CLIENT");

        userRepository.save(user);
        return "Data saved to MongoDB!";
    }
}
*/