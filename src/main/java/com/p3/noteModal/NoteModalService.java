package com.p3.noteModal;

import org.json.JSONObject;

public class NoteModalService {

    NoteModalDAO noteModalDAO = new NoteModalDAO();

    public void postNewNote(JSONObject note) {
        noteModalDAO.postNewNote(note);
    }

    public String getSenderName(int userId){
        return noteModalDAO.getSenderName(userId);
    }

}
