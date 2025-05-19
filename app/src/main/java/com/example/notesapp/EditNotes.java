package com.example.notesapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import jp.wasabeef.richeditor.RichEditor;

public class EditNotes extends AppCompatActivity {
    private Note note;

    Button btn_toggleEdit;
    TextView tv_title;
    NoteDBHelper dbHelper;

    private RichEditor mEditor;
    private TextView mPreview;

    private boolean isTextColorBlack = true;
    private boolean isEditing = true;
    private int noteId = -1; // Initialize with -1 for new notes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes);

        mEditor = findViewById(R.id.editor);
        mPreview = findViewById(R.id.preview);
        tv_title = findViewById(R.id.tvTitle);
        btn_toggleEdit = findViewById(R.id.btnToggleEdit);

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId", -1);
        dbHelper = new NoteDBHelper(this);
        note = dbHelper.getNoteById(noteId);

        if (note != null) {
            tv_title.setText(note.getTitle());
            mEditor.setHtml(note.getContent());
        } else {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupEditor();
        setupToolbarButtons();


    }

    private void setupEditor() {
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(22);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPadding(10, 10, 10, 10);
        if (note.getContent() != null && !note.getContent().isEmpty()) {
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

        // Assuming you have a folderId, otherwise pass a default or store it in note
        int folderId = note != null ? note.getFolderId() : 0; // adjust if necessary

        // Update note in DB
        dbHelper.updateNote(noteId, updatedTitle, updatedContent, folderId);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("noteId", noteId);
        resultIntent.putExtra("noteTitle", updatedTitle);
        resultIntent.putExtra("noteContent", updatedContent);

//        setResult(RESULT_OK, resultIntent); useless thing gemini put
        finish();
    }




}