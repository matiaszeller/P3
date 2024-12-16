package com.p3.exportModal;

import java.time.LocalDate;

public class ExportModalService {

    ExportModalDAO exportModalDAO = new ExportModalDAO();

    public void getTimelogsCSV(LocalDate startDate, LocalDate endDate, String savePath) {
        exportModalDAO.getTimelogsCSV(startDate, endDate, savePath);
    }
}
