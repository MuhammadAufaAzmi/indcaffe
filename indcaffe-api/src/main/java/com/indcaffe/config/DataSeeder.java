package com.indcaffe.config;

import com.indcaffe.entity.*;
import com.indcaffe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;
    @Autowired
    CafeRepository cafeRepository;
    @Autowired
    MitraRepository mitraRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    SurplusPostRepository surplusPostRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seeder disabled based on user request to "delete semua user"
        /*
        if (userRepository.count() == 0) {
            // Seed Admin Cafe
        */
    }
}
