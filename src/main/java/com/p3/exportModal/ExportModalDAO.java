package com.p3.exportModal;

import com.p3.networking.ServerApi;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

public class ExportModalDAO {
    private final ServerApi api = new ServerApi();



    public void getTimelogsCSV(LocalDate startDate, LocalDate endDate, String savePath) {
        String url = "timelog/downloadCSV?startDate=" + startDate + "&endDate=" + endDate;

        HttpResponse<String> response = api.get(url, null, true);

        if (response != null && response.statusCode() == 200) {
            try {
                Files.write(Paths.get(savePath), response.body().getBytes());
            } catch (IOException e) {
                System.err.println("Failed to save CSV: " + e.getMessage());
            }
        } else {
            System.err.println("Failed to fetch CSV. Status: " + (response != null ? response.statusCode() : "null response"));
        }
    }
}
