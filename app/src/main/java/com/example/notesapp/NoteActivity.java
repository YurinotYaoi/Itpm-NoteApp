package com.example.notesapp;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class NoteActivity extends AppCompatActivity {

    ArrayList<Note> notes = new ArrayList<>(); // Initialize here
    ListView lv_note;
    NoteAdapter myAdapter; // Declare the adapter as a member variable
    Button btnCreateNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);

        lv_note = findViewById(R.id.lvNote);
        btnCreateNote = findViewById(R.id.btnCreateNote);

        // Add the initial folder
        notes.add(new Note(1, "Formulas", null, 0));

        // Initialize the adapter with the (now populated) folders list
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
                        notes.add(new Note(notes.size() + 1, noteTitle, null, 0));
                        myAdapter.notifyDataSetChanged(); // Tell the adapter the data has changed
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
}