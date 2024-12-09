package org.acme.projectjobschedule.app;

import org.acme.projectjobschedule.domain.ProjectJobSchedule;

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

    public DataModel getDatafromJson(){
        DataModel model = new DataModel();
        model= loadFromFile(filepath);
        return model;
    }

    public ProjectJobSchedule initProjectJobSheduleObject(){
        ProjectJobSchedule pjs = new ProjectJobSchedule();
        DataModel model = new DataModel();
        pjs.setJobs(model.getJobList());
        pjs.setProjects(model.getProjectList());
        pjs.setResources(model.getResourceList());

        return pjs;
    }
    private DataModel loadFromFile(String filePath) {
        DataModel dataModel = new DataModel();
        try (FileInputStream fis = new FileInputStream(filePath);
             JsonReader jsonReader = Json.createReader(fis)) {
            // Чтение JSON
            JsonObject jsonObject = jsonReader.readObject();

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
}