package org.acme.projectjobschedule.app;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.time.Duration;

import org.acme.projectjobschedule.data.*;
import org.acme.projectjobschedule.domain.*;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import org.acme.projectjobschedule.solver.ProjectJobSchedulingConstraintProvider;

import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.Solver;

import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import java.nio.file.*;

public class ProjectJobScheduleApp {

    public static void main(String[] args) {

        if(args.length!=0) {
            // Load the problem from JSON
            ImportFileCreator importFileCreator = new ImportFileCreator(args[0]);
            String importFilePath = "src/main/resources/importFiles/" + args[0] + "Import.json";
            importFileCreator.convertToJsonFile(importFilePath);

            DataModel model = new DataModel(importFilePath);
            model.readOperationHashMap();
            ProjectJobSchedule problem = model.generateProjectJobSchedule();
            SolverFactory<ProjectJobSchedule> solverFactory = SolverFactory.create(new SolverConfig()
                    .withSolutionClass(ProjectJobSchedule.class)
                    .withEntityClasses(Allocation.class)
                    .withConstraintProviderClass(ProjectJobSchedulingConstraintProvider.class)
                    // The solver runs only for 5 seconds on this small dataset.
                    // It's recommended to run for at least 5 minutes ("5m") otherwise.
                    .withTerminationConfig(new TerminationConfig()
                            .withSpentLimit(Duration.ofSeconds(model.getTS())) // Максимум 5 минут
                            .withUnimprovedSpentLimit(Duration.ofSeconds(model.getUS())))); // Или 1 минута без улучшений

            // Solve the problem
            Solver<ProjectJobSchedule> solver = solverFactory.buildSolver();
            ProjectJobSchedule solution = solver.solve(problem);

            SolutionManager<ProjectJobSchedule, HardMediumSoftScore> solutionManager = SolutionManager.create(solverFactory);
            ScoreExplanation<ProjectJobSchedule, HardMediumSoftScore> scoreExplanation = solutionManager.explain(solution);

            HardMediumSoftScore score = problem.getScore();

            List<Allocation> allocations = solution.getAllocations();

            JsonExporter exporter = new JsonExporter(score, model.getID(), problem.getProjects(),
                    problem.getResources(), problem.getResourceRequirements(), allocations, scoreExplanation);

            String exportFileDefaultPath = "src/main/resources/exportFiles/" + args[0] + "Export.json";
            if (args.length > 1) {
                try {
                    Path customExportPath = Paths.get(args[1]);
                    Files.createDirectories(customExportPath.getParent()); // создаёт все нужные директории
                    exporter.convertToJsonFile(String.valueOf(customExportPath));
                } catch (IOException e) {
                    System.err.println("Ошибка записи: " + e.getMessage());
                    exporter.convertToJsonFile(exportFileDefaultPath);
                }
            }

            else {
                exporter.convertToJsonFile(exportFileDefaultPath);
            }
        }
        else {
            System.out.println("Import file not found in directory src/main/resources/ !");
        }
    }
    }
    

