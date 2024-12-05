package com.p3.timelogEditModal;

import org.json.JSONArray;

public class TimelogEditModalService {

    TimelogEditModalDAO dao = new TimelogEditModalDAO();

    public void postUpdatedTimelogs(JSONArray timelogs) {
        dao.postUpdatedTimelogs(timelogs);
    }
}
