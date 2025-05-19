package com.example.notesapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class NoteActivity extends AppCompatActivity {
    NoteDBHelper dbHelper;
    ArrayList<Note> notes = new ArrayList<>();
    ListView lv_note;
    NoteAdapter myAdapter;
    Button btnCreateNote;

    private static final int EDIT_NOTE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);

        dbHelper = new NoteDBHelper(this);

        lv_note = findViewById(R.id.lvNote);
        btnCreateNote = findViewById(R.id.btnCreateNote);

        notes = dbHelper.getAllNotes(); // Initial load
        myAdapter = new NoteAdapter(this, notes, dbHelper);
        lv_note.setAdapter(myAdapter);

        btnCreateNote.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            builder.setTitle("Enter Note Name");

            final EditText input = new EditText(NoteActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Create", (dialog, which) -> {
                String noteTitle = input.getText().toString().trim();
                if (!noteTitle.isEmpty()) { //currently folder Id is hardcoded to zero
                    long id = dbHelper.addNote(noteTitle, null, 0);
                    notes.add(new Note((int) id, noteTitle, null, 0));
                    myAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Note '" + noteTitle + "' created", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Note name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromDB();
        myAdapter.notifyDataSetChanged();
    }

    //useless thing gemini put. I'll see this later
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK && data != null) {
//            int updatedNoteId = data.getIntExtra("noteId", -1);
//            String updatedTitle = data.getStringExtra("noteTitle");
//            String updatedContent = data.getStringExtra("noteContent");
//
//            if (updatedNoteId != -1) {
//                dbHelper.updateNote(updatedNoteId, updatedTitle, updatedContent, 0);
//            }
//        }
//    }

    public void loadNotesFromDB() {
        notes.clear();
        notes.addAll(dbHelper.getAllNotes());
    }

}