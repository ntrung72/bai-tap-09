package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    String login() {
        return "auth/login";
    }

    @GetMapping("/dashboard")
    String dashboard() {
        return "dashboard";
    }
}
