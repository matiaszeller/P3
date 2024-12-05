package com.p3.noteModal;

import com.p3.networking.ServerApi;
import org.json.JSONObject;

import java.net.http.HttpResponse;

public class NoteModalDAO {

    private final ServerApi api = new ServerApi();

    public void postNewNote(JSONObject note) {
        String url = "note/addNewNote";

        try {
            api.post(url, null, note.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSenderName(int userId){
        String url = "user/nameById/" + userId;

        HttpResponse response = api.get(url, null, true);

        return (String) response.body();
    }

}
