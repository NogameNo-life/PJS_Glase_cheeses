package org.acme.projectjobschedule.app;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.acme.projectjobschedule.domain.*;
import org.acme.projectjobschedule.domain.resource.GlobalResource;
import org.acme.projectjobschedule.domain.resource.LocalResource;
import org.acme.projectjobschedule.domain.resource.Resource;
import org.acme.projectjobschedule.domain.ExecutionMode;

import static org.acme.projectjobschedule.domain.JobType.SOURCE;
import static org.acme.projectjobschedule.domain.JobType.STANDARD;

public class DataModel extends JsonImporter {

    private String ID;
    private String StartDate;
    private String EndDate;
    private String Termination;

    private List<Project> projects;
    private List<Resource> resources;
    private List<List<ExecutionMode>> executionModeList;
    private List<Job> jobs;
    private List<String> successorJobsList;
    private List<List<ResourceRequirement>> resourceRequirementList;
    private List<String> RestrictionList;
    private List<Resource> ResourceList;

    public DataModel(String filepath) {
        super(filepath);
    }

    public List<Project> getProjectList() {
        return projects;
    }

    public List<Job> getJobList() {
        return jobs;
    }

    public List<Resource> getResourceList() {
        return resources;
    }

    public List<List<ExecutionMode>> getExecutionModeList() {
        return executionModeList;
    }

    public void initModelObject(){
        initBase();
        initProjectList();
        initJobList();
    }

    public ProjectJobSchedule generateProjectJobSchedule(){
        ProjectJobSchedule projectJobSchedule = new ProjectJobSchedule();

        // Projects
        List<Project> projects = initProject();
        List<Resource> resources1 = initResource();
        projectJobSchedule.setProjects(projects);
        projectJobSchedule.setResources(resources1);
        return projectJobSchedule;

    }

 private   List<Project> initProject() {
     List<Project> projects1 = new ArrayList<>();
     int id = 0;
     List<Map<String, Object>> jsonProjects = (List<Map<String, Object>>) jsonMap.get("ProjectList");
     for (Map<String, Object> jsonProject : jsonProjects) {
         Project project = new Project();
         project.setId(String.valueOf(id++));
         String PID = (String) jsonProject.get("PID");
         project.setPID(PID);
         int priority = (int) jsonProject.get("Priority");
         project.setPriority(priority);
         int vb = (int) jsonProject.get("VB");
         project.setVb(vb);
         String gtin = (String) jsonProject.get("GTIN");
         project.setGtin(gtin);
         int np = (int) jsonProject.get("NP");
         project.setNp(np);
         projects1.add(project);
     }
     return projects1;
 }

 private List<Resource> initResource(){
        List<Resource> resources1 = new ArrayList<>();
        int id=0;
     List<Map<String, Object>> jsonResourceList = (List<Map<String, Object>>) jsonMap.get("ResourceList");
     for (Map<String, Object> jsonResource : jsonResourceList) {

         if (jsonResource.get("@type").equals("global")) {

             GlobalResource globalResource = new GlobalResource();
             globalResource.setId(String.valueOf(id++));
             String rid = (String) jsonResource.get("RID");
             globalResource.setRID(rid);
             int capacity = (int) jsonResource.get("Capacity");
             globalResource.setCapacity(capacity);
             List<String> restrictionList = (List<String>) jsonResource.get("RestrictionList");
             if (RestrictionList == null) {
                 this.RestrictionList = restrictionList;
             } else {
                 this.RestrictionList = Collections.emptyList();
             }
             resources1.add(globalResource);
         } else if (jsonResource.get("@type").equals("local")) {
             LocalResource localResource = new LocalResource();
             localResource.setId(String.valueOf(id++));
             String rid = (String) jsonResource.get("RID");
             localResource.setRID(rid);
             int capacity = (int) jsonResource.get("Capacity");
             localResource.setCapacity(capacity);
             boolean renewable = (boolean) jsonResource.get("Renewable");
             localResource.setRenewable(renewable);
             List<String> restrictionList = (List<String>) jsonResource.get("RestrictionList");
             if (RestrictionList == null) {
                 this.RestrictionList = restrictionList;
             } else {
                 this.RestrictionList = Collections.emptyList();
             }
             resources1.add(localResource);
         }

     }
     return resources1;
 }

 private void initJob(int jobsSize, List<Project> projects, List<Resource> resources){
     List<Map<String, Object>> jsonJobs = (List<Map<String, Object>>) jsonMap.get("JobList");
     List<Job> jobs = new ArrayList<>(jobsSize);
     int jobsCountPerProject = jobsSize / 2;
     int countJob = 0;
     for(Project project : projects){
         List<Job> jobsPerProject = new ArrayList<>(jobsCountPerProject);
         jobsPerProject.add(new Job(String.valueOf(countJob++), project, SOURCE));
         for (int i = 0; i < jobsCountPerProject - 2; i++) {
             jobsPerProject.add(new Job(String.valueOf(countJob++), project, STANDARD));
         }
     }
 }

