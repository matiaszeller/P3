package com.p3.timelogEditModal;

import com.p3.networking.ServerApi;
import org.json.JSONArray;

public class TimelogEditModalDAO {

    private final ServerApi api = new ServerApi();

    public void postUpdatedTimelogs(JSONArray timelogs) {
        String url = "timelog/list";

        try{
            api.put(url, null, timelogs.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
