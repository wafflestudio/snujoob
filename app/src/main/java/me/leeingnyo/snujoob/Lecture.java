package me.leeingnyo.snujoob;

public class Lecture {
    public Integer id;
    public String name;
    public String subjectNumber;
    public String lectureNumber;
    public String lecturer;
    public String time;
    public Integer enrolled;
    public Integer wholeCapacity;
    public Integer enrolledCapacity;
    public Integer competitor;
    // 귀찮다 일단 다 public


    @Override
    public String toString() {
        return String.format("%s (%s %s)", name, lectureNumber, lecturer);
    }
}