package com.p3.employeeHistory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeHistoryService {
    EmployeeHistoryDAO employeeHistoryDAO = new EmployeeHistoryDAO();

    // Return list of lists - Each list contains all timelogs for a day. Allows for looping through each day (no more than 7) and append to pane on fxml
    public JSONArray getWeekTimelogs(LocalDate localDate, int userId) {
        // Response is in string format and is converted to a jsonArray
        return new JSONArray(employeeHistoryDAO.getWeekTimelogs(localDate, userId));
    }
}
