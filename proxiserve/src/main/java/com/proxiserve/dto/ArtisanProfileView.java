package com.proxiserve.dto;

import java.util.List;

import com.proxiserve.model.Artisan;
import com.proxiserve.model.Certification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtisanProfileView {
    private Artisan artisan;
    private List<Certification> certifications;
    
}
