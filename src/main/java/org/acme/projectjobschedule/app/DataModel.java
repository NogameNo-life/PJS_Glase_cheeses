package org.acme.projectjobschedule.app;

import java.util.ArrayList;
import java.util.List;
import javax.json.JsonObject;
import javax.json.JsonArray;

import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.acme.projectjobschedule.domain.resource.GlobalResource;
import org.acme.projectjobschedule.domain.resource.LocalResource;
import org.acme.projectjobschedule.domain.resource.Resource;

public class DataModel {

    private String id;
    private String StartDate;
    private String EndDate;
    private String Termination;

    List<Project> projects = new ArrayList<Project>();
    List<Resource> resources = new ArrayList<Resource>();
    List<ExecutionMode> executionModeList = new ArrayList<ExecutionMode>();
    List<Job> jobs = new ArrayList<Job>();

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id=id;
    }

    public String getStartDate(){
        return StartDate;
    }

    public void setStartDate(String StartDate){
        this.StartDate=StartDate;
    }

    public String getEndDateDate(){
        return EndDate;
    }

    public void setEndDate(String EndDate){
        this.StartDate=EndDate;
    }

    public String getTermination(){
        return Termination;
    }

    public void setTermination(String Termination){
        this.Termination=Termination;
    }

    public List<Project> getProjectList(){
        return projects;
    }
    public void setProjectList(JsonObject jsonObject){
        JsonArray projectsArray = jsonObject.getJsonArray("ProjectList");
        for (JsonObject projectObject : projectsArray.getValuesAs(JsonObject.class)) {
            Project project = new Project();
            project.setId(projectObject.getString("PID"));
            project.setPriority(projectObject.getInt("Priority"));
            project.setVb(projectObject.getInt("VB"));
            project.setGtin(projectObject.getString("GTIN"));
            project.setNp(projectObject.getInt("NP"));
            this.projects.add(project);
    }

    }

    public List<Job> getJobList(){
        return jobs;
    }
    public void setJobList(JsonObject jsonObject){
        JsonArray jobArray = jsonObject.getJsonArray("JobList");
        for (JsonObject jobObject : jobArray.getValuesAs(JsonObject.class)) {
            Job job = new Job();
            job.setId(jobObject.getString("JID"));
            //job.setSuccessorJobs(jobObject.getJsonArray("SuccessorList").getValuesAs(Job.class));
            setExecutionModeList(jsonObject);
            job.setExecutionModes(getexecutionModeList());

            this.jobs.add(job);
        }
    }

    public List<Resource> getResourceList(){
        return resources;
    }

    public void setResourceList(JsonObject jsonObject){
        JsonArray resourcesArray = jsonObject.getJsonArray("ResourceList");
        for (JsonObject resourceObject : resourcesArray.getValuesAs(JsonObject.class)) {
            if(resourceObject.getString("@type").equals("local")){
                LocalResource localResource = new LocalResource();
                localResource.setId(resourceObject.getString("RID"));
                localResource.setCapacity(resourceObject.getInt("Capacity"));
                localResource.setRenewable(resourceObject.getBoolean("Renewable"));
               // resource.setRestrictionList(resourceObject.getJsonArray("RestrictionList").getValuesAs(Object.class));
                this.resources.add(localResource);
            }
            else{
            GlobalResource globalResource = new GlobalResource();
            globalResource.setId(resourceObject.getString("RID"));
            globalResource.setCapacity(resourceObject.getInt("Capacity"));
            //resource.setRestrictionList(resourceObject.getJsonArray("RestrictionList").getValuesAs(Object.class));
            this.resources.add(globalResource);
        }
    }
    }

    public List<ExecutionMode> getexecutionModeList(){
        return executionModeList;
    }

    private void setExecutionModeList(JsonObject jsonObject) {
        JsonArray executionModeArray = jsonObject.getJsonArray("ExecutionModeList");
        for (JsonObject executionModeObject : executionModeArray.getValuesAs(JsonObject.class)) {
            ExecutionMode executionMode = new ExecutionMode();
            executionMode.setId(executionModeObject.getString("JID"));
            executionMode.setDuration(executionModeObject.getInt("Duration"));

            List<ResourceRequirement> resourceRequirementList = new ArrayList<>();
            JsonArray resourceRequirementArray = executionModeObject.getJsonArray("ResourceRequirementList");
            for (JsonObject resourceRequirementObject : resourceRequirementArray.getValuesAs(JsonObject.class)) {
                ResourceRequirement resourceRequirement = new ResourceRequirement();
                resourceRequirement.setId(resourceRequirementObject.getString("RID"));
                resourceRequirement.setRequirement(resourceRequirementObject.getInt("Requirement"));
                resourceRequirementList.add(resourceRequirement);
            }

            executionMode.setResourceRequirements(resourceRequirementList);
            this.executionModeList.add(executionMode);

        }


    }
}
