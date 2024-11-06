package com.p3.login;

public class LoginService {
    public boolean validateUser(String username) {
        com.p3.networking.net request = new com.p3.networking.net();
        String Response = request.sendRequestToServer(username);
        if(Response.equals("correct") == true) {
            return true;
        } else{return false;}




    }
}
