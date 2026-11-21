package com.vcube.support.config;

import com.vcube.support.entity.Category;
import com.vcube.support.entity.User;
import com.vcube.support.repository.CategoryRepository;
import com.vcube.support.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(CategoryRepository categoryRepo, UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            if (categoryRepo.count() == 0) {
                List<String[]> defaults = List.of(
                    new String[]{"Billing & Payments", "Charges, refunds, invoices, and failed transactions", "12"},
                    new String[]{"Technical Issue", "App crashes, bugs, API errors, and performance issues", "8"},
                    new String[]{"Account & Login", "Password resets, locked accounts, and profile access", "6"},
                    new String[]{"Delivery & Shipping", "Delayed orders, damaged packages, and tracking", "24"},
                    new String[]{"General Inquiry", "Product questions, feature requests, and feedback", "48"}
                );
                for (String[] d : defaults) {
                    Category c = new Category();
                    c.setName(d[0]);
                    c.setDescription(d[1]);
                    c.setDefaultSlaHours(Integer.parseInt(d[2]));
                    categoryRepo.save(c);
                }
            }

            if (!userRepo.existsByEmail("customer@demo.com")) {
                User customer = new User();
                customer.setName("Aarav Sharma");
                customer.setEmail("customer@demo.com");
                customer.setPassword(encoder.encode("password123"));
                customer.setRole("CUSTOMER");
                userRepo.save(customer);
            }

            if (!userRepo.existsByEmail("agent1@demo.com")) {
                User agent1 = new User();
                agent1.setName("Priya Nair");
                agent1.setEmail("agent1@demo.com");
                agent1.setPassword(encoder.encode("password123"));
                agent1.setRole("AGENT");
                userRepo.save(agent1);
            }

            if (!userRepo.existsByEmail("agent2@demo.com")) {
                User agent2 = new User();
                agent2.setName("Rohan Verma");
                agent2.setEmail("agent2@demo.com");
                agent2.setPassword(encoder.encode("password123"));
                agent2.setRole("AGENT");
                userRepo.save(agent2);
            }
        };
    }
}
