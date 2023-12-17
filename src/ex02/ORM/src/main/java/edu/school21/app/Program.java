package edu.school21.app;

import edu.school21.models.Car;
import edu.school21.models.Computer;
import edu.school21.models.User;
import edu.school21.repositories.DataSource;
import edu.school21.services.*;

import java.sql.SQLException;


public class Program {
    public static void main(String[] argc) {
        AnnotationParser annotationParser = new AnnotationParser();
        ModelsScanner modelsScanner = new ModelsScanner("edu.school21.models");
        WorkingObject workingObject = new WorkingObjectImpl();
        DataSource dataSource = new DataSource();
        OrmManager ormManager = new OrmManagerImpl(annotationParser, modelsScanner, workingObject, dataSource);
        System.out.println("Initialization:");
        ormManager.initialize();
        System.out.println("--------------------");
        System.out.println("Create two 'User' objects and two 'Car' objects, and save the objects to the repository:");
        User user1 = new User(1L,"Artur", "Jann", 26);
        User user2 = new User(2L, "Ekaterina", null, 22);
        Car car1 = new Car(1L, "BMW", "green", true, 220.500);
        Car car2 = new Car(2L, "Range Rover", "read", false, null);
        System.out.println(user1 + "\n" + user2 + "\n" + car1 + "\n" + car2);
        ormManager.save(user1);
        ormManager.save(user2);
        ormManager.save(car1);
        ormManager.save(car2);
        System.out.println("--------------------");
        System.out.println("User id = 1 changed the name to \"Daniel\", we will make an update in the repository:");
        user1.setFirstName("Daniel");
        try {
            ormManager.update(user1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("--------------------");
        System.out.println("Now let's try to update with a non-existent ID = 13:");
        user1.setId(13L);
        try {
            ormManager.update(user1);
        } catch (SQLException e) {
            System.out.println("error: " + e.getMessage());
        }
        user1.setId(1L);
        System.out.println("--------------------");
        System.out.println("Find the machine with id = 1 in the repository:");
        try {
            System.out.println(ormManager.findById(1L, Car.class));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("--------------------");
        System.out.println("Find the machine with id = 15 in the repository:");
        Car car3 = null;
        try {
            car3 = ormManager.findById(15L, Car.class);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        if (car3 == null) {
            System.out.println("There is no entry in the repository");
        }
        System.out.println("--------------------");
    }
}
