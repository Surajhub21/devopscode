package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@SpringBootApplication
@Controller
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/index.html");
    }

    @Autowired
    private ApplicationContext context;

    @GetMapping("/exit")
    public String shutdownApp() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000); // give response before shutting down
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            SpringApplication.exit(context ,() -> 0);
        });
        thread.setDaemon(false);
        thread.start();

        return "✅ Application shutting down...";
    }
}
