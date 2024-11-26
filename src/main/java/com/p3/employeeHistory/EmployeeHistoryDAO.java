package com.p3.employeeHistory;

import com.p3.networking.ServerApi;

import java.net.http.HttpResponse;
import java.time.LocalDate;

public class EmployeeHistoryDAO {

    private final ServerApi api = new ServerApi();

    public String getWeekTimelogs(LocalDate localDate, int userId){
        String url = "timelog/history?date=" + localDate + "&userId=" + userId;
        HttpResponse response = api.get(url, null);

        return (String) response.body();
    }

}
