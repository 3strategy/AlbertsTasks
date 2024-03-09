package com.example.tasks.Obj;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Task {
    private String dateStart, dateEnd, className, serNum, dateChecked;
    private int year;
    private boolean fullClass;
    public Task(){}

    public Task(String dateStart, String dateEnd, String className, String serNum, int year, boolean fullClass) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.className = className;
        this.serNum = serNum;
        this.year = year;
        this.fullClass = fullClass;
        this.dateChecked = "";
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSerNum() {
        return serNum;
    }

    public void setSerNum(String serNum) {
        this.serNum = serNum;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDateChecked() {
        return dateChecked;
    }

    public void setDateChecked(String dateChecked) {
        this.dateChecked = dateChecked;
    }

    public boolean isFullClass() {
        return fullClass;
    }

    public void setFullClass(boolean fullClass) {
        this.fullClass = fullClass;
    }

    public boolean equals(Task other) {
        return (this.dateStart.equals(other.dateStart) &&
        this.dateEnd.equals(other.dateEnd) &&
        this.className.equals(other.className) &&
        this.serNum.equals(other.serNum) &&
        this.year == other.year);
    }
    public boolean isIn(ArrayList<Task> tasks) {
        for (Task task : tasks) {
            if (this.equals(task)){
                return true;
            }
        }
        return false;
    }
}
