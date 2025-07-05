package com.example.tasks.Obj;

import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Represents a task with various attributes such as start and end dates,
 * class name, serial number, year, and completion status.
 * This class provides constructors to create Task objects and methods
 * to access and modify its properties. It also includes methods for
 * comparing tasks and checking if a task exists in a list of tasks.
 */
public class Task {
    private String dateStart, dateEnd, className, serNum, dateChecked;
    private int year;
    private boolean fullClass;
    /**
     * Default constructor for the Task class.
     * Initializes a new Task object with default values.
     */
    public Task(){}

    /**
     * Constructs a new Task object with specified details.
     *
     * @param dateStart The start date of the task.
     * @param dateEnd The end date of the task.
     * @param className The name of the class associated with the task.
     * @param serNum The serial number of the task.
     * @param year The year associated with the task.
     * @param fullClass A boolean indicating if the task pertains to a full class.
     */
    public Task(String dateStart, String dateEnd, String className, String serNum, int year, boolean fullClass) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.className = className;
        this.serNum = serNum;
        this.year = year;
        this.fullClass = fullClass;
        this.dateChecked = "";
    }

    /**
     * Gets the start date of the task.
     * @return The start date string.
     */
    public String getDateStart() {
        return dateStart;
    }

    /**
     * Sets the start date of the task.
     * @param dateStart The new start date string.
     */
    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    /**
     * Gets the end date of the task.
     * @return The end date string.
     */
    public String getDateEnd() {
        return dateEnd;
    }

    /**
     * Sets the end date of the task.
     * @param dateEnd The new end date string.
     */
    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    /**
     * Gets the class name associated with the task.
     * @return The class name string.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the class name associated with the task.
     * @param className The new class name string.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets the serial number of the task.
     * @return The serial number string.
     */
    public String getSerNum() {
        return serNum;
    }

    /**
     * Sets the serial number of the task.
     * @param serNum The new serial number string.
     */
    public void setSerNum(String serNum) {
        this.serNum = serNum;
    }


    /**
     * Gets the year associated with the task.
     * @return The year as an integer.
     */
    public int getYear() {
        return year;
    }


    /**
     * Sets the year associated with the task.
     * @param year The new year as an integer.
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Gets the date when the task was checked or completed.
     * @return The date checked string.
     */
    public String getDateChecked() {
        return dateChecked;
    }

    /**
     * Sets the date when the task was checked or completed.
     * @param dateChecked The new date checked string.
     */
    public void setDateChecked(String dateChecked) {
        this.dateChecked = dateChecked;
    }


    /**
     * Checks if the task pertains to a full class.
     * @return {@code true} if the task is for a full class, {@code false} otherwise.
     */
    public boolean isFullClass() {
        return fullClass;
    }


    /**
     * Sets whether the task pertains to a full class.
     * @param fullClass {@code true} if the task is for a full class, {@code false} otherwise.
     */
    public void setFullClass(boolean fullClass) {
        this.fullClass = fullClass;
    }

    /**
     * Compares this task with another task to check for equality.
     * Two tasks are considered equal if their start date, end date,
     * class name, serial number, and year are the same.
     *
     * @param other The other Task object to compare with.
     * @return {@code true} if the tasks are equal, {@code false} otherwise.
     */
    public boolean equals(Task other) {
        return (this.dateStart.equals(other.dateStart) &&
        this.dateEnd.equals(other.dateEnd) &&
        this.className.equals(other.className) &&
        this.serNum.equals(other.serNum) &&
        this.year == other.year);
    }

    /**
     * Checks if this task is present in a given list of tasks.
     *
     * @param tasks An ArrayList of Task objects.
     * @return {@code true} if this task is found in the list, {@code false} otherwise.
     */
    public boolean isIn(ArrayList<Task> tasks) {
        for (Task task : tasks) {
            if (this.equals(task)){
                return true;
            }
        }
        return false;
    }
}
