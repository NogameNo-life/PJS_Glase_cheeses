package org.acme.projectjobschedule.app;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonImporter {
    protected String filepath;
    protected Map<String, Object> jsonMap;

    public JsonImporter(String filepath){
        this.filepath = filepath;
    }

public  Map<String, Object> getJsonMap(){
        return jsonMap;
}
    public String getFilepath(){
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

 public void printOperationHashMap(){
    System.out.println(jsonMap);
}

    protected void readOperationHashMap() {
        try {
            // Путь к JSON-файлу
            File jsonFile = new File(filepath);

            // Создаем ObjectMapper для работы с JSON
            ObjectMapper objectMapper = new ObjectMapper();

            // Читаем JSON-файл в JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonFile);

            // Создаем HashMap для хранения данных
            jsonMap = new HashMap<>();

            // Обрабатываем корневые поля
            jsonMap.put("ID", rootNode.path("ID").asText());
            jsonMap.put("StartDate", rootNode.path("StartDate").asText());
            jsonMap.put("EndDate", rootNode.path("EndDate").asText());
            jsonMap.put("Termination", rootNode.path("Termination").asText());

            // Обрабатываем массив ResourceList
            List<Map<String, Object>> resourceList = new ArrayList<>();
            for (JsonNode resourceNode : rootNode.path("ResourceList")) {
                Map<String, Object> resourceMap = new HashMap<>();
                resourceMap.put("RID", resourceNode.path("RID").asText());
                resourceMap.put("Capacity", resourceNode.path("Capacity").asInt());
                resourceMap.put("@type", resourceNode.path("@type").asText());
                resourceMap.put("Renewable", resourceNode.path("Renewable").asBoolean());
                resourceMap.put("RestrictionList", parseStringList(resourceNode.path("RestrictionList")));
                resourceList.add(resourceMap);
            }
            jsonMap.put("ResourceList", resourceList);

            // Обрабатываем массив JobList
            List<Map<String, Object>> jobList = new ArrayList<>();
            for (JsonNode jobNode : rootNode.path("JobList")) {
                Map<String, Object> jobMap = new HashMap<>();
                jobMap.put("JID", jobNode.path("JID").asText());
                jobMap.put("SuccessorList", parseStringList(jobNode.path("SuccessorList")));
                jobList.add(jobMap);
            }
            jsonMap.put("JobList", jobList);

            // Обрабатываем массив ProjectList
            List<Map<String, Object>> projectList = new ArrayList<>();
            for (JsonNode projectNode : rootNode.path("ProjectList")) {
                Map<String, Object> projectMap = new HashMap<>();
                projectMap.put("PID", projectNode.path("PID").asText());
                projectMap.put("Priority", projectNode.path("Priority").asInt());
                projectMap.put("VB", projectNode.path("VB").asInt());
                projectMap.put("GTIN", projectNode.path("GTIN").asText());
                projectMap.put("NP", projectNode.path("NP").asInt());

                // Обрабатываем массив ExecutionModeList
                List<Map<String, Object>> executionModeList = new ArrayList<>();
                for (JsonNode executionModeNode : projectNode.path("ExecutionModeList")) {
                    Map<String, Object> executionModeMap = new HashMap<>();
                    executionModeMap.put("JID", executionModeNode.path("JID").asText());
                    executionModeMap.put("Duration", executionModeNode.path("Duration").asInt());

                    // Обрабатываем массив ResourceRequirementList
                    List<Map<String, Object>> resourceRequirementList = new ArrayList<>();
                    for (JsonNode resourceRequirementNode : executionModeNode.path("ResourceRequirementList")) {
                        Map<String, Object> resourceRequirementMap = new HashMap<>();
                        resourceRequirementMap.put("RID", resourceRequirementNode.path("RID").asText());
                        resourceRequirementMap.put("Requirement", resourceRequirementNode.path("Requirement").asInt());
                        resourceRequirementList.add(resourceRequirementMap);
                    }
                    executionModeMap.put("ResourceRequirementList", resourceRequirementList);
                    executionModeList.add(executionModeMap);
                }
                projectMap.put("ExecutionModeList", executionModeList);
                projectList.add(projectMap);
            }
            jsonMap.put("ProjectList", projectList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Метод для преобразования JsonNode в List<String>
    private static List<String> parseStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        for (JsonNode item : node) {
            list.add(item.asText());
        }
        return list;
    }


}