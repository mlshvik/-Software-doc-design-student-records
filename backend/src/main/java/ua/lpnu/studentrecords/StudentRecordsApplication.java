package ua.lpnu.studentrecords;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входу бекенду «Система обліку студентів у ВНЗ».
 */
@SpringBootApplication
public class StudentRecordsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentRecordsApplication.class, args);
    }
}
