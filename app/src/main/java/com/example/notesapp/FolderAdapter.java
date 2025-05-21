package com.example.notesapp;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Import for AlertDialog

import java.util.ArrayList;

public class FolderAdapter extends ArrayAdapter<Folder> {

    // Define the interface for the callback
    public interface OnFolderRenameListener {
        void onFolderRenameRequested(int folderId, String currentName);
    }

    private Activity context;
    private ArrayList<Folder> Items;
    private NoteDBHelper dbHelper;
    private OnFolderRenameListener renameListener;

    // Constructor: Now accepts the OnFolderRenameListener and corrects dbHelper assignment
    public FolderAdapter(Activity context, ArrayList<Folder> Items, NoteDBHelper dbHelperParam, OnFolderRenameListener listener) {
        super(context, R.layout.item_folder, Items);
        this.context = context;
        this.Items = Items;
        this.dbHelper = dbHelperParam; // Corrected: Assign the parameter to the member variable
        this.renameListener = listener; // Store the listener
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View v = inflater.inflate(R.layout.item_folder, null, true);

        // Assuming these IDs exist in item_folder.xml
        ImageView iv_folder = v.findViewById(R.id.ivFolder);
        TextView tv_title = v.findViewById(R.id.tvTitle); // Assuming this is the TextView for folder name
        ImageView iv_delete = v.findViewById(R.id.ivDelete);
        ImageView iv_open = v.findViewById(R.id.ivOpen);
        // You might want an explicit rename icon, or just use the title TextView click for rename
        // ImageView iv_rename = v.findViewById(R.id.ivRename); // If you have a separate rename icon

        Folder current_object = Items.get(position);

        tv_title.setText("" + current_object.getName());

        // --- NEW CODE FOR RENAMING THE TITLE (by clicking the TextView) ---
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (renameListener != null) {
                    // Call the callback method on the listener (which will be your FolderActivity)
                    renameListener.onFolderRenameRequested(current_object.getId(), current_object.getName());
                }
            }
        });
        // --- END NEW CODE ---


        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the correct position. Using getPositionForView is more reliable than 'position' directly from getView
                int adapterPosition = getAdapterPosition(view); // Use a helper to get actual position
                if (adapterPosition != ListView.INVALID_POSITION) {
                    Folder deletedFolder = Items.get(adapterPosition);
                    int deletedFolderId = deletedFolder.getId();
                    String deletedFolderName = deletedFolder.getName();

                    // Show confirmation dialog before deleting (recommended)
                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete Folder")
                            .setMessage("Are you sure you want to delete folder \"" + deletedFolderName + "\"? Notes in this folder will become unfiled.")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                // Perform the actual deletion from the database
                                dbHelper.deleteFolder(deletedFolderId);

                                // Remove from adapter list and update UI
                                Items.remove(adapterPosition);
                                notifyDataSetChanged();

                                Toast.makeText(getContext(), "Folder '" + deletedFolderName + "' deleted", Toast.LENGTH_SHORT).show();

                                // Optional: You might want to reload notes in NoteActivity if it's open,
                                // as notes inside this folder are now unfiled.
                                // This would typically involve a broadcast or another callback mechanism
                                // if NoteActivity needs to be immediately aware.
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }

            // Helper method to get the correct adapter position for a clicked view
            private int getAdapterPosition(View view) {
                // Traverse up the view hierarchy to find the ListView item (the row)
                View listItem = (View) view.getParent(); // This might need to be adjusted based on item_folder.xml structure
                while (listItem != null && !(listItem.getParent() instanceof ListView)) {
                    listItem = (View) listItem.getParent();
                }
                if (listItem != null && listItem.getParent() instanceof ListView) {
                    return ((ListView) listItem.getParent()).getPositionForView(listItem);
                }
                return -1;
            }
        });

        // If you had a separate rename icon, its listener would go here:
        /*
        if (iv_rename != null) {
            iv_rename.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition(v);
                if (adapterPosition != ListView.INVALID_POSITION && renameListener != null) {
                    Folder folderToRename = Items.get(adapterPosition);
                    renameListener.onFolderRenameRequested(folderToRename.getId(), folderToRename.getName());
                }
            });
        }
        */
        iv_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Opening folder: " + current_object.getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, NoteActivity.class);
                intent.putExtra("folderId", current_object.getId());
                intent.putExtra("folderName", current_object.getName());
                context.startActivity(intent);
            }
        });

        return v;
    }
}