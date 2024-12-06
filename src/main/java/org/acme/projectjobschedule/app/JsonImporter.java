package org.acme.projectjobschedule.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.acme.projectjobschedule.domain.Job;

import org.acme.projectjobschedule.app.DataModel;

public class JsonImporter extends ProjectJobSchedule {

    public JsonImporter(String filePath) {
        loadFromFile(filePath);
    }

    private void loadFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Десериализация JSON
            DataModel dataModel = mapper.readValue(new File(filePath), DataModel.class);

            // Установка данных через сеттеры
            setProjects(dataModel.getProjectList());
            setResources(dataModel.getResourceList());
            setJobs(dataModel.getJobList());

            // Преобразование ExecutionMode и ResourceRequirement
            setExecutionModes(extractExecutionModes(dataModel.getJobList()));
            setResourceRequirements(extractResourceRequirements(dataModel.getJobList()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<ExecutionMode> extractExecutionModes(List<Job> jobs) {
        // Извлечение ExecutionMode из JobList
        return jobs.stream()
                .flatMap(job -> job.getExecutionModes().stream())
                .toList();
    }

    private List<ResourceRequirement> extractResourceRequirements(List<Job> jobs) {
        // Извлечение ResourceRequirement из ExecutionMode
        return jobs.stream()
                .flatMap(job -> job.getExecutionModes().stream())
                .flatMap(mode -> mode.getResourceRequirements().stream())
                .toList();
    }
}

