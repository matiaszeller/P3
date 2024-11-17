package com.p3.login;

import com.p3.config.DatabaseConfig;
import com.p3.networking.ServerApi;

import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDateTime;

/*  TODO OBS!!!! Disse queries ligger lokalt lige NU. Men de skal flyttes til at køre på serveren.
    Denne class skal kalde de forskellige queries og sende dem tilbage til LoginService.
    Her skal vi måske bruge Jakobs kode til at oprette connections(?)
    Husk på: Controllers: Håndterer UI og kalder services
             Services: Logic
             DAO: Database kald
*/
public class LoginDAO {
    private final ServerApi api = new ServerApi();

    public String getUserRole(String username){
        String url = "user/role/" + username;
        HttpResponse response = api.get(url, null);

        return (String) response.body(); // Works for this method, but all repsonses from server should return json
    }

    public String getManagerPassword(String username) { // TODO Prob not correct way to secure password
        String url = "user/pass/" + username;
        HttpResponse response = api.get(url, null);

        return (String) response.body();        // TODO har ikke lige testet om den decrypter ordentligt på service men burde virke
    }

    public String getUserId(String username) {
        String url = "user/id/" + username;
        HttpResponse response = api.get(url, null);

        return (String) response.body();
    }

    public String getUserFullName(String username) {
        String url = "user/name/" + username;
        HttpResponse response = api.get(url, null);

        return (String) response.body();
    }

    public String getClockedInStatus(String username) {
        String url = "user/clockInStatus/" + username;
        HttpResponse response = api.get(url, null);

        return (String) response.body();
    }

    public void setClockedInStatus(String username, boolean status) {
        String url = "user/clockInStatus/" + username + "?status=" + status;

        try{
            api.put(url, null, null);   // TODO Kan ikke lige få headers til at virke som jeg troede
        }
        catch(Exception e){
            e.printStackTrace();    // Jeg er træt og forstår ikke hvorfor den kræver exception her - tror det fordi det put but idk fucksss
        }
    }

    public void insertCheckInEvent(int userId, LocalDateTime eventTime) {

    }
}