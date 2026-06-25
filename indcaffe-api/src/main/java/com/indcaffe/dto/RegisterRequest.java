package com.indcaffe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    
    // Type specific fields
    private String type; // "CAFE" or "MITRA"
    
    // Cafe/Mitra details
    @NotBlank
    private String name;
    private String city;
    private String address; // for Cafe
    private String organizationType; // for Mitra
}
