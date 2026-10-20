package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/database")
    String database(Map<String, Object> model) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
    
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string " +
                "(tick timestamp, random_string varchar(50))"
            );
    
            String randomString = getRandomString();

            // Log database access for CS471 Section 10
            System.out.println("Thomas Herrera - Database accessed - Random String: " + randomString);
    
            try (var insert = connection.prepareStatement(
                    "INSERT INTO table_timestamp_and_random_string VALUES (now(), ?)")) {
                insert.setString(1, randomString);
                insert.executeUpdate();
            }
    
            final var resultSet = statement.executeQuery(
                "SELECT tick, random_string FROM table_timestamp_and_random_string ORDER BY tick DESC"
            );
    
            final var output = new ArrayList<String>();
    
            while (resultSet.next()) {
                output.add(
                    "Timestamp: " + resultSet.getTimestamp("tick") +
                    " | Random String: " + resultSet.getString("random_string")
                );
            }
    
            model.put("records", output);
            return "database";
        }
    }

    private String getRandomString() {
        return java.util.UUID.randomUUID().toString().substring(0, 12);
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
