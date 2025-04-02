package com.proxiserve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // correspond à src/main/resources/templates/login.html
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup"; // si tu as signup.html aussi
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // page de redirection après login (à créer)
    }
}
