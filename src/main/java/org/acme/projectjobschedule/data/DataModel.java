package org.acme.projectjobschedule.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.acme.projectjobschedule.domain.*;
import static org.acme.projectjobschedule.domain.JobType.*;
import org.acme.projectjobschedule.domain.resource.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataModel extends JsonImporter {

    // Формат даты и времени
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String id;
    private int ts;
    private int us;
    private LocalDateTime startDate;

    private List <ExecutionMode> executionModes_fromJson;
    private Map<String, List<String>> successorJobMap;
    private List<ResourceRequirement> resourceRequirementList;
    private List<String> RestrictionList;
    private final ObjectMapper objectMapper;

    public DataModel(String filepath) {
        super(filepath);
        this.objectMapper = new ObjectMapper();
    }

    private List<ResourceRequirement> getResourceRequirements(){
        return resourceRequirementList;
    }

    public int getTS(){ return ts;}
    public int getUS(){ return us;}
    public String getID(){ return id;}
    public LocalDateTime getStartDate(){ return startDate;}

    public ProjectJobSchedule generateProjectJobSchedule(){

        ProjectJobSchedule projectJobSchedule = new ProjectJobSchedule();
        initBase();
        // Projects
        List<Project> projects = generateProjects();
        // Resources
        List<Resource> resources = generateResources();
        // Jobs
        List<Job> jobsFromJson  = readJobList();
        List<Job> jobs = generateJobs(jobsFromJson, projects);
        // Generate and add ExecutionModes
        List<ExecutionMode> executionModeList = generateExecutionModes(jobs, projects.size());
        // ResourceRequirements
        List<ResourceRequirement> resourceRequirements = getResourceRequirements();
        // Allocations
        List<Allocation> allocations = generateAllocations(jobs);

        projectJobSchedule.setProjects(projects);
        projectJobSchedule.setResources(resources);
        projectJobSchedule.setJobs(jobs);
        projectJobSchedule.setExecutionModes(executionModeList);
        projectJobSchedule.setResourceRequirements(resourceRequirements);
        projectJobSchedule.setAllocations(allocations);
        return projectJobSchedule;
    }

    private  List<Project> generateProjects() {
        List<Project> projects = new ArrayList<>();
        this.executionModes_fromJson = new ArrayList<>();
        this.resourceRequirementList = new ArrayList<>();
        int project_id = 0;

        List<Map<String, Object>> jsonProjects = objectMapper.convertValue(jsonMap.get("ProjectList"),
                new TypeReference<>() {});
        for (Map<String, Object> jsonProject : jsonProjects) {
            Project project = new Project();
            project.setId(String.valueOf(project_id));
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

            projects.add(project);

            List<Map<String, Object>> jsonExecutionModeList = objectMapper.convertValue(jsonProject.get("ExecutionModeList"),
                    new TypeReference<>() {});
            for (Map<String, Object> jsonExecutionMode : jsonExecutionModeList) {
                ExecutionMode executionMode = new ExecutionMode();
                executionMode.setId(String.valueOf(project_id));
                executionMode.setJID((String) jsonExecutionMode.get("JID"));
                executionMode.setId(String.valueOf(project_id));
                executionMode.setDuration((int) jsonExecutionMode.get("Duration"));
                List<ResourceRequirement>resourceRequirementsList1 = new ArrayList<>();
                List<Map<String, Object>> jsonResourceRequirementList = objectMapper.convertValue(
                        jsonExecutionMode.get("ResourceRequirementList"),
                        new TypeReference<>() {});
                for (Map<String, Object> jsonResourceRequirement : jsonResourceRequirementList) {
                    ResourceRequirement resourceRequirement = new ResourceRequirement();
                    resourceRequirement.setId(String.valueOf(project_id));
                    resourceRequirement.setRID((String) jsonResourceRequirement.get("RID"));
                    resourceRequirement.setRequirement((int) jsonResourceRequirement.get("Requirement"));
                    resourceRequirementsList1.add(resourceRequirement);
                    resourceRequirementList.add(resourceRequirement);
                }
                executionMode.setResourceRequirements(resourceRequirementsList1);
                this.executionModes_fromJson.add(executionMode);
            }
            ++project_id;
        }
        return projects;
    }

    private List<Resource> generateResources(){
        List<Resource> resources = new ArrayList<>();
        int id=0;
        List<Map<String, Object>> jsonResourceList = objectMapper.convertValue(jsonMap.get("ResourceList"),
                new TypeReference<>() {});
        for (Map<String, Object> jsonResource : jsonResourceList) {

            if (jsonResource.get("@type").equals("global")) {

                GlobalResource globalResource = new GlobalResource();
                globalResource.setId(String.valueOf(id));
                globalResource.setRID((String) jsonResource.get("RID"));
                globalResource.setCapacity((int) jsonResource.get("Capacity"));

                List<String> restrictionList = objectMapper.convertValue(jsonResource.get("RestrictionList"),
                        new TypeReference<>() {});
                if (RestrictionList == null) {
                    this.RestrictionList = restrictionList;
                } else {
                    this.RestrictionList = Collections.emptyList();
                }
                resources.add(globalResource);

            } else if (jsonResource.get("@type").equals("local")) {
                LocalResource localResource = new LocalResource();
                localResource.setId(String.valueOf(id));
                localResource.setRID((String) jsonResource.get("RID"));
                localResource.setCapacity((int) jsonResource.get("Capacity"));
                localResource.setRenewable((boolean) jsonResource.get("Renewable"));
                List<String> restrictionList = objectMapper.convertValue(jsonResource.get("RestrictionList"),
                        new TypeReference<>() {});
                if (RestrictionList == null) {
                    this.RestrictionList = restrictionList;
                } else {
                    this.RestrictionList = Collections.emptyList();
                }
                resources.add(localResource);
            }
            ++id;
        }
        for(Resource resource : resources) {
            for (ResourceRequirement requirement : resourceRequirementList) {
                if (resource.getRID().equals(requirement.getRID())) {
                    requirement.setResource(resource);
                }
            }
        }
        return resources;
    }

    private List<Job> readJobList() {
        List<Map<String, Object>> jsonJobs = objectMapper.convertValue(jsonMap.get("JobList"),
                new TypeReference<>() {});
        int id =0;
        List<Job> jobs = new ArrayList<>();
        successorJobMap = new HashMap<>();
        for (Map<String, Object> jsonJob : jsonJobs) {
            Job job = new Job();
            job.setId(String.valueOf(id++));
            String jid = (String) jsonJob.get("JID");
            job.setJID(jid);
            if(job.getJID().equals("SOURCE") || job.getJID().equals("SINK")){
                job.setJobType(JobType.valueOf(job.getJID()));
            }
            else {
                job.setJobType(STANDARD);
            }
            List<String> successorList = objectMapper.convertValue( jsonJob.get("SuccessorList"),
                    new TypeReference<>() {});
            successorJobMap.put(job.getJID(), successorList);
            jobs.add(job);
        }
        return jobs;
    }

    private List<Job> generateJobs(List<Job> jobsFromJson, List<Project> projects) {
        int jobsSize = jobsFromJson.size();
        List<Job> jobs = new ArrayList<>(jobsSize * projects.size());
        int jobsCountPerProject;
        jobsCountPerProject = jobsSize;
        int countJob = 0;
        for (Project project : projects) {
            // Generate the job list
            List<Job> jobsPerProject = new ArrayList<>(jobsCountPerProject);
            for (Job json_job : jobsFromJson) {
                jobsPerProject.add(new Job(String.valueOf(countJob++), project, json_job.getJobType(), json_job.getJID()));
            }
            // Add all jobs of the given project
            jobs.addAll(jobsPerProject);
        }
        for(Map.Entry<String, List<String>> entry : successorJobMap.entrySet()){
            String key = entry.getKey();
            List<String> strList = entry.getValue();
            for(Job job : jobs){
                List<Job> successorJobs = new ArrayList<>();
                if(job.getJID().equals(key)){
                    if(strList.isEmpty()){
                        successorJobs = Collections.emptyList();
                        job.setSuccessorJobs(successorJobs);
                    }
                    else{
                        for(String str : strList){
                            for(Job job1 : jobs){
                                if(job1.equals(job)) continue;
                                if(job1.getJID().equals(str) && job.getProject().getPID().equals(job1.getProject().getPID())) {
                                    successorJobs.add(job1);
                                }
                            }
                        }
                        job.setSuccessorJobs(successorJobs);
                    }
                }

            }
        }
        return jobs;
    }

    private List<ExecutionMode> generateExecutionModes(List<Job> jobs, int projectsSize){
        List<ExecutionMode> executionModeList = new ArrayList<>();
        int index=0;
            for (int i = 0; i < projectsSize; ++i) {
                String id = String.valueOf(i);
                List<ExecutionMode> executionModeSink = new ArrayList<>();
                List<ExecutionMode> executionModeSource = new ArrayList<>();
                List<ExecutionMode> executionModeStandard = new ArrayList<>();

                for (ExecutionMode executionMode : executionModes_fromJson) {

                    if (executionMode.getJID().equals("SINK") && executionMode.getId().equals(id)) {
                            executionModeSink.add(executionMode);
                            executionModeList.add(executionMode);
                    }
                    else if(executionMode.getJID().equals("SOURCE") && executionMode.getId().equals(id)){
                        executionModeSource.add(executionMode);
                        executionModeList.add(executionMode);
                    }
                    else if(executionMode.getJID()!=null && executionMode.getId().equals(id)) {
                        executionModeStandard.add(executionMode);
                        executionModeList.add(executionMode);
                    }
                }
                for(Job job: jobs){
                    if(job.getJobType().equals(SINK) && job.getProject().getId().equals(id)){
                        for(ExecutionMode executionMode : executionModeSink){
                            executionMode.setJob(job);
                        }
                        job.setExecutionModes(executionModeSink);

                    }
                    else if(job.getJobType().equals(SOURCE) && job.getProject().getId().equals(id)){
                        for(ExecutionMode executionMode : executionModeSource){
                            executionMode.setJob(job);
                        }
                        job.setExecutionModes(executionModeSource);
                    }
                    else if( job.getProject().getId().equals(id)){
                        job.setJobType(STANDARD);
                        for(ExecutionMode executionMode : executionModeStandard){
                            executionMode.setJob(job);
                        }
                        job.setExecutionModes(executionModeStandard);
                    }
                }
        }
            for(ExecutionMode executionMode : executionModeList){
                executionMode.setId(String.valueOf(++index));
                for(ResourceRequirement requirement : executionMode.getResourceRequirements()){
                    requirement.setExecutionMode(executionMode);
                }
            }
            return executionModeList;
    }

    private List<Allocation> generateAllocations(List<Job> jobs) {
        List<Allocation> allocations = new ArrayList<>(jobs.size());
        int doneDate = 0;
        for (int i = 0; i < jobs.size(); i++) {
            allocations.add(new Allocation(String.valueOf(i), jobs.get(i)));
        }
        // Set source, sink, predecessor and successor jobs
        for (int i = 0; i < jobs.size(); i++) {
            Allocation allocation = allocations.get(i);
            Allocation sourceAllocation = allocations.stream()
                    .filter(a -> a.getJob().getJobType() == SOURCE
                            && a.getJob().getProject().equals(allocation.getJob().getProject()))
                    .findFirst()
                    .get();
            Allocation sinkAllocation = allocations.stream()
                    .filter(a -> a.getJob().getJobType() == SINK
                            && a.getJob().getProject().equals(allocation.getJob().getProject()))
                    .findFirst()
                    .get();
            List<Allocation> predecessorAllocations = allocations.stream()
                    .filter(a -> !a.equals(allocation) && a.getJob().getSuccessorJobs().contains(allocation.getJob()))
                    .distinct()
                    .toList();
            List<Allocation> successorAllocations = allocation.getJob().getSuccessorJobs().stream()
                    .map(j -> allocations.stream().filter(a -> a.getJob().equals(j)).findFirst().get())
                    .toList();
            allocation.setSourceAllocation(sourceAllocation);
            allocation.setSinkAllocation(sinkAllocation);
            allocation.setPredecessorAllocations(predecessorAllocations);
            allocation.setSuccessorAllocations(successorAllocations);
            allocation.setPredecessorsDoneDate(doneDate);
            allocation.setStartDateTime(startDate);
            allocation.setEndDateTime(startDate);
            boolean isSource = allocation.getJob().getJobType() == SOURCE;
            boolean isSink = allocation.getJob().getJobType() == SINK;
            if (isSource || isSink) {
                allocation.setExecutionMode(allocation.getJob().getExecutionModes().get(0));
                allocation.setDelay(0);
                if (isSink) {
                    doneDate += 4;
                }
            }
        }

        return allocations;
    }

    private void initBase() {
        this.id = (String) jsonMap.get("ID");
        this.startDate = LocalDateTime.parse((String)jsonMap.get("StartDate"), formatter);
        LocalDateTime endDate = LocalDateTime.parse((String) jsonMap.get("EndDate"), formatter);
        String termination = (String) jsonMap.get("Termination");
        String[] parts = termination.split(";");
        this.ts= Integer.parseInt(parts[0].replaceAll("\\D+", ""));
        this.us=  Integer.parseInt(parts[1].replaceAll("\\D+", ""));
    }
    public record Pair<K, V>(K key, V value) {}
}



