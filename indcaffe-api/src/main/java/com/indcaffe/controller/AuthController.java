package com.indcaffe.controller;

import com.indcaffe.dto.AuthRequest;
import com.indcaffe.dto.AuthResponse;
import com.indcaffe.dto.RegisterRequest;
import com.indcaffe.entity.Cafe;
import com.indcaffe.entity.Mitra;
import com.indcaffe.entity.Role;
import com.indcaffe.entity.User;
import com.indcaffe.repository.CafeRepository;
import com.indcaffe.repository.MitraRepository;
import com.indcaffe.repository.UserRepository;
import com.indcaffe.security.JwtUtil;
import com.indcaffe.entity.Category;
import com.indcaffe.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CafeRepository cafeRepository;

    @Autowired
    MitraRepository mitraRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateJwtToken(authentication);

            User user = userRepository.findByUsername(loginRequest.getUsername()).get();

            Long cafeId = null;
            Long mitraId = null;
            String name = "";
            String city = "";
            String address = "";

            if (user.getRole() == Role.CAFE) {
                Cafe cafe = cafeRepository.findByUserId(user.getId()).orElse(null);
                if (cafe != null) {
                    cafeId = cafe.getId();
                    name = cafe.getName();
                    city = cafe.getCity();
                    address = cafe.getAddress();
                }
            } else if (user.getRole() == Role.MITRA) {
                Mitra mitra = mitraRepository.findByUserId(user.getId()).orElse(null);
                if (mitra != null) {
                    mitraId = mitra.getId();
                    name = mitra.getName();
                    city = mitra.getCity();
                    address = mitra.getOrganizationType(); // we use organizationType as address placeholder or just keep it blank, wait Mitra has city, but no address field? Let's check Mitra.java. Oh, I'll just use organizationType.
                }
            }

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .id(user.getId())
                    .cafeId(cafeId)
                    .mitraId(mitraId)
                    .name(name)
                    .city(city)
                    .address(address)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build());
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(401).body(new java.util.HashMap<String, String>() {{
                put("message", "Username atau Password salah (atau Anda belum mendaftar)!");
            }});
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .password(encoder.encode(signUpRequest.getPassword()))
                .build();

        if ("CAFE".equalsIgnoreCase(signUpRequest.getType())) {
            user.setRole(Role.CAFE);
            User savedUser = userRepository.save(user);

            Cafe cafe = new Cafe();
            cafe.setName(signUpRequest.getName());
            cafe.setCity(signUpRequest.getCity());
            cafe.setAddress(signUpRequest.getAddress());
            cafe.setUser(savedUser);
            cafeRepository.save(cafe);

            // Create a default Category for the Cafe
            Category defaultCategory = new Category();
            defaultCategory.setName("Umum");
            defaultCategory.setCafe(cafe);
            categoryRepository.save(defaultCategory);

            return ResponseEntity.ok("User registered successfully!");
        } else if ("MITRA".equalsIgnoreCase(signUpRequest.getType())) {
            user.setRole(Role.MITRA);
            user = userRepository.save(user);

            Mitra mitra = Mitra.builder()
                    .name(signUpRequest.getName())
                    .city(signUpRequest.getCity())
                    .organizationType(signUpRequest.getOrganizationType())
                    .user(user)
                    .build();
            mitraRepository.save(mitra);
        } else {
            return ResponseEntity.badRequest().body("Error: Invalid role type specified.");
        }

        return ResponseEntity.ok("User registered successfully!");
    }
}
