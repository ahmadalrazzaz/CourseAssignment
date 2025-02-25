package FinalProject;

public class Course {
    public String id;
    public String title;
    public String discipline;
    public int numberOfHours;
    public int numOfGroups;

    public Course(String id, String title, String discipline, int numberOfHours, int numOfGroups) {
        this.id = id;
        this.title = title;
        this.discipline = discipline;
        this.numberOfHours = numberOfHours;
        this.numOfGroups = numOfGroups;
    }

    public Course(Course other) {
        this.id = new String(other.id);
        this.title = other.title;
        this.discipline = other.discipline;
        this.numberOfHours = other.numberOfHours;
        this.numOfGroups = other.numOfGroups;
    }
}