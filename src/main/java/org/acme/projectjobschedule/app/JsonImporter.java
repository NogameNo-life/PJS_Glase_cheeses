package org.acme.projectjobschedule.app;

import org.acme.projectjobschedule.domain.*;
import org.acme.projectjobschedule.domain.resource.Resource;
import org.acme.projectjobschedule.app.DataModel;

import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileInputStream;

public class JsonImporter {
    private String filepath;

    public String getFilepath(){
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public JsonImporter(String filePath) {
       this.filepath=filePath;
    }

    public JsonImporter(){

    }

    public ProjectJobSchedule initProjectJobSheduleObject(){
        ProjectJobSchedule pjs = new ProjectJobSchedule();
        DataModel model = new DataModel();
        model=loadFromFile(filepath);
        pjs.setJobs(model.getJobList());
        pjs.setProjects(model.getProjectList());
        pjs.setResources(model.getResourceList());
        printDataObject(model);
        return pjs;
    }
    private DataModel loadFromFile(String filePath) {
        DataModel dataModel = new DataModel();
        try (FileInputStream fis = new FileInputStream(filePath);
             JsonReader jsonReader = Json.createReader(fis)) {
            // Чтение JSON
            JsonObject jsonObject;
            jsonObject = jsonReader.readObject();

            // Инициализация объекта DataModel
            dataModel.setJobList(jsonObject);
            dataModel.setProjectList(jsonObject);
            dataModel.setResourceList(jsonObject);
            dataModel.setId(jsonObject.getString("ID"));
            dataModel.setStartDate(jsonObject.getString("StartDate"));
            dataModel.setEndDate(jsonObject.getString("EndDate"));
            dataModel.setTermination(jsonObject.getString("Termination"));

        } catch (IOException e) {
            e.printStackTrace();

        }
        return dataModel;
    }

    public void printDataObject(DataModel model){
        if (model != null) {
            System.out.println("ID: " + model.getId());
            System.out.println("StartDate: " + model.getStartDate());
            System.out.println("EndDate: " + model.getEndDate());
            System.out.println("Termination: " + model.getTermination());

            System.out.println("ResourceList:");
            for (Resource resource : model.getResourceList()) {
                System.out.println("  RID: " + resource.getId());
                System.out.println("  Capacity: " + resource.getCapacity());
                System.out.println("  Renewable: " + resource.isRenewable());
                //  System.out.println("  RestrictionList: " + resource.getRestrictionList());
            }

            System.out.println("JobList:");
            for (Job job : model.getJobList()) {
                System.out.println("  JID: " + job.getId());
                //System.out.println("  SuccessorList: " + job.getSuccessorList());
                System.out.println("  ExecutionModeList:");
                for (ExecutionMode executionMode : job.getExecutionModes()) {
                    System.out.println("    JID: " + executionMode.getId());
                    System.out.println("    Duration: " + executionMode.getDuration());

                    System.out.println("    ResourceRequirementList:");
                    for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirements()) {
                        System.out.println("      RID: " + resourceRequirement.getId());
                        System.out.println("      Requirement: " + resourceRequirement.getRequirement());
                    }
                }
            }

            System.out.println("ProjectList:");
            for (Project project : model.getProjectList()) {
                System.out.println("  PID: " + project.getId());
                System.out.println("  Priority: " + project.getPriority());
                System.out.println("  VB: " + project.getVb());
                System.out.println("  GTIN: " + project.getGtin());
                System.out.println("  NP: " + project.getNp());

            }

        } else {
            System.out.println("DataModel is not loaded.");
        }

    }
}