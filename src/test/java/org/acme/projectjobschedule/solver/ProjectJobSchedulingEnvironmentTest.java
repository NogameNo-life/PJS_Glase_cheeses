package org.acme.projectjobschedule.solver;

import org.acme.projectjobschedule.data.DemoDataGenerator;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "slowly", matches = "true")

public class ProjectJobSchedulingEnvironmentTest {

    @Test
    void solveFullAssert() {
        solve(EnvironmentMode.FULL_ASSERT);
    }

    @Test
    void solveFastAssert() {
        solve(EnvironmentMode.FAST_ASSERT);
    }

    void solve(EnvironmentMode environmentMode) {
        SolverFactory<ProjectJobSchedule> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(ProjectJobSchedule.class)
                .withEntityClasses(Allocation.class)
                .withConstraintProviderClass(ProjectJobSchedulingConstraintProvider.class)
                .withEnvironmentMode(environmentMode)
                .withTerminationSpentLimit(Duration.ofSeconds(30)));

        // Load the problem
        DemoDataGenerator demo_data = new DemoDataGenerator();
        ProjectJobSchedule problem = demo_data.generateDemoData();

        // Solve the problem
        Solver<ProjectJobSchedule> solver = solverFactory.buildSolver();
        ProjectJobSchedule solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
    }
}
