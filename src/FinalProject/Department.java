package FinalProject;

import java.util.HashMap;
import java.util.PriorityQueue;

public class Department {
    public HashMap<String, Course> courseMap;
    public PriorityQueue<Professor> listOfProfs;

    public Department(PriorityQueue<Professor> listOfProfs) {
        this.listOfProfs = listOfProfs;
        this.courseMap = new HashMap<String, Course>();
    }

    public void printCourseMap() {
        System.out.println(courseMap.size());
        for (String key : courseMap.keySet()) {
            Course value = courseMap.get(key);
            System.out.println("Course ID: " + key + ", Course: " + value.title);
        }
    }

    public void addGroups(String key, int extraGroups){
        Course existingCourse = courseMap.get(key);
        existingCourse.numOfGroups += extraGroups;
    }
}