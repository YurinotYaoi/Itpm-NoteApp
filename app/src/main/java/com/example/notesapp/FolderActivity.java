package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FolderActivity extends AppCompatActivity implements FolderAdapter.OnFolderRenameListener { // IMP: Implement the interface

    NoteDBHelper dbHelper;
    ArrayList<Folder> folders = new ArrayList<>();
    ListView lv_folder;
    FolderAdapter myAdapter;
    Button btnCreateFolder;

    // This constant isn't directly used here for FolderActivity's result handling,
    // but keep it if it's used elsewhere, otherwise consider removing.
    // private static final int EDIT_NOTE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_folder);

        dbHelper = new NoteDBHelper(this);

        lv_folder = findViewById(R.id.lvFolder);
        btnCreateFolder = findViewById(R.id.btnCreateFolder);

        // Initialize the adapter with the (now populated) folders list
        folders = dbHelper.getAllFolders();
        // IMP: Pass 'this' as the listener to the FolderAdapter
        myAdapter = new FolderAdapter(FolderActivity.this, folders, dbHelper, this);
        lv_folder.setAdapter(myAdapter);

        // Set up item click listener for the ListView (if you want to open notes from folder)
        // This is usually where you'd navigate to NoteActivity filtered by folderId
//        lv_folder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Folder clickedFolder = folders.get(position);
//                Toast.makeText(FolderActivity.this, "Opening folder: " + clickedFolder.getName(), Toast.LENGTH_SHORT).show();
//                // TODO: Implement navigation to NoteActivity, passing clickedFolder.getId()
//                // Example:
//                // Intent intent = new Intent(FolderActivity.this, NoteActivity.class);
//                // intent.putExtra("folderId", clickedFolder.getId());
//                // startActivity(intent);
//            }
//        });


        btnCreateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FolderActivity.this);
                builder.setTitle("Enter Folder Name");

                final EditText input = new EditText(FolderActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Create", (dialog, which) -> {
                    String folderName = input.getText().toString().trim();
                    if (!folderName.isEmpty()) {
                        long id = dbHelper.addFolder(folderName);
                        if (id != -1) { // Check if insertion was successful
                            folders.add(new Folder((int)id, folderName));
                            myAdapter.notifyDataSetChanged(); // Tell the adapter the data has changed
                            Toast.makeText(FolderActivity.this, "Folder '" + folderName + "' created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FolderActivity.this, "Error creating folder. Name might already exist.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(FolderActivity.this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
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
        loadFoldersFromDB(); // Reloads all folders
        myAdapter.notifyDataSetChanged(); // Updates the UI
    }

    public void loadFoldersFromDB() {
        folders.clear();
        folders.addAll(dbHelper.getAllFolders());
    }

    // IMP: Implement the onFolderRenameRequested method from the interface
    @Override
    public void onFolderRenameRequested(int folderId, String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Folder");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentName); // Pre-fill with current name
        input.setSelection(currentName.length()); // Place cursor at the end for easy editing
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(currentName)) {
                dbHelper.updateFolder(folderId, newName); // Update the folder in the database
                loadFoldersFromDB(); // Reload all folders from DB
                myAdapter.notifyDataSetChanged(); // Notify adapter to refresh list
                Toast.makeText(this, "Folder renamed to '" + newName + "'", Toast.LENGTH_SHORT).show();
            } else if (newName.isEmpty()) {
                Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
            } else { // Name was not changed
                Toast.makeText(this, "Folder name not changed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // This method is not used in the provided code, consider removing it if it's unused
    public void GoToLogin(View view) {
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

    public void GoToHome(View view) {
        Intent i = new Intent(this, MainActivity.class);
    }
}