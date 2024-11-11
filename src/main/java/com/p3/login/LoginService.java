package com.p3.login;

import com.p3.login.LoginDAO;
import org.mindrot.jbcrypt.BCrypt;

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
}