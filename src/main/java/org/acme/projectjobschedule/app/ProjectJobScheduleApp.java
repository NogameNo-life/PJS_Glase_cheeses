package org.acme.projectjobschedule.app;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.resource.Resource;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.solver.ProjectJobSchedulingConstraintProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


        DataModel model = new DataModel(filePath);
        model.readOperationHashMap();
        ProjectJobSchedule problem =model.generateProjectJobSchedule();
        System.out.println("ProjectList:");
        for(Project project : problem.getProjects()){
        System.out.println(project.getId());
        System.out.println(project.getPID());
            System.out.println(project.getPriority());
            System.out.println(project.getVb());
            System.out.println(project.getGtin());
            System.out.println(project.getNp());
            System.out.println();

    }
        System.out.println("ResourceList");
        for(Resource resource : problem.getResources()){
            System.out.println(resource.getId());
            System.out.println(resource.getRID());
            System.out.println(resource.getCapacity());
            System.out.println(resource.isRenewable());
            System.out.println();
        }
    }
}
