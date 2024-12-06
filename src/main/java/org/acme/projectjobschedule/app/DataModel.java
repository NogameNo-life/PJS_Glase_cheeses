package org.acme.projectjobschedule.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.resource.Resource;

public class DataModel {
    @JsonProperty("ProjectList")
    private List<Project> projectList;

    @JsonProperty("ResourceList")
    private List<Resource> resourceList;

    @JsonProperty("JobList")
    private List<Job> jobList;

    // Getters и Setters
    public List<Project> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }

    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }
}
