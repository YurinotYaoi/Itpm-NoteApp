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
    SQLiteDatabase db;
    Cursor c;

    Button btn_toggleEdit;
    TextView tv_title;

    private RichEditor mEditor;
    private TextView mPreview;

    private boolean isTextColorBlack = true;
    private boolean isEditing = true;
    private int noteId = -1; // Initialize with -1 for new notes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes);

        db = openOrCreateDatabase("NotesDB", MODE_PRIVATE, null);
        String sql = "CREATE TABLE IF NOT EXISTS Notes(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, folderId INTEGER)";
        db.execSQL(sql);

        mEditor = findViewById(R.id.editor);
        mPreview = findViewById(R.id.preview);
        tv_title = findViewById(R.id.tvTitle);
        btn_toggleEdit = findViewById(R.id.btnToggleEdit);

        setupEditor();
        setupToolbarButtons();

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId", -1);
        String noteTitle = intent.getStringExtra("noteTitle");
        String noteContent = intent.getStringExtra("noteContent");

        if (noteTitle != null) {
            tv_title.setText(noteTitle);
        } else {
            tv_title.setText("New Note"); // Default title for new notes
        }

        if (noteContent != null) {
            mEditor.setHtml(noteContent);
        } else {
            mEditor.setPlaceholder("Start typing your note...");
        }
    }

    private void setupEditor() {
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(22);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder("Insert text here...");
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

        Intent resultIntent = new Intent();
        resultIntent.putExtra("noteId", noteId);
        resultIntent.putExtra("noteTitle", updatedTitle);
        resultIntent.putExtra("noteContent", updatedContent);

        setResult(RESULT_OK, resultIntent);
        finish(); // Go back to NoteActivity
    }


}