    private void initProjectList() {
        List<Map<String, Object>> jsonProjects = (List<Map<String, Object>>) jsonMap.get("ProjectList");
        this.projects = new ArrayList<>();
        this.executionModeList = new ArrayList<>();
        this.resourceRequirementList = new ArrayList<>();
        List<ExecutionMode> exmList = new ArrayList<>();
        List<ResourceRequirement> resReqList = new ArrayList<>();
        for (Map<String, Object> jsonProject : jsonProjects) {
            Project project = new Project();
            String id = (String) jsonProject.get("PID");
            project.setId(id);
            int priority = (int) jsonProject.get("Priority");
            project.setPriority(priority);
            int vb = (int) jsonProject.get("VB");
            project.setVb(vb);
            String gtin = (String) jsonProject.get("GTIN");
            project.setGtin(gtin);
            int np = (int) jsonProject.get("NP");
            project.setNp(np);

                List<Map<String, Object>> jsonExecutionModeList = (List<Map<String, Object>>) jsonProject.get("ExecutionModeList");
                for (Map<String, Object> jsonExecutionMode : jsonExecutionModeList) {

                    ExecutionMode executionMode = new ExecutionMode();
                    String jid = (String) jsonExecutionMode.get("JID");
                    executionMode.setId(jid);
                    int duration = (int) jsonExecutionMode.get("Duration");
                    executionMode.setDuration(duration);

                    List<Map<String, Object>> jsonResourceRequirementList = (List<Map<String, Object>>) jsonExecutionMode.get("ResourceRequirementList");

                    for (Map<String, Object> jsonResourceRequirement : jsonResourceRequirementList) {

                        ResourceRequirement resourceRequirement = new ResourceRequirement();
                        String rid = (String) jsonResourceRequirement.get("RID");
                        resourceRequirement.setId(rid);
                        int requirement = (int) jsonResourceRequirement.get("Requirement");
                        resourceRequirement.setRequirement(requirement);
                        resReqList.add(resourceRequirement);
                    }
                    exmList.add(executionMode);
                }
            this.projects.add(project);
            this.resourceRequirementList.add(resReqList);
            this.executionModeList.add(exmList);
            }
        if (this.projects == null) {
            this.projects = Collections.emptyList();
        }
        if (this.executionModeList == null) {
            this.resourceRequirementList = Collections.emptyList();
        }
        if (this.resourceRequirementList == null) {
            this.resourceRequirementList = Collections.emptyList();
        }
        }

    private void initJobList() {
        List<Map<String, Object>> jsonJobs = (List<Map<String, Object>>) jsonMap.get("JobList");
        this.jobs = new ArrayList<>();
        for (Map<String, Object> jsonJob : jsonJobs) {
            Job job = new Job();
            String id = (String) jsonJob.get("JID");
            job.setId(id);
            List<String> successorList = (List<String>) jsonJob.get("SuccessorList");
            if (successorList == null) {
                this.successorJobsList = successorList;
            } else {
                this.successorJobsList = Collections.emptyList();
            }
            this.jobs.add(job);
        }
        if (this.jobs == null) {
            this.jobs = Collections.emptyList();
        }
    }

    private void initBase() {
        this.ID = (String) jsonMap.get("ID");
        this.StartDate = (String) jsonMap.get("StartDate");
        this.EndDate = (String) jsonMap.get("EndDate");
        this.Termination = (String) jsonMap.get("Termination");
        this.ResourceList = new ArrayList<>();
        List<Map<String, Object>> jsonResourceList = (List<Map<String, Object>>) jsonMap.get("ResourceList");
        for (Map<String, Object> jsonResource : jsonResourceList) {

            if (jsonResource.get("@type").equals("global")) {

                GlobalResource globalResource = new GlobalResource();
                String rid = (String) jsonResource.get("RID");
                globalResource.setId(rid);
                int capacity = (int) jsonResource.get("Capacity");
                List<String> restrictionList = (List<String>) jsonResource.get("RestrictionList");
                if (RestrictionList == null) {
                    this.RestrictionList = restrictionList;
                } else {
                    this.RestrictionList = Collections.emptyList();
                }
            } else if (jsonResource.get("@type").equals("local")) {
                LocalResource localResource = new LocalResource();
                String rid = (String) jsonResource.get("RID");
                localResource.setId(rid);
                int capacity = (int) jsonResource.get("Capacity");
                boolean renewable = (boolean) jsonResource.get("Renewable");
                List<String> restrictionList = (List<String>) jsonResource.get("RestrictionList");
                if (RestrictionList == null) {
                    this.RestrictionList = restrictionList;
                } else {
                    this.RestrictionList = Collections.emptyList();
                }
            }
        }
        if(this.ResourceList==null){
            this.ResourceList= Collections.emptyList();
        }
    }
}



