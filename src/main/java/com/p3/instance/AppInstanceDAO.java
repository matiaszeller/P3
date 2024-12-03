package com.p3.instance;

import com.p3.networking.ServerApi;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppInstanceDAO {

    private final ServerApi api = new ServerApi();

    /*public LocalDateTime getServerTime() {
        String url = "time/getTime";
        HttpResponse response = api.get(url, null, false);

        if (response.statusCode() == 200) {
            String timeString = (String) response.body();
            return LocalDateTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            throw new RuntimeException("Failed to fetch server time: " + response.statusCode());
        }
    } */
        }