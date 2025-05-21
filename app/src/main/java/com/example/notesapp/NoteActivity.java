package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView; // Make sure this is imported if you're using setOnItemClickListener
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

    private int currentFolderId = 0; // New: To store the ID of the folder being viewed. Default to 0 (All Notes/Unfiled)
    private String currentFolderName = "All Notes"; // New: To store the name of the folder being viewed

    private static final int EDIT_NOTE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);

        dbHelper = new NoteDBHelper(this);

        lv_note = findViewById(R.id.lvNote);
        btnCreateNote = findViewById(R.id.btnCreateNote);

        // --- NEW: Check for incoming folderId from Intent ---
        Intent intent = getIntent();
        if (intent.hasExtra("folderId")) {
            currentFolderId = intent.getIntExtra("folderId", 0); // Get the passed folderId
            currentFolderName = intent.getStringExtra("folderName"); // Get the passed folderName
            // Optionally update a TextView here to show the current folder name
            // For example: TextView folderTitle = findViewById(R.id.tvFolderTitle);
            // if (folderTitle != null) folderTitle.setText(currentFolderName);
        }
        // --- END NEW ---

        // Load notes based on the currentFolderId
        loadNotesFromDB(); // This method will now be updated to filter by folderId

        // 2. Pass 'this' (the Activity) as the listener to the adapter
        myAdapter = new NoteAdapter(this, notes, dbHelper, this);
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
                    // IMP: Use currentFolderId when adding a new note
                    long id = dbHelper.addNote(noteTitle, null, currentFolderId);
                    if (id != -1) {
                        notes.add(new Note((int) id, noteTitle, null, currentFolderId));
                        myAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Note '" + noteTitle + "' created in " + currentFolderName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error creating note.", Toast.LENGTH_SHORT).show();
                    }
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
        // Always reload notes specific to the current folder when resuming
        loadNotesFromDB();
        myAdapter.notifyDataSetChanged();
    }

    // --- MODIFIED: loadNotesFromDB to filter by folderId ---
    public void loadNotesFromDB() {
        notes.clear();
        if (currentFolderId == 0) { // Assuming 0 means 'All Notes' or 'Unfiled'
            notes.addAll(dbHelper.getAllNotes()); // Get all notes if viewing "All Notes"
        } else {
            // Get notes only for the current folder
            notes.addAll(dbHelper.getNotesByFolder(currentFolderId));
        }
    }
    // --- END MODIFIED ---

    // --- Existing onNoteRenameRequested method ---
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
                Note noteToUpdate = dbHelper.getNote(noteId); // Use getNoteById (already exists)
                if (noteToUpdate != null) {
                    // Update the database with the new title, keeping content and folderId same
                    dbHelper.updateNote(noteId, newTitle, noteToUpdate.getContent(), noteToUpdate.getFolderId());
                    loadNotesFromDB(); // Reload notes from DB (which will now respect currentFolderId)
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

    public void GoToFolder(View view) {
        Intent i = new Intent(this, FolderActivity.class);
        startActivity(i);
    }
    // --- End onNoteRenameRequested method ---
}