package com.example.notesapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType; // Import for InputType
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // Import for EditText
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // Import for AlertDialog
import androidx.appcompat.app.AppCompatActivity;

import jp.wasabeef.richeditor.RichEditor;

public class EditNotes extends AppCompatActivity {
    private Note note; // The note object being edited

    Button btn_toggleEdit, btn_back;
    TextView tv_title;
    NoteDBHelper dbHelper;

    private RichEditor mEditor;
    private TextView mPreview;

    private boolean isTextColorBlack = true;
    private boolean isEditing = true;
    private int noteId = -1; // Initialize with -1, will be set from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes);

        mEditor = findViewById(R.id.editor);
        mPreview = findViewById(R.id.preview);
        tv_title = findViewById(R.id.tvTitle);
        btn_toggleEdit = findViewById(R.id.btnToggleEdit);
        btn_back = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId", -1); // Get noteId from intent
        dbHelper = new NoteDBHelper(this); // Initialize DB Helper

        // Fetch the full Note object from the database using its ID
        note = dbHelper.getNote(noteId);

        if (note != null) {
            tv_title.setText(note.getTitle());
            mEditor.setHtml(note.getContent());
        } else {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if note not found
            return; // Exit onCreate
        }

        // --- Make tv_title clickable for renaming ---
        tv_title.setOnClickListener(v -> showRenameDialog());
        // --- End New ---

        setupEditor();
        setupToolbarButtons();

        btn_back.setOnClickListener(v -> {
            saveNote();
            finish();
        });


    }

    // Method to display the rename dialog
    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Note");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(tv_title.getText().toString()); // Pre-fill with current title
        input.setSelection(tv_title.getText().length()); // Place cursor at the end
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            String currentTitle = note.getTitle();

            if (!newTitle.isEmpty() && !newTitle.equals(currentTitle)) {
                // Update the 'note' object in memory with the new title
                note.setTitle(newTitle);

                // Update the database using the updated 'note' object's details
                dbHelper.updateNote(note.getId(), note.getTitle(), note.getContent(), note.getFolderId());

                // Update the TextView on the screen
                tv_title.setText(newTitle);
                Toast.makeText(this, "Note renamed to '" + newTitle + "'", Toast.LENGTH_SHORT).show();
            } else if (newTitle.isEmpty()) {
                Toast.makeText(this, "Note title cannot be empty", Toast.LENGTH_SHORT).show();
            } else { // Title was not changed
                Toast.makeText(this, "Note title not changed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void setupEditor() {
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(22);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPadding(10, 10, 10, 10);
        // Ensure note.getContent() is not null before setting HTML
        if (note != null && note.getContent() != null && !note.getContent().isEmpty()) {
            mEditor.setHtml(note.getContent());
        } else {
            mEditor.setPlaceholder("Insert text here...");
        }
        mEditor.setOnTextChangeListener(text -> mPreview.setText(text));
        mEditor.setInputEnabled(true);
    }

    private void setupToolbarButtons() {
        findViewById(R.id.action_undo).setOnClickListener(v -> mEditor.undo());
        findViewById(R.id.action_redo).setOnClickListener(v -> mEditor.redo());
        findViewById(R.id.action_bold).setOnClickListener(v -> mEditor.setBold());
        findViewById(R.id.action_italic).setOnClickListener(v -> mEditor.setItalic());
        findViewById(R.id.action_underline).setOnClickListener(v -> mEditor.setUnderline());
        findViewById(R.id.action_heading1).setOnClickListener(v -> mEditor.setHeading(1));
        findViewById(R.id.action_heading2).setOnClickListener(v -> mEditor.setHeading(2));
        findViewById(R.id.action_indent).setOnClickListener(v -> mEditor.setIndent());
        findViewById(R.id.action_outdent).setOnClickListener(v -> mEditor.setOutdent());
        findViewById(R.id.action_align_left).setOnClickListener(v -> mEditor.setAlignLeft());
        findViewById(R.id.action_align_center).setOnClickListener(v -> mEditor.setAlignCenter());
        findViewById(R.id.action_align_right).setOnClickListener(v -> mEditor.setAlignRight());
        findViewById(R.id.action_insert_bullets).setOnClickListener(v -> mEditor.setBullets());
        findViewById(R.id.action_insert_numbers).setOnClickListener(v -> mEditor.setNumbers());
        findViewById(R.id.action_insert_checkbox).setOnClickListener(v -> mEditor.insertTodo());

        findViewById(R.id.action_txt_color).setOnClickListener(v -> {
            isTextColorBlack = !isTextColorBlack;
            mEditor.setTextColor(isTextColorBlack ? Color.BLACK : Color.RED);
        });

        btn_toggleEdit.setOnClickListener(v -> {
            isEditing = !isEditing;
            mEditor.setInputEnabled(isEditing);
            btn_toggleEdit.setText(isEditing ? "Save" : "Edit");
            // Only save when transitioning from edit to read mode
            if (!isEditing) {
                saveNote();
            }
            // If transitioning from read to edit, don't save yet.
        });
    }

    private void saveNote() {
        String updatedContent = mEditor.getHtml();
        String updatedTitle = tv_title.getText().toString().trim();

        if (note != null) { // Ensure note object exists
            // Update the 'note' object in memory first
            note.setTitle(updatedTitle);
            note.setContent(updatedContent);
            // folderId remains the same unless you add UI to change it

            // Then update the database using the updated note object's properties
            dbHelper.updateNote(note.getId(), note.getTitle(), note.getContent(), note.getFolderId());

            // You can set a result here if you want to pass data back explicitly,
            // but onResume() in NoteActivity handles the refresh effectively.
            // Intent resultIntent = new Intent();
            // resultIntent.putExtra("noteId", note.getId());
            // resultIntent.putExtra("noteTitle", note.getTitle());
            // resultIntent.putExtra("noteContent", note.getContent());
            // setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: Cannot save note.", Toast.LENGTH_SHORT).show();
        }
        // finish(); // Consider if you want to finish the activity immediately after saving
    }
}