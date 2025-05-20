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

// 1. Implement the interface
public class NoteActivity extends AppCompatActivity implements NoteAdapter.OnNoteRenameListener {
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
        // 2. Pass 'this' (the Activity) as the listener to the adapter
        myAdapter = new NoteAdapter(this, notes, dbHelper, this); // <-- Changed constructor call
        lv_note.setAdapter(myAdapter);

        btnCreateNote.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            builder.setTitle("Enter Note Name");

            final EditText input = new EditText(NoteActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Create", (dialog, which) -> {
                String noteTitle = input.getText().toString().trim();
                if (!noteTitle.isEmpty()) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK && data != null) {
            int updatedNoteId = data.getIntExtra("noteId", -1);
            String updatedTitle = data.getStringExtra("noteTitle");
            String updatedContent = data.getStringExtra("noteContent");

            // You'll need to fetch the existing note's folderId to avoid setting it to 0
            // if it's not being passed back from EditNotes
            Note existingNote = dbHelper.getNote(updatedNoteId); // You'll need to implement this in dbHelper
            if (existingNote != null) {
                dbHelper.updateNote(updatedNoteId, updatedTitle, updatedContent, existingNote.getFolderId());
            } else {
                // Handle case where note is not found
            }
        }
    }

    public void loadNotesFromDB() {
        notes.clear();
        notes.addAll(dbHelper.getAllNotes());
    }

    // --- NEW METHOD TO HANDLE NOTE RENAME REQUEST ---
    @Override
    public void onNoteRenameRequested(int noteId, String currentTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Note");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentTitle); // Pre-fill with current title
        input.setSelection(currentTitle.length()); // Place cursor at the end for easy editing
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty() && !newTitle.equals(currentTitle)) {
                // To update just the title, you need the existing content and folderId
                // You'll need a method in NoteDBHelper like getNote(id) to fetch the full object.
                Note noteToUpdate = dbHelper.getNote(noteId); // <-- Requires dbHelper.getNote() method
                if (noteToUpdate != null) {
                    // Update the database with the new title, keeping content and folderId same
                    dbHelper.updateNote(noteId, newTitle, noteToUpdate.getContent(), noteToUpdate.getFolderId());
                    loadNotesFromDB(); // Reload all notes from DB
                    myAdapter.notifyDataSetChanged(); // Notify adapter to refresh list
                    Toast.makeText(this, "Note renamed to '" + newTitle + "'", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error: Note not found for renaming.", Toast.LENGTH_SHORT).show();
                }
            } else if (newTitle.isEmpty()) {
                Toast.makeText(this, "Note title cannot be empty", Toast.LENGTH_SHORT).show();
            } else { // Title was not changed
                Toast.makeText(this, "Note title not changed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    // --- END NEW METHOD ---
}