package org.acme.projectjobschedule.app;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.solver.ProjectJobSchedulingConstraintProvider;
import org.acme.projectjobschedule.data.DemoDataGenerator;
import org.acme.projectjobschedule.app.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.acme.projectjobschedule.app.JsonImporter;
import java.nio.file.Path;
import java.nio.file.Paths;



public class ProjectJobScheduleApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectJobScheduleApp.class);

    public enum DemoData {
        SMALL,
        LARGE
    }


    public static void main(String[] args) {

        SolverFactory<ProjectJobSchedule> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(ProjectJobSchedule.class)
                .withEntityClasses(Allocation.class)
                .withConstraintProviderClass(ProjectJobSchedulingConstraintProvider.class)
                // The solver runs only for 5 seconds on this small dataset.
                // It's recommended to run for at least 5 minutes ("5m") otherwise.
                .withTerminationSpentLimit(Duration.ofSeconds(5)));

        // Load the problem
     //   DemoDataGenerator demo_data = new DemoDataGenerator();
      //  ProjectJobSchedule problem = demo_data.generateDemoData();

        // Load the problem from JSON
        String filePath = "src/main/resources/data.json"; // Путь к файлу JSON

        // Создание экземпляра JsonImporter
        JsonImporter importer = new JsonImporter(filePath);
        ProjectJobSchedule problem = importer.initProjectJobSheduleObject();

    }

    public static void  printProjectJobSchedule(ProjectJobSchedule schedule){
            LOGGER.info("");
            List<Project> projects = schedule.getProjects();
            List<Job> jobs = schedule.getJobs();

            Map<Project, List<Job>> projectJobMap = jobs.stream()
                    .filter(job -> job.getProject() != null)
                    .collect(Collectors.groupingBy(Job::getProject));

            LOGGER.info("| Project id |                   Project                     | Job type   |");
            LOGGER.info("|" + "------------|-----------------------------------------------|------------|");

            for (Project project : projects) {
                List<Job> projectJobs = projectJobMap.getOrDefault(project, Collections.emptyList());

                if (projectJobs.isEmpty()) {
                    LOGGER.info("| " + String.format("%-11s", project.getReleaseDate()) + " | "
                            + "No scheduled jobs".formatted() + " |");
                    continue;
                }

                for (Job job : projectJobs) {
                    LOGGER.info("| " + String.format("%-10s", project.getId()) + " | "
                            + String.format("%-11s", job.getProject()) + " | "
                            + String.format("%-10s", job.getJobType()) + " | ");
                }
                LOGGER.info("|" + "------------|-----------------------------------------------|------------|");
            }

            List<Job> unassignedJobs = jobs.stream()
                    .filter(job -> job.getProject() == null || job.getJobType() == null)
                    .toList();

            if (!unassignedJobs.isEmpty()) {
                LOGGER.info("");
                LOGGER.info("Unassigned jobs:");
                for (Job job : unassignedJobs) {
                    LOGGER.info("  " + job.getProject() + " - Job type: " + job.getJobType());
                }
            }
        }
    }




