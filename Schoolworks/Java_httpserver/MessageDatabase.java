package com.o3.server;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;
import java.security.SecureRandom;
import java.util.Base64;
import org.apache.commons.codec.digest.Crypt;


public class MessageDatabase {

    private Connection connection = null;
    private SecureRandom secureRandom = new SecureRandom();

    public synchronized void open(String dbName) throws SQLException {
        if (dbName == null || dbName.isEmpty()) {
            throw new SQLException("DATABASE_PATH is not found");
        }
        //Reference to the path of the database file and check if the file exists.
        File dbFile = new File(dbName);
        boolean dbExists = dbFile.exists();
        //Create database file if not exist.
        String jdbcUrl = "jdbc:sqlite:" + dbName;
        this.connection = DriverManager.getConnection(jdbcUrl);
        if (!dbExists) {
            initialiseDatabase();
        }
    }

    private synchronized void initialiseDatabase() throws SQLException {
        try {
            final String createUsers = 
                    "CREATE TABLE IF NOT EXISTS users (" +
                        "username TEXT PRIMARY KEY NOT NULL," +
                        "password TEXT NOT NULL," +
                        "email TEXT NOT NULL," +
                        "nickname TEXT" +
                    ")";

            final String createMessages =
                    "CREATE TABLE IF NOT EXISTS messages (" +
                            "record_id TEXT PRIMARY KEY," +
                            "target_body_name TEXT," +
                            "center_body_name TEXT," +
                            "original_posting_time INTEGER," +
                            "orbital_elements TEXT," +
                            "state_vector TEXT," +
                            "record_time_received INTEGER," +
                            "record_owner TEXT," +
                            "record_payload TEXT," +
                            "observatory TEXT" +
                    ")";
            
            try (Statement statement = connection.createStatement()) {
                statement.execute(createUsers);
                statement.execute(createMessages);
                System.out.println("Database created");
            }
        } catch (SQLException e) {
            throw new SQLException("Error initialising database: " + e.getMessage(), e);
        }
    }

    public boolean registerUser(String username, String password, String email, String nickname)
            throws SQLException {
        if (username == null || username.isEmpty()) throw new SQLException("username is empty");
        if (password == null || password.isEmpty()) throw new SQLException("password is empty");
        if (email == null || email.isEmpty()) throw new SQLException("email is empty");
        if (nickname == null || nickname.isEmpty()) throw new SQLException("nickname is empty");
       
        String hashedPassword = hashPassword(password);
        String insertSql = "INSERT INTO users(username, password, email, nickname) VALUES(?,?,?,?)";
        try (PreparedStatement insertUser = connection.prepareStatement(insertSql)) {
            insertUser.setString(1, username);
            insertUser.setString(2, hashedPassword);
            insertUser.setString(3, email);
            insertUser.setString(4, nickname);
            insertUser.executeUpdate();
        }
        System.out.println("User added to database");
        return true;
    }

