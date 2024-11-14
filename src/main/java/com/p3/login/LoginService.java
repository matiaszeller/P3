package com.p3.login;

import com.p3.login.LoginDAO;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;

//todo implement networking feature, to talk with server.
public class LoginService {
    private final LoginDAO loginDAO = new LoginDAO();
    public String validateUser(String username) {
        return loginDAO.getUserRole(username);
    }

    public boolean validateManager(String username, String password) {
        String storedPassword = loginDAO.getManagerPassword(username);
        return storedPassword != null && BCrypt.checkpw(password, storedPassword);
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public int getUserId(String username) {
        return loginDAO.getUserId(username);
    }
    public String getUserFullName(String username) {
        return loginDAO.getUserFullName(username);
    }

    public boolean getClockedInStatus(String username) {
        return loginDAO.getClockedInStatus(username);
    }

    public void setClockedInStatus(String username, boolean status) {
        loginDAO.setClockedInStatus(username, status);
    }

    public void insertCheckInEvent(int userId, LocalDateTime eventTime) {
        loginDAO.insertCheckInEvent(userId, eventTime);
    }
}