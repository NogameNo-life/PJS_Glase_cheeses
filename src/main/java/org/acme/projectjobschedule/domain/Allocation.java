package org.acme.projectjobschedule.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.valuerange.CountableValueRange;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeFactory;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import org.acme.projectjobschedule.domain.solver.DelayStrengthComparator;
import org.acme.projectjobschedule.domain.solver.ExecutionModeStrengthWeightFactory;
import org.acme.projectjobschedule.domain.solver.NotSourceOrSinkAllocationFilter;
import org.acme.projectjobschedule.domain.solver.PredecessorsDoneDateUpdatingVariableListener;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@PlanningEntity(pinningFilter = NotSourceOrSinkAllocationFilter.class)
@JsonIdentityInfo(scope = Allocation.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Allocation {

    @PlanningId
    private String id;
    private Job job;

    @JsonIdentityReference(alwaysAsId = true)
    private Allocation sourceAllocation;
    @JsonIdentityReference(alwaysAsId = true)
    private Allocation sinkAllocation;
    @JsonIdentityReference(alwaysAsId = true)
    private List<Allocation> predecessorAllocations;
    @JsonIdentityReference(alwaysAsId = true)
    private List<Allocation> successorAllocations;

    // Planning variables: changes during planning, between score calculations.
    @PlanningVariable(strengthWeightFactoryClass = ExecutionModeStrengthWeightFactory.class)
    private ExecutionMode executionMode;
    @PlanningVariable(strengthComparatorClass = DelayStrengthComparator.class)
    private Integer delay; // In days

    // Shadow variables
    @ShadowVariable(variableListenerClass = PredecessorsDoneDateUpdatingVariableListener.class,
            sourceVariableName = "executionMode")
    @ShadowVariable(variableListenerClass = PredecessorsDoneDateUpdatingVariableListener.class, sourceVariableName = "delay")
    private Integer predecessorsDoneDate;

    // Filled from shadow variables
    private Integer startDate;
    private Integer endDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<Integer> busyDates;

    public Allocation() {
    }

    public Allocation(String id) {
        this.id = id;
    }

    public Allocation(String id, Job job) {
        this(id);
        this.job = job;
        this.predecessorsDoneDate = 0;
    }

    // ************************************************************************
    // Ranges
    // ************************************************************************

    @ValueRangeProvider
    @JsonIgnore
    public List<ExecutionMode> getExecutionModeRange() {
        return job.getExecutionModes();
    }

    @ValueRangeProvider
    @JsonIgnore
    public CountableValueRange<Integer> getDelayRange() {
        return ValueRangeFactory.createIntValueRange(0, 500);
    }

    // ************************************************************************
    // Get and Set
    // ************************************************************************

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Allocation getSourceAllocation() {
        return sourceAllocation;
    }

    public void setSourceAllocation(Allocation sourceAllocation) {
        this.sourceAllocation = sourceAllocation;
    }

    public Allocation getSinkAllocation() {
        return sinkAllocation;
    }

    public void setSinkAllocation(Allocation sinkAllocation) {
        this.sinkAllocation = sinkAllocation;
    }

    public List<Allocation> getPredecessorAllocations() {
        return predecessorAllocations;
    }

    public void setPredecessorAllocations(List<Allocation> predecessorAllocations) {
        this.predecessorAllocations = predecessorAllocations;
    }

    public List<Allocation> getSuccessorAllocations() {
        return successorAllocations;
    }

    public void setSuccessorAllocations(List<Allocation> successorAllocations) {
        this.successorAllocations = successorAllocations;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
        invalidateComputedVariables();
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
        invalidateComputedVariables();
    }

    public Integer getPredecessorsDoneDate() {
        return predecessorsDoneDate;
    }

    public void setPredecessorsDoneDate(Integer predecessorsDoneDate) {
        this.predecessorsDoneDate = predecessorsDoneDate;
        invalidateComputedVariables();
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public void invalidateComputedVariables() {
        this.startDate = null;
        this.endDate = null;
        this.busyDates = null;
    }

    public Integer getStartDate() {
        if (predecessorsDoneDate != null) {
            startDate = predecessorsDoneDate + Objects.requireNonNullElse(delay, 0);
        }
        return startDate;
    }

    public Integer getEndDate() {
        if (predecessorsDoneDate != null) {
            endDate = getStartDate() + (executionMode == null ? 0 : executionMode.getDuration());
        }
        return endDate;
    }

    public LocalDateTime getStartDateTime(){
        Duration startDuration = Duration.ofMinutes(getStartDate());
        return startDateTime.plus(startDuration);
    }

    public LocalDateTime getEndDateTime(){
        Duration endDuration = Duration.ofMinutes(getEndDate());
        return endDateTime.plus(endDuration);
    }

    public void setStartDateTime(LocalDateTime startDateTime){
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime){
        this.endDateTime = endDateTime;
    }

    @JsonIgnore
    public List<Integer> getBusyDates() {
        if (busyDates == null) {
            if (predecessorsDoneDate == null) {
                busyDates = Collections.emptyList();
            } else {
                var start = getStartDate();
                var end = getEndDate();
                var dates = new Integer[end - start];
                for (int i = 0; i < dates.length; i++) {
                    dates[i] = start + i;
                }
                busyDates = Arrays.asList(dates);
            }
        }
        return busyDates;
    }

    @JsonIgnore
    public Project getProject() {
        return job.getProject();
    }

    @JsonIgnore
    public int getProjectCriticalPathEndDate() {
        return job.getProject().getCriticalPathEndDate();
    }

    @JsonIgnore
    public int getProjectDelay() {
        return getEndDate() - getProjectCriticalPathEndDate();
    }

    @JsonIgnore
    public JobType getJobType() {
        return job.getJobType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Allocation that))
            return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Allocation-" + id;
    }
}
