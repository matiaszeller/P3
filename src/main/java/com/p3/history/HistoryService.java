package com.p3.history;

public class HistoryService {

    private final HistoryDAO historyDAO = new HistoryDAO();

    public boolean checkDatabaseConnection() {
        return historyDAO.checkDatabaseConnection();
    }
}
