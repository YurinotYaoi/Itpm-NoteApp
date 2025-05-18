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
    SQLiteDatabase db;
    Cursor c;

    ArrayList<Note> notes = new ArrayList<>();
    ListView lv_note;
    NoteAdapter myAdapter;
    Button btnCreateNote;

    private static final int EDIT_NOTE_REQUEST = 1; // Request code for starting EditNotes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);

        db = openOrCreateDatabase("NotesDB", MODE_PRIVATE, null);
        String sql = "CREATE TABLE IF NOT EXISTS Notes(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, folderId INTEGER)";
        db.execSQL(sql);

        lv_note = findViewById(R.id.lvNote);
        btnCreateNote = findViewById(R.id.btnCreateNote);

        // Initial load of notes
        loadNotesFromDB();
        myAdapter = new NoteAdapter(NoteActivity.this, notes);
        lv_note.setAdapter(myAdapter);


        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setTitle("Enter Note Name");

                final EditText input = new EditText(NoteActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Create", (dialog, which) -> {
                    String noteTitle = input.getText().toString().trim();
                    if (!noteTitle.isEmpty()) {
                        addNoteToDB(noteTitle, null, 0);
                        loadNotesFromDB(); // Reload after adding
                        myAdapter.notifyDataSetChanged();
                        Toast.makeText(NoteActivity.this, "Note '" + noteTitle + "' created", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NoteActivity.this, "Note name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload notes from the database every time the activity resumes
        loadNotesFromDB();
        myAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_NOTE_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                int updatedNoteId = data.getIntExtra("noteId", -1);
                String updatedTitle = data.getStringExtra("noteTitle");
                String updatedContent = data.getStringExtra("noteContent");

                if (updatedNoteId != -1) {
                    updateNoteInDB(updatedNoteId, updatedTitle, updatedContent, 0);
                    // No need to call loadNotesFromDB and notifyDataSetChanged here
                    // because onResume will handle it.
                }
            }
        }
    }

    // Method to load notes from database
    private void loadNotesFromDB() {
        notes.clear();
        c = db.rawQuery("SELECT * FROM Notes", null);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                String content = c.getString(c.getColumnIndexOrThrow("content"));
                int folderId = c.getInt(c.getColumnIndexOrThrow("folderId"));
                notes.add(new Note(id, title, content, folderId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Method to add a note to the database
    private void addNoteToDB(String title, String content, int folderId) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("folderId", folderId);
        long id = db.insert("Notes", null, values);
        notes.add(new Note((int) id, title, content, folderId));
    }

    // Method to update a note in the database
    public void updateNoteInDB(int id, String title, String content, int folderId) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("folderId", folderId);
        db.update("Notes", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // Method to delete a note from the database
    public void deleteNoteFromDB(int id) {
        db.delete("Notes", "id = ?", new String[]{String.valueOf(id)});
    }
}