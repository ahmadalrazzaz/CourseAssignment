package FinalProject;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        PriorityQueue<Professor> professorQueue = new PriorityQueue<>(Comparator.reverseOrder());
        //PriorityQueue<Professor> professorQueue = new PriorityQueue<>();

        // Reading professors.txt file and adding to PriorityQueue

        try {
            File professorsFile = new File("Data/professors.txt");

            BufferedReader professorReader = new BufferedReader(new FileReader(professorsFile));
            String line = professorReader.readLine();

            while (line != null) {
                // Splitting and assigning each part to professor attributes
                String[] parts = line.split(":");
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                double seniority = Double.parseDouble(parts[2].replaceAll("[a-zA-z]", "").trim());
                Set<String> disciplines = new HashSet<>();

                if (parts.length > 3 && !parts[3].isEmpty()) {
                    String[] disciplinesString = parts[3].replaceAll("[ |-]", "").split("[,/et]+");
                    Collections.addAll(disciplines, disciplinesString);
                    professorQueue.add(new Professor(id, name, seniority, disciplines));
                } else {
                    professorQueue.add(new Professor(id, name, seniority, new HashSet<>()));
                }

                line = professorReader.readLine();
            }
            professorReader.close();
        } catch (IOException e) {}

        Department csDepartment = new Department(professorQueue);

        // Reading ListeDeCours.csv file and adding to Department CourseMap

        try {
            File courseFile = new File("Data/ListeDeCours.csv");

            BufferedReader courseReader = new BufferedReader(new FileReader(courseFile));

            courseReader.readLine(); // Skips the first line containing row titles
            String line = courseReader.readLine();

            while (line != null) {
                // <courseID, Course>

                // Splitting and assigning each part to course attributes
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String discipline = parts[0].replaceAll("-", "").trim();
                    String courseId = parts[1].trim() + "-" + parts[4].trim(); // adding language at the end for distinction
                    String title = parts[2];
                    int numberOfHours = Integer.parseInt(parts[3].trim());
                    int numOfGroups = Integer.parseInt(parts[5].trim());

                    // adds extra groups to duplicate course entries
                    if (csDepartment.courseMap.containsKey(courseId)) {
                        csDepartment.addGroups(courseId, numOfGroups);
                    } else {
                        Course course = new Course(courseId, title, discipline, numberOfHours, numOfGroups);
                        csDepartment.courseMap.put(courseId, course);
                    }
                } else if (parts.length > 6) { // addressing formatting issue in the course list CSV file because of commas in course title
                    String discipline = parts[0].replaceAll("-", "").trim();
                    String courseId = parts[1].trim() + "-" + parts[5].trim();
                    String title = parts[2].trim() + ", " + parts[3].trim();
                    int numberOfHours = Integer.parseInt(parts[4].trim());
                    int numOfGroups = Integer.parseInt(parts[6].trim());

                    // adds extra groups to duplicate course entries
                    if (csDepartment.courseMap.containsKey(courseId)) {
                        csDepartment.addGroups(courseId, numOfGroups);
                    } else {
                        Course course = new Course(courseId, title, discipline, numberOfHours, numOfGroups);
                        csDepartment.courseMap.put(courseId, course);
                    }
                }
                line = courseReader.readLine();
            }
            courseReader.close();
        } catch (IOException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        // cloned the professorQueue since we wanted to poll() and that deletes the instance of professor from the queue
        PriorityQueue<Professor> requestsQueue = new PriorityQueue<>(professorQueue);

        while (!requestsQueue.isEmpty()) {
            Professor thisProf = requestsQueue.poll();

            StringBuilder requestName = new StringBuilder();
            requestName.append("Data/").append(thisProf.id).append("_request.txt"); // Data/{id}_request.txt
            File requestFile = new File(requestName.toString());

            // Makes sure that the request file exists before running the BufferedReader
            if (!requestFile.exists()) {
                continue;
            }

            // Reads first line of the request where the professor wrote the hours they want per week
            try {
                BufferedReader requestReader = new BufferedReader(new FileReader(requestFile));

                String finalRequestName = "Output/" + thisProf.id + "_request_final.csv";
                BufferedWriter writer = new BufferedWriter(new FileWriter(finalRequestName));

                String line = requestReader.readLine();

                if (line != null) {
                    String[] parts = line.split("[, -]+");
                    for (String part : parts)
                    {
                        try {
                            int hours = Integer.parseInt(part); // If it is a number
                            if (hours > thisProf.maxHours){ // Checks if more than current weekly hours
                                thisProf.maxHours = hours; // if it is, sets it as the max weekly hours
                            }
                        } catch (NumberFormatException e){} // Catches exceptions where it's not a number
                    }
                }

                // if no number is present in the hours request, assign default value of 24
                if (thisProf.maxHours == -1) {
                    thisProf.maxHours = 24;
                }

                thisProf.maxHoursAllowed = thisProf.maxHours;

                writer.write(line);
                line = requestReader.readLine();

                boolean continueReading = true;
                boolean courseNotFound;
                int numberOfGroupsAssigned = 0;

                while (line != null) {
                    if (!(line.contains("NaN") && !line.contains("420"))){
                        String[] parts = line.split(":");

                        String language = null;

                        int numOfGroups;

                        try {
                            numOfGroups = Integer.parseInt(parts[4].trim());
                        } catch (NumberFormatException e) {
                            numOfGroups = 1;
                        }

                        // Checks language of the requested course to be added to the request courseID
                        if (parts[3].contains("English") || parts[3].contains("Anglais") || parts[3].contains("AN")){
                            language = "AN";
                        } else if (parts[3].contains("French") || parts[3].contains("Français") || parts[3].contains("FR")) {
                            language = "FR";
                        } else { // In case neither language is written, check for corresponding title
                            String courseTitle = parts[1].trim();

                            courseNotFound = false;

                            for (Course course : csDepartment.courseMap.values()){
                                if (course.title.equals(courseTitle))
                                {
                                    String courseID = course.id;
                                    String discipline = course.discipline;
                                    int numberOfHours = course.numberOfHours;
                                    int weeklyHours = numberOfHours/15;
                                    Course requestedCourse = new Course(courseID, courseTitle, discipline, numberOfHours, numOfGroups);

                                    if (thisProf.maxHours >= weeklyHours) {
                                        if (thisProf.setOfDisciplines.contains(requestedCourse.discipline)){
                                            thisProf.addCourse(requestedCourse, numOfGroups);
                                        }
                                    } else {
                                        thisProf.rejectCourse(course);
                                        break;
                                    }

                                    continueReading = false;
                                }
                                else {
                                    courseNotFound = true;
                                }
                            }

                            if (courseNotFound = true) {
                                String courseID = parts[0].trim();
                                String discipline = parts[2].replaceAll("-", "").trim();

                                Course requestedCourse = new Course(courseID, courseTitle, discipline, 0,0);

                                thisProf.rejectCourse(requestedCourse);
                            }
                        }

                        if (continueReading){
                            String courseID = parts[0].trim() + "-" + language;
                            String title = parts[1].trim();
                            String discipline = parts[2].replaceAll("-", "").trim();

                            Course course = csDepartment.courseMap.get(courseID);
                            int numberOfHours;
                            int weeklyHours;
                            Course requestedCourse;

                            if (course != null) {
                                numberOfHours = course.numberOfHours;
                                weeklyHours = numberOfHours/15;
                                requestedCourse = new Course(courseID, title, discipline, numberOfHours, numOfGroups);

                                if (thisProf.maxHours >= weeklyHours) {
                                    if (thisProf.setOfDisciplines.contains(requestedCourse.discipline)){
                                        thisProf.addCourse(requestedCourse, numOfGroups);
                                        numberOfGroupsAssigned = numOfGroups;
                                    }
                                    else {
                                        thisProf.rejectCourse(course);
                                    }
                                } else {
                                    thisProf.rejectCourse(course);
                                    break;
                                }

                            } else {
                                thisProf.rejectCourse(new Course(courseID, title, discipline, 0, numOfGroups));
                            }
                        }
                    }
                    writer.write(line + "- Assigned groups: " + numberOfGroupsAssigned);
                    writer.newLine();

                    line = requestReader.readLine();
                }

                requestReader.close();

            } catch (IOException e){}

        }

        PriorityQueue<Professor> professorAssignments = new PriorityQueue<>(professorQueue);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Output/professors_finalAssignments.txt"))) {

            while (!professorAssignments.isEmpty()){
                Professor professor = professorAssignments.poll();

                int totalAssignedHours = 0;
                int totalClassesAssigned = 0;

                if (professor.listOfAffectedCourses != null) {
                    for (Course course : professor.listOfAffectedCourses) {
                        if (course != null) {
                            totalAssignedHours += (course.numberOfHours/15);
                            totalClassesAssigned++;
                        }
                    }
                }

                writer.write(professor.id + " | " + professor.maxHoursAllowed + ", " + totalAssignedHours + " | " + totalClassesAssigned);
                writer.newLine();
            } System.out.println("professors_finalAssignments.txt created successfully.");
        } catch (IOException e) {
            System.out.println("Error writing professors_finalAssignments.txt");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Output/courses_unassigned.csv"))) {
            for (Map.Entry<String, Course> entry : csDepartment.courseMap.entrySet()) {
                Course course = entry.getValue();
                if (course.numOfGroups > 0) {
                    writer.write(course.id + "," + course.title + "," + course.discipline + "," + course.numberOfHours + "," + course.numOfGroups);
                    writer.newLine();
                }
            }
            System.out.println("courses_unassigned.csv created successfully.");
        } catch (IOException e) {
            System.out.println("Error writing courses_unassigned.csv");
        }
    }
}