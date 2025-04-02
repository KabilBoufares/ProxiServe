package com.proxiserve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Va chercher login.html dans templates/
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup"; // correspond à templates/signup.html (sans .html ici)
    }


    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard"; // Redirection après login (à créer)
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
 
}
