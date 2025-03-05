package com.example.jobportal.services;

import com.example.jobportal.entity.*;
import com.example.jobportal.repository.JobPostActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobPostActivityService {

    private final JobPostActivityRepository jobPostActivityRepository;

    @Autowired
    public JobPostActivityService(JobPostActivityRepository jobPostActivityRepository) {
        this.jobPostActivityRepository = jobPostActivityRepository;
    }

    public JobPostActivity addNew(JobPostActivity jobPostActivity) {

        return jobPostActivityRepository.save(jobPostActivity);
    }

    public List<RecruiterJobsDto> getRecruiterJobs(int recruiter) {

        List<IRecruiterJobs> recruiterJobsDtos= jobPostActivityRepository.getRecruiterJobs(recruiter); //we are writing I bcs we are using interface
        List<RecruiterJobsDto> recruiterJobsDtoList = new ArrayList<>();

        for (IRecruiterJobs rec : recruiterJobsDtos) { //construct DTO based on info, converting to DTO from database. x rec ":" y means x is the object and y is the DTO and : means is converted to
            JobLocation loc = new JobLocation(
                    rec.getLocationId(),
                    rec.getCity(),
                    rec.getState(),
                    rec.getCountry()
            );
            JobCompany comp= new JobCompany(
                    rec.getCompanyId(),
                    rec.getName(),
                    ""
            );
            recruiterJobsDtoList.add(new RecruiterJobsDto(
                    rec.getTotalCandidates(),
                    rec.getJob_post_id(),
                    rec.getJob_title(),
                    loc,
                    comp
                    )
            );
        }
        return recruiterJobsDtoList;
    }

    public JobPostActivity getOne(int id) {

        return jobPostActivityRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not Found"));

    }
}
