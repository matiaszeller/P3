package com.p3.noteModal;

import com.p3.session.Session;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.scene.input.KeyEvent;

import java.time.LocalDate;
import java.util.Objects;

public class NoteModalController {
    NoteModalService noteModalService = new NoteModalService();
    private final VBox noteContainer = new VBox(10);
    private LocalDate date;
    private JSONArray dayNotes;
    private int userId;
    private int recipientId;

    @FXML
    private VBox modalMainContainer;
    @FXML
    private ScrollPane modalNoteContentContainer;
    @FXML
    private Label modalMainLabel;
    @FXML
    private HBox modalButtonContainer;
    @FXML
    private Button modalBackButton;
    @FXML
    private HBox noteInputContainer;
    @FXML
    private TextArea inputTextArea;
    @FXML
    private Button inputTextButton;

    @FXML
    public void initialize() {
        setActionHandlers();
    }

    private void setActionHandlers() {
        modalBackButton.setOnAction(event -> {
            Stage stage = (Stage) modalBackButton.getScene().getWindow();
            stage.close();
        });

        Scene scene = modalMainContainer.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if(event.getCode() == KeyCode.ENTER) {
                    if(!event.isShiftDown()) {
                        event.consume();
                        postNoteObject();
                    }
                }
            });
        }

        inputTextArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                postNoteObject();
            }
        });

        inputTextButton.setOnAction(event -> postNoteObject());
    }

    public void generateModal(JSONArray dayNotes, LocalDate date, int userId) {
        this.dayNotes = dayNotes;
        this.userId = userId;
        setDate(date);

        if(!dayNotes.isEmpty()) {
            if(Session.getCurrentUserId() == dayNotes.getJSONObject(0).getInt("writer_id")) {
                recipientId = dayNotes.getJSONObject(0).getInt("recipient_id");
            } else {
                recipientId = dayNotes.getJSONObject(0).getInt("writer_id");
            }
        } else if (Session.getRole() == "manager") {
            recipientId = userId;  // default for first manager
        } else {
            recipientId = 1;
        }


        modalMainLabel.setText(String.format("Tilføj noter til d. %s", date));

        inputTextArea.setWrapText(true);
        noteContainer.getStyleClass().add("notesScrollPaneBoxContainer");

        if(!dayNotes.isEmpty()) {
            for (int i = 0; i < dayNotes.length(); i++) {
                JSONObject note = dayNotes.getJSONObject(i);
                addNoteToContainer(note);
            }
        }

        modalNoteContentContainer.setContent(noteContainer);

        // Tror de stopper fra at scrolle horizontalt men ikke verticalt but idk
        modalNoteContentContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        modalNoteContentContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    }

    private void addNoteToContainer(JSONObject note) {
        int recipientId = note.getInt("recipient_id");
        int writerId = note.getInt("writer_id");
        boolean isSessionUserRecipient = recipientId == Session.getCurrentUserId();

        VBox noteVBox = new VBox(5);
        noteVBox.setPadding(new javafx.geometry.Insets(10));

        Label noteSenderLabel = new Label();

        if (isSessionUserRecipient) {
            noteSenderLabel.setText(noteModalService.getSenderName(writerId));
        } else {
            noteSenderLabel.setText(Session.getCurrentUserFullName());
        }
        noteSenderLabel.getStyleClass().add("noteSenderLabel");
        noteVBox.getChildren().add(noteSenderLabel);

        Label noteTextLabel = new Label(note.getString("written_note"));
        noteTextLabel.setWrapText(true);
        noteVBox.getChildren().add(noteTextLabel);

        // Create an HBox to align the noteVBox within the noteContainer
        HBox noteAlignmentBox = new HBox();
        noteAlignmentBox.setPrefWidth(noteContainer.getWidth());

        // Align note to left or right based on recipient_id
        if (isSessionUserRecipient) {
            // User is the recipient, align note to the left
            noteAlignmentBox.setAlignment(Pos.CENTER_LEFT);
            noteVBox.getStyleClass().add("noteUserIsRecipient");
        } else {
            // User is the sender, align note to the right
            noteAlignmentBox.setAlignment(Pos.CENTER_RIGHT);
            noteVBox.getStyleClass().add("noteUserIsSender");
        }

        noteAlignmentBox.getChildren().add(noteVBox);
        noteContainer.getChildren().add(noteAlignmentBox);
    }

    private void postNoteObject() {
        String noteText = inputTextArea.getText();


        // Create note object and post to server
        if(!noteText.isEmpty()) {
            JSONObject newNote = new JSONObject();
            newNote.put("writer_id", Session.getCurrentUserId());
            newNote.put("written_note", noteText);
            newNote.put("full_name", Session.getCurrentUserFullName());
            newNote.put("note_date", date);
            System.out.println(userId);
            System.out.println(Session.getRole());
            if(Objects.equals(Session.getRole(), "manager")) {
                newNote.put("recipient_id", userId);
            }
            else{
                newNote.put("recipient_id", recipientId);
            }

            System.out.println(newNote.toString());

            noteModalService.postNewNote(newNote);

            // Visual confirmation of note being added to server
            addNoteToContainer(newNote);

            // Clear for next input
            inputTextArea.clear();
        }
    }

    private void setDate(LocalDate date) {
        this.date = date;
    }
}
