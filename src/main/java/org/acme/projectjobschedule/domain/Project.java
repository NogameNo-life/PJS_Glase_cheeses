package org.acme.projectjobschedule.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

//@JsonIdentityInfo(scope = Project.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Project {

    private String PID;
    private String id;
    @JsonProperty("Priority")
    private int priority;
    @JsonProperty("VB")
    private int vb;
    @JsonProperty("GTIN")
    private String gtin;
    @JsonProperty("NP")
    private int np;
    private int releaseDate;
    private int criticalPathDuration;

    public Project() {
    }

    public Project(String id) {
        this.id = id;
    }

    public Project(String id, int releaseDate, int criticalPathDuration) {
        this(id);
        this.releaseDate = releaseDate;
        this.criticalPathDuration = criticalPathDuration;
    }

    public String getPID(){
        return  PID;
    }

    public void setPID(String PID) {
        this.PID = PID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public  int getVb (){
        return vb;
    }

    public  void setVb (int vb ){
        this.vb= vb;
    }

    public int getPriority(){
        return priority;
    }

    public void setPriority(int priority){
        this.priority=priority;
    }

    public String getGtin(){
        return gtin;
    }

    public void setGtin(String gtin){
        this.gtin=gtin;
    }

    public  int getNp(){
        return np;
    }

    public void setNp(int np){
        this.np=np;
    }
    public int getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(int releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getCriticalPathDuration() {
        return criticalPathDuration;
    }

    public void setCriticalPathDuration(int criticalPathDuration) {
        this.criticalPathDuration = criticalPathDuration;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public int getCriticalPathEndDate() {
        return releaseDate + criticalPathDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Project project))
            return false;
        return Objects.equals(getId(), project.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }


}
