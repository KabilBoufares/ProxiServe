package com.proxiserve.dto;

import java.util.List;

import com.proxiserve.model.Artisan;
import com.proxiserve.model.User;
import com.proxiserve.model.Certification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtisanProfileView {
    private String id;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String profession;
    private String companyName;
    private String profilePictureUrl;
    private String biography;
    private List<String> skills;
    private List<String> serviceCategories;
    private String workingHoursWeekdays;
    private String workingHoursSaturday;
    private String workingHoursSunday;
    private List<String> workPhotoUrls;
    private List<Certification> certifications;
    
}
