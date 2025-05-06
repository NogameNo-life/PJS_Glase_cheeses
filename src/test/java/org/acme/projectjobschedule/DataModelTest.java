package org.acme.projectjobschedule;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;

import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

import org.acme.projectjobschedule.data.DataModel;
import org.acme.projectjobschedule.data.JsonExporter;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;

import org.acme.projectjobschedule.solver.ProjectJobSchedulingConstraintProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.TestInstance;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataModelTest {
    private ProjectJobSchedule solution;

    @BeforeAll
    public void solveProblemOnce() {
        solution = solveProblem();
    }

    @Test
    public void testImportAndExport() throws Exception {

        String actualOutputFile = Files.readString(Paths.get("src/test/resources/exportData.json"));
        String expectedOutputFile = Files.readString(Paths.get("src/test/resources/expectedOutput.json"));
        // Сравнение содержимого JSON
        JSONAssert.assertEquals(actualOutputFile, expectedOutputFile, JSONCompareMode.STRICT);
    }

    @Test
    public void testHardScoreIsZero() {
        assertThat(solution.getScore().hardScore()).isEqualTo(0);
    }

    private ProjectJobSchedule solveProblem() {
        // Файл для импорта
        String inputFilePath = "src/test/resources/importData.json";
        // Файл для экспорта
        String actualOutputPath = "src/test/resources/exportData.json";

        DataModel model = new DataModel(inputFilePath);
        model.readOperationHashMap();
        ProjectJobSchedule problem = model.generateProjectJobSchedule();
        SolverFactory<ProjectJobSchedule> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(ProjectJobSchedule.class)
                .withEntityClasses(Allocation.class)
                .withConstraintProviderClass(ProjectJobSchedulingConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig()
                        .withSpentLimit(Duration.ofSeconds(model.getTS()))
                        .withUnimprovedSpentLimit(Duration.ofSeconds(model.getUS()))));
        // Solve the problem
        Solver<ProjectJobSchedule> solver = solverFactory.buildSolver();
        ProjectJobSchedule solution = solver.solve(problem);

        SolutionManager<ProjectJobSchedule, HardMediumSoftScore> solutionManager = SolutionManager.create(solverFactory);
        ScoreExplanation<ProjectJobSchedule, HardMediumSoftScore> scoreExplanation = solutionManager.explain(solution);

        HardMediumSoftScore score = problem.getScore();

        List<Allocation> allocations = solution.getAllocations();

        JsonExporter exporter = new JsonExporter(score, model.getID(), problem.getProjects(),
                problem.getResources(),problem.getResourceRequirements(), allocations,scoreExplanation);
        exporter.convertToJsonFile(actualOutputPath);

        return solution;
    }
}
