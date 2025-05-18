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

public class FolderActivity extends AppCompatActivity {

    ArrayList<Folder> folders = new ArrayList<>(); // Initialize here
    ListView lv_folder;
    FolderAdapter myAdapter; // Declare the adapter as a member variable
    Button btnCreateFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_folder);

        lv_folder = findViewById(R.id.lvFolder);
        btnCreateFolder = findViewById(R.id.btnCreateFolder);

        // Add the initial folder
        folders.add(new Folder(1, "Math"));

        // Initialize the adapter with the (now populated) folders list
        myAdapter = new FolderAdapter(FolderActivity.this, folders);
        lv_folder.setAdapter(myAdapter);

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
                        folders.add(new Folder(folders.size() + 1, folderName));
                        myAdapter.notifyDataSetChanged(); // Tell the adapter the data has changed
                        Toast.makeText(FolderActivity.this, "Folder '" + folderName + "' created", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FolderActivity.this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });
    }
}