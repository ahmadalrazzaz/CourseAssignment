package FinalProject;

import java.util.ArrayList;
import java.util.Set;

public class Professor implements Comparable<Professor>{
    public int id;
    public String name;
    public double seniorityLevel;
    public Set<String> setOfDisciplines;
    public ArrayList<Course> listOfAffectedCourses;
    public ArrayList<Course> rejectedCourses;
    public int maxHours;
    public int maxHoursAllowed;

    public Professor(int id, String name, double seniority, Set<String> setOfDisciplines) {
        this.id = id;
        this.name = name;
        this.seniorityLevel = seniority;
        this.setOfDisciplines = setOfDisciplines;
        this.listOfAffectedCourses = null;
        this.rejectedCourses = null;
        this.maxHours = -1;
    }
    
    public void addCourse(Course course, int numOfGroups) {
        if (listOfAffectedCourses == null) {
            listOfAffectedCourses = new ArrayList<>();
        }
        if (course != null) {
            if (course.numOfGroups >= numOfGroups && course.numOfGroups > 0) {
                maxHours -= (course.numberOfHours/15);
                course.numOfGroups -= numOfGroups;

                listOfAffectedCourses.add(course);
            }
            else {
                rejectCourse(course);
            }
        }
    }

    public void rejectCourse(Course course) {
        if (rejectedCourses == null) {
            rejectedCourses = new ArrayList<>();
        }
        if (course != null) {
            rejectedCourses.add(course);
        }
    }
    
    public void displayCourses() {
        System.out.println("--------------------------------");
        System.out.println("Professor ID:" + id);
        int counter = 1;
        if (listOfAffectedCourses != null) {
            System.out.println("---- Accepted Courses ----------");
            for (Course course : this.listOfAffectedCourses) {
                System.out.println("Course " + counter + ": " + course.id + " : " + course.title);
                counter++;
            }
        }
        if (rejectedCourses != null) {
            System.out.println("---- Rejected Courses ----------");
            for (Course courses : rejectedCourses) {
                System.out.println("Course: " + courses.id + " : " + courses.title);
            }
        }
    }

    // Did not use this method because noticed that higher ID means higher seniority so
    // we sorted the list in descending order in the priority queue and went through the requests in that order.
    @Override
    public int compareTo(Professor other) {
        if (this.seniorityLevel > other.seniorityLevel) {
            return 1;
        } else if (this.seniorityLevel < other.seniorityLevel) {
            return -1;
        } else if (this.id < other.id) {
            return 1;
        }
        return -1;
    }
}

