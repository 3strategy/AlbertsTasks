/*
 * Model class for a student
 */
package com.example.tasks.models;

public class Student {

    /**
     * ClassName : יא571
     * FullName : עובד איל
     * ID : 011972
     * NickName : איל
     * Sex : ז
     */

    private String ClassName;
    private String FullName;
    private String ID;
    private String NickName;
    private String Sex;

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String ClassName) {
        this.ClassName = ClassName;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String FullName) {
        this.FullName = FullName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNickName() {
        return NickName;
    }

    public void setNickName(String NickName) {
        this.NickName = NickName;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String Sex) {
        this.Sex = Sex;
    }
}