    public boolean validateUser(String username, String password) throws SQLException {
        final String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement validateUser = connection.prepareStatement(sql)) {
            validateUser.setString(1, username);
            try (ResultSet resultSet = validateUser.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                String storedPassword = resultSet.getString("password");
                return verifyPassword(password, storedPassword);
            }
        }
    }

    public String getNickname(String username) throws SQLException {
        //Get nickname of the user from the database if the username exists.
        String sql = "SELECT nickname FROM users WHERE username = ?";
        try(PreparedStatement getNickName = connection.prepareStatement(sql)) {
            getNickName.setString(1, username);
            try (ResultSet resultSet = getNickName.executeQuery()) {
                if (!resultSet.next()) {
                    return "DummyNickname";
                }
                return resultSet.getString("nickname");
            }
        }
    }

    public synchronized void insertObservation(ObservationRecord dataRecord) throws SQLException {
        if (dataRecord.getRecordTime() == null) {
            dataRecord.setRecordTimeReceivedNowUtc();
        }

        try {    
            String sql = "INSERT INTO messages(record_id, target_body_name, center_body_name, original_posting_time, orbital_elements, state_vector, record_time_received, record_owner, record_payload, observatory) " 
                        + "VALUES(?,?,?,?,?,?,?,?,?,?)";
            
            try (PreparedStatement insertMessage = connection.prepareStatement(sql)) {
                insertMessage.setString(1, dataRecord.getId());
                insertMessage.setString(2, dataRecord.getTargetBodyName());
                insertMessage.setString(3, dataRecord.getCenterBodyName());
                insertMessage.setLong(4, dataRecord.getEpoch());
                
                if (dataRecord.getOrbitalElementsJson() != null 
                    && !dataRecord.getOrbitalElementsJson().isEmpty()) {
                    insertMessage.setString(5, dataRecord.getOrbitalElementsJson());
                } else {
                    insertMessage.setNull(5, Types.VARCHAR);
                }
                
                if (dataRecord.getStateVectorJson() != null 
                    && !dataRecord.getStateVectorJson().isEmpty()) {
                    insertMessage.setString(6, dataRecord.getStateVectorJson());
                } else {
                    insertMessage.setNull(6, Types.VARCHAR);
                }
                
                insertMessage.setLong(7, dataRecord.getRecordTime());
                insertMessage.setString(8, dataRecord.getRecordOwner());

                if (dataRecord.getRecordPayload() != null) {
                    insertMessage.setString(9, dataRecord.getRecordPayload());
                } else {
                    insertMessage.setNull(9, Types.VARCHAR);
                }

                if (dataRecord.getObservatory() != null) {
                    insertMessage.setString(10, dataRecord.getObservatory().toString());
                } else {
                    insertMessage.setNull(10, Types.VARCHAR);
                }

                insertMessage.executeUpdate();

                //Restrive an unique record ID.
                //try (ResultSet keys = insertMessage.getGeneratedKeys()) {
                    //if (keys.next()) {
                        //long id = keys.getLong(1);
                        //dataRecord.setId((int) id);
                        //return id;
                    //}
                //}
            }
            System.out.println("Observation record inserted into database");
        } catch (SQLException e) {
            throw new SQLException("Error inserting observation: " + e.getMessage(), e);
        }
    }

    public synchronized List<ObservationRecord> readMessage() throws SQLException {
        List<ObservationRecord> recordFromDatabase = new ArrayList<>();
        try {
            String sql = "SELECT * FROM messages ORDER BY record_id ASC";
            try (PreparedStatement extractData = connection.prepareStatement(sql)) {
                ResultSet resultSet = extractData.executeQuery();
                while (resultSet.next()) {
                    String recordId = resultSet.getString("record_id");
                    long timeReceived = resultSet.getLong("record_time_received");
                    String targetBodyName = resultSet.getString("target_body_name");
                    String centerBodyName = resultSet.getString("center_body_name");
                    long originalPostingTime = resultSet.getLong("original_posting_time");
                    String orbitalElements = resultSet.getString("orbital_elements");
                    String stateVector = resultSet.getString("state_vector");
                    String owner = resultSet.getString("record_owner");
                    String payload = resultSet.getString("record_payload");
                    String observation = resultSet.getString("observatory");

                    // Rebuild object from stored JSON
                    JSONObject orbitalElementsJson = null;
                    JSONObject stateVectorJson = null;
                    JSONArray observationArray = null;

                    if (orbitalElements != null && !orbitalElements.trim().isEmpty()) {
                        orbitalElementsJson = new JSONObject(orbitalElements);
                    }

                    if (stateVector != null && !stateVector.trim().isEmpty()) {
                        stateVectorJson = new JSONObject(stateVector);
                    }

                    if (observation != null && !observation.trim().isEmpty()) {
                        observationArray = new JSONArray(observation);
                    }

                    ObservationRecord record = new ObservationRecord();
                    // Overwrite server-controlled metadata from columns (source of truth)
                    record.setId(recordId);
                    record.setRecordTime(timeReceived);
                    record.setTargetBodyName(targetBodyName);
                    record.setCenterBodyName(centerBodyName);
                    record.setEpoch(originalPostingTime);
                    if (orbitalElementsJson != null && !orbitalElementsJson.isEmpty()) {
                        record.setOrbitalElements(orbitalElementsJson);
                    }
                    if (stateVectorJson != null && !stateVectorJson.isEmpty()) {
                        record.setStateVector(stateVectorJson);
                    }
                    record.setRecordOwner(owner);
                    record.setRecordPayload(payload);
                    record.setObservatory(observationArray);

                    recordFromDatabase.add(record);
                    System.out.println("Observation record read from database: " + recordId);
                }
            }
        } catch (Exception e) {
            throw new SQLException("Error reading messages: " + e.getMessage(), e);
        }
        return recordFromDatabase;
    }

    private String generateSha512Salt() {
        // crypt(3) SHA-512 uses "$6$<salt>$"
        byte[] saltBytes = new byte[16];
        secureRandom.nextBytes(saltBytes);
        // URL-safe Base64; keep it short-ish and without '=' padding
        String salt = Base64.getUrlEncoder().withoutPadding().encodeToString(saltBytes);
        // Crypt expects the salt format hint. Ending '$' is important.
        return "$6$" + salt + "$";
    }

    private String hashPassword(String plainPassword) {
        // hash+salt (SHA-512) using our generated salt
        String salt = generateSha512Salt();
        return Crypt.crypt(plainPassword, salt);
    }

    private boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        // crypt the plaintext using the stored hash (contains salt + algorithm params)
        String computedString = Crypt.crypt(plainPassword, storedHash);
        return storedHash.equals(computedString);
    }
}
