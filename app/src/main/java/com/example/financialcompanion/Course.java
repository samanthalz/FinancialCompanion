package com.example.financialcompanion;

public class Course {

    String course_name;
    String course_author;
    Boolean quiz_status;
    Integer read_duration;


    public Course(String course_name, String course_author, Boolean quiz_status, Integer read_duration){
        String courseName = this.course_name;
        String author = this.course_author;
        Boolean quizStatus = this.quiz_status;
        Integer duration = this.read_duration;

    }

    //generated getter and setter
    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getCourse_author() {
        return course_author;
    }

    public void setCourse_author(String course_author) {
        this.course_author = course_author;
    }

    public Boolean getQuiz_status() {
        return quiz_status;
    }

    public void setQuiz_status(Boolean quiz_status) {
        this.quiz_status = quiz_status;
    }

    public Integer getRead_duration() {
        return read_duration;
    }

    public void setRead_duration(Integer read_duration) {
        this.read_duration = read_duration;
    }
}
