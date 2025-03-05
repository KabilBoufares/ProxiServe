package com.proxiserve.controller;

import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public HealthCheckController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/ping")
    public String ping() {
        return "API is alive! kabil is here";
    }

    @GetMapping("/test-db")
    public String testDB() {
        User user = new User();
        user.setEmail("test1@hebil1.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole("CLIENT");
        userRepository.save(user);
        return "Data saved to MongoDB!";
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