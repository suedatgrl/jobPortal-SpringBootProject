package com.example.jobportal.controller;

import com.example.jobportal.entity.JobSeekerProfile;
import com.example.jobportal.entity.Skills;
import com.example.jobportal.entity.Users;
import com.example.jobportal.repository.JobSeekerProfileRepository;
import com.example.jobportal.repository.UsersRepository;
import com.example.jobportal.services.JobSeekerProfileService;
import com.example.jobportal.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequestMapping("/job-seeker-profile")
@Controller
public class JobSeekerProfileController {

    private final UsersRepository usersRepository;
    private JobSeekerProfileService jobSeekerProfileService;



    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService, JobSeekerProfileRepository jobSeekerProfileRepository, UsersRepository usersRepository) {
        this.jobSeekerProfileService = jobSeekerProfileService;

        this.usersRepository = usersRepository;
    }

    @GetMapping("/")
    public String jobSeekerProfile(Model model) {
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Skills> skills = new ArrayList<>();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(
                    () -> new UsernameNotFoundException("User not found"));
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());

            if(seekerProfile.isPresent()) {

                jobSeekerProfile = seekerProfile.get();
                if(jobSeekerProfile.getSkills().isEmpty()){
                    skills.add(new Skills());
                    jobSeekerProfile.setSkills(skills);

                }
            }

            model.addAttribute("skills",skills);
            model.addAttribute("profile",jobSeekerProfile);
        }
        return "job-seeker-profile";
    }

    @PostMapping("/addNew")
    public  String addNew(JobSeekerProfile jobSeekerProfile,
                          @RequestParam("image") MultipartFile image,
                          @RequestParam("pdf") MultipartFile pdf,
                          Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!(authentication instanceof AnonymousAuthenticationToken)) {

            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(
                    () -> new UsernameNotFoundException("User not found"));

            jobSeekerProfile.setUserId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());
        }
        List<Skills> skillsList=  new ArrayList<>();

        model.addAttribute("profile",jobSeekerProfile);
        model.addAttribute("skills",skillsList);

        for(Skills skills : jobSeekerProfile.getSkills() ){
            skills.setJobSeekerProfile(jobSeekerProfile);
        }

        String imageName="";
        String resumeName="";

        if(!Objects.equals(image.getOriginalFilename(),"")){
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            jobSeekerProfile.setProfilePhoto(imageName);
        }

        if(!Objects.equals(pdf.getOriginalFilename(),"")){
            resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));
            jobSeekerProfile.setResume(resumeName);
        }


        JobSeekerProfile seekerProfile =jobSeekerProfileService.addNew(jobSeekerProfile);

        //save the file, file system
        try{
            String uploadDir = "photos/candidate/" + jobSeekerProfile.getUserAccountId(); //photos represent the folder name where the image will be saved.
            // "/photos/candidate does not work. "photos/candidate" works." directory error.
            if(!Objects.equals(image.getOriginalFilename(),"")){
                FileUploadUtil.saveFile(uploadDir, imageName, image);
            }
            if(!Objects.equals(pdf.getOriginalFilename(),"")){
                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);
            }
        }
        catch (IOException ex){
            throw new RuntimeException(ex);
        }
        return "redirect:/dashboard/";
    }
}

