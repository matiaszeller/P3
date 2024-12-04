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
import javafx.scene.input.KeyCode;
import java.time.LocalDate;

public class NoteModalController {
    private JSONArray notesToAdd = new JSONArray();
    private final VBox noteContainer = new VBox(10);
    private LocalDate date;

    @FXML
    private VBox modalMainContainer;
    @FXML
    private ScrollPane modalNoteContentContainer;
    @FXML
    private Label modalMainLabel;
    @FXML
    private HBox modalButtonContainer;
    @FXML
    private Button modalConfirmButton;
    @FXML
    private Button modalCancelButton;
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
        modalCancelButton.setOnAction(event -> {
            Stage stage = (Stage) modalCancelButton.getScene().getWindow();
            stage.close();
        });

        // An array with input comments are loaded, on stage close, return array and post from main controller
        modalConfirmButton.setOnAction(event -> {
            Stage stage = (Stage) modalConfirmButton.getScene().getWindow();
            stage.close();
        });

        Scene scene = modalMainContainer.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                System.out.println(event.getCode());
                if(event.getCode() == KeyCode.ENTER) {
                    if(!event.isShiftDown()) {
                        event.consume();
                        appendNoteToArray();
                    }
                }
            });
        }

        inputTextArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                appendNoteToArray();
            }
        });

        inputTextButton.setOnAction(event -> {appendNoteToArray();});
    }

    public JSONArray generateModal(JSONArray dayNotes) {
        JSONObject firstDayNote = dayNotes.getJSONObject(0);
        date = LocalDate.parse(firstDayNote.getString("note_date"));
        modalMainLabel.setText(String.format("Tilføj noter til d. %s", date));

        inputTextArea.setWrapText(true);

        noteContainer.getStyleClass().add("notesScrollPaneBoxContainer");

        for (int i = 0; i < dayNotes.length(); i++) {
            JSONObject note = dayNotes.getJSONObject(i);
            addNoteToContainer(note);
        }

        modalNoteContentContainer.setContent(noteContainer);

        // Tror de stopper fra at scrolle horizontalt men ikke verticalt but idk
        modalNoteContentContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        modalNoteContentContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Return inputNotes
        if(!notesToAdd.isEmpty()) {
            return notesToAdd;
        }
        return null;
    }

    private void addNoteToContainer(JSONObject note) {
        VBox noteVBox = new VBox(5);
        noteVBox.setPadding(new javafx.geometry.Insets(10));

        Label noteSenderLabel = new Label("Writer ID: " + note.getInt("writer_id"));
        noteSenderLabel.getStyleClass().add("noteSenderLabel");
        noteVBox.getChildren().add(noteSenderLabel);

        Label noteTextLabel = new Label(note.getString("written_note"));
        noteTextLabel.setWrapText(true);
        noteVBox.getChildren().add(noteTextLabel);

        // Create an HBox to align the noteVBox within the noteContainer
        HBox noteAlignmentBox = new HBox();
        noteAlignmentBox.setPrefWidth(noteContainer.getWidth());

        // Align note to left or right based on recipient_id
        int recipientId = note.getInt("recipient_id");
        if (recipientId == Session.getCurrentUserId()) {
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

    private void appendNoteToArray() {
        String noteText = inputTextArea.getText();

        if(!noteText.isEmpty()) {
            JSONObject newNote = new JSONObject();
            newNote.put("writer_id", Session.getCurrentUserId());
            newNote.put("recipient_id", 1); // TODO brian hardcoded for nu, men lav get request til at skaffe mulige managers
            newNote.put("written_note", noteText);
            newNote.put("full_name", Session.getCurrentUserFullName());
            newNote.put("note_date", date);
            notesToAdd.put(newNote);

            addNoteToContainer(newNote);

            inputTextArea.clear();
        }
    }

}
