package org.acme.projectjobschedule.data;

import java.io.File;
import java.io.IOException;

import java.util.*;

import org.acme.projectjobschedule.domain.*;
import org.acme.projectjobschedule.domain.resource.Resource;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import ai.timefold.solver.core.api.score.constraint.Indictment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonExporter {

    private final Map<String, Object> jsonMap;
    private HardMediumSoftScore totalScore;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public JsonExporter(HardMediumSoftScore score, String ID,  List<Project> projects, List<Resource> resources,
                        List<ResourceRequirement> requirementList, List<Allocation> allocations, ScoreExplanation<ProjectJobSchedule, HardMediumSoftScore> scoreExplanation) {

        List<String> projectsPid= getProjectsPid(projects);
        List<String> resourcesRid = getResourcesRid(resources);
        List<JsonAllocationList> jallocationList = getJallocationList(allocations, requirementList);
        List<ResultAnalyze> Indicments = getIndicmentList(scoreExplanation);
        this.jsonMap = new LinkedHashMap<>();

        jsonMap.put("ID", ID);
        jsonMap.put("HardConstraintsPenalty", score.hardScore());
        jsonMap.put("MediumConstraintsPenalty1", score.mediumScore());
        jsonMap.put("SoftConstraintsPenalty", score.softScore());
        jsonMap.put("Projects", projectsPid);
        jsonMap.put("Resources", resourcesRid);
        jsonMap.put("AllocationList", jallocationList);
        jsonMap.put("IndicmentsList", Indicments);
        jsonMap.put("TotalMatch", totalScore);
    }

    public void convertToJsonFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);

        try {
            // Создаем все необходимые родительские директории
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, jsonMap);
            System.out.println("JSON файл успешно создан: " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при создании JSON файла: " + e.getMessage());
        }
    }

    private List<String> getResourcesRid(List<Resource> resources){
        List<String> resourcesRid = new ArrayList<>();
        for (Resource resource : resources) {
            resourcesRid.add(resource.getRID());
        }
         return resourcesRid;
    }
    private List<String> getProjectsPid(List<Project> projects){
        List<String> projectsPid = new ArrayList<>();
        for (Project project : projects) {
            projectsPid.add(String.valueOf(project.getPID()));
        }
        return projectsPid;
    }

    private List<JsonAllocationList> getJallocationList(List<Allocation> allocations,
                                                        List<ResourceRequirement> requirementList ){
        List<JsonAllocationList> jallocationList = new ArrayList<>();
        for (Allocation allocation : allocations) {
            if (allocation.getJob().getJID().equals("SOURCE") || allocation.getJob().getJID().equals("SINK")) {
                continue;
            }
            JsonAllocationList jallocation = new JsonAllocationList(allocation.getId(), allocation.getProject().getPID(),
                    allocation.getExecutionMode().getJID(), String.valueOf(allocation.getStartDateTime().format(formatter)),
                    String.valueOf(allocation.getEndDateTime().format(formatter)),  allocation.getExecutionMode().getDuration(),
                    requirementList, allocation.getExecutionMode(), allocation.getPredecessorAllocations());
            jallocationList.add(jallocation);
        }
        return jallocationList;
    }

    private List<ResultAnalyze> getIndicmentList(ScoreExplanation<ProjectJobSchedule, HardMediumSoftScore> scoreExplanation) {

        Map<Object, Indictment<HardMediumSoftScore>> indictmentMap = scoreExplanation.getIndictmentMap();
        List<ResultAnalyze> indicmentsList = new ArrayList<>();
        this.totalScore = HardMediumSoftScore.ZERO;

        for (var entry : indictmentMap.entrySet()) {
            Object allocation = entry.getKey(); // Объект, например, Allocation-8
            Indictment<HardMediumSoftScore> indictment = entry.getValue(); // Информация об обвинениях
            int matchCount = indictment.getConstraintMatchSet().size();

            HardMediumSoftScore indictmentScore = indictment.getScore();
            totalScore = totalScore.add(indictmentScore);
            // Проходим по каждому нарушенному ограничению и штрафу
            for (ConstraintMatch<HardMediumSoftScore> constraintMatch : indictment.getConstraintMatchSet()) {

                indicmentsList.add(new ResultAnalyze(String.valueOf(allocation),String.valueOf(matchCount),constraintMatch.getConstraintRef().constraintName(),
                        String.valueOf(constraintMatch.getScore())));
            }
        }
        indicmentsList.sort(Comparator.comparing(ResultAnalyze::allocationNum));
        return indicmentsList;
    }

    @JsonPropertyOrder({ "ID", "PID", "JID", "StartDate", "EndDate", "Duration", "PredAllocationList" })
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)

    static class JsonAllocationList{

        private final String id;
        private final String pid;
        private final String jid;
        private final String startDate;
        private final String endDate;
        private final int duration;
        private final List<String> resourceRequirementList;
        private final List<String> predAllocationList;

        // Геттеры
        @JsonProperty("ID")
        public String getID() {
            return id;
        }

        @JsonProperty("PID")
        public String getPID() {
            return pid;
        }

        @JsonProperty("JID")
        public String getJID() {
            return jid;
        }

        @JsonProperty("StartDate")
        public String getStartDate() {
            return  startDate;
        }

        @JsonProperty("EndDate")
        public String getEndDate() {
            return endDate;
        }

        @JsonProperty("Duration")
        public int getDuration() {
            return duration;
        }

        @JsonProperty("ResourceRequirementList")
        public List<String> getResourceRequirementList() {
            return resourceRequirementList;
        }

        @JsonProperty("PredAllocationList")
        public List<String> getPredAllocationList() {
            return predAllocationList;
        }

        public JsonAllocationList(String id, String pid, String jid, String startDate, String endDate,
                              int duration, List<ResourceRequirement> requirementsList,
                              ExecutionMode executionMode,
                              List<Allocation> predAllocationlist){
            int numericId = Integer.parseInt(id) + 1;
            this.id = "Allocation" + numericId;
            this.pid = pid;
            this.jid = jid;
            this.startDate = startDate;
            this.endDate = endDate;
            this.duration = duration;
            this.resourceRequirementList = new ArrayList<>();
            this.predAllocationList = new ArrayList<>();
            for(ResourceRequirement requirement : requirementsList){
               if(requirement.getExecutionMode().equals(executionMode)){
                   resourceRequirementList.add(requirement.getRID());
               }
            }
            for(Allocation predAllocation : predAllocationlist){
                int numericAllocId = Integer.parseInt(predAllocation.getId()) + 1;
                predAllocationList.add("Allocation" + numericAllocId );
}
        }
    }

    @JsonPropertyOrder({"Allocation", "MatchCount", "Constraint", "Penalty"})
        @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    record ResultAnalyze(String allocationNum, String matchCount, String constraintName, String penaltyScore) {

            @Override
            @JsonProperty("Allocation")
            public String allocationNum() {
                return allocationNum;
            }

            @Override
            @JsonProperty("MatchCount")
            public String matchCount() {
                return matchCount;
            }

        @Override
        @JsonProperty("Constraint")
            public String constraintName() {
                return constraintName;
            }

        @Override
        @JsonProperty("Penalty")
            public String penaltyScore() {
                return penaltyScore;
            }

    }
}