package com.o3.server;

import com.sun.net.httpserver.BasicAuthenticator;
import java.sql.SQLException;

public class UserAuthenticator extends BasicAuthenticator {
    private MessageDatabase myDb;


    public UserAuthenticator(String datarecord, MessageDatabase myDb) {
        super(datarecord);
        this.myDb = myDb;
    }

    @Override
    public boolean checkCredentials(String userName, String passWord) {
        if (userName == null || passWord == null) {
            return false;
        }
        
        try {
            return myDb.validateUser(userName, passWord);
        } catch (SQLException e) {
            System.err.println("Error validating user credentials: " + e.getMessage());
            return false; 
        }
    }

    public boolean addUser(String userName, String passWord, String email, String nickName) {
        if (userName == null || passWord == null || email == null || nickName == null) {
            return false;
        }

        if (userName.trim().isEmpty() || passWord.trim().isEmpty() || email.trim().isEmpty() || nickName.trim().isEmpty()) {
            return false;
        }
        
        try {
            return myDb.registerUser(userName, passWord, email, nickName);
        } catch (SQLException e) {
            System.err.println("Error adding user to database: " + e.getMessage());
            return false; 
        }
    }
}
