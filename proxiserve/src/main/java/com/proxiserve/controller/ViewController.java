package com.proxiserve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.ui.Model;

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
    @GetMapping("/request-reset-password")
    public String showRequestResetPasswordPage() {
        return "request-reset-password"; // templates/request-reset-password.html
    }
    
     @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password"; // templates/reset-password.html
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
