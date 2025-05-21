package com.example.notesapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class NoteAdapter extends ArrayAdapter<Note> {

    // Define the interface for the callback (this is already in your code, good!)
    public interface OnNoteRenameListener {
        void onNoteRenameRequested(int noteId, String currentTitle);
    }

    private Activity context;
    private ArrayList<Note> Items;
    private NoteDBHelper dbHelper;
    private OnNoteRenameListener renameListener; // <-- New: Field to store the listener

    // Constructor: Now accepts the OnNoteRenameListener
    public NoteAdapter(Activity context, ArrayList<Note> Items, NoteDBHelper dbHelper, OnNoteRenameListener listener) {
        super(context, R.layout.item_note, Items);
        this.context = context;
        this.Items = Items;
        this.dbHelper = dbHelper;
        this.renameListener = listener; // <-- New: Store the listener
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View v = inflater.inflate(R.layout.item_note, null, true);

        // ImageView iv_fruit = v.findViewById(R.id.ivNote); // Potentially the folder icon
        TextView tv_title = v.findViewById(R.id.tvTitle);
        ImageView iv_delete = v.findViewById(R.id.ivDelete);
        ImageView iv_open = v.findViewById(R.id.ivOpen);

        Note current_object = Items.get(position);

        tv_title.setText("" + current_object.getTitle());
        final int currentPosition = position; // Keep this for delete/open

        // --- NEW CODE FOR RENAMING THE TITLE ---
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (renameListener != null) {
                    // Call the callback method on the listener (which will be your NoteActivity)
                    // Pass the ID and current title of the clicked note
                    renameListener.onNoteRenameRequested(current_object.getId(), current_object.getTitle());
                }
            }
        });
        // --- END NEW CODE ---

        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPosition != ListView.INVALID_POSITION && currentPosition < Items.size()) {
                    Note deletedNote = Items.get(currentPosition);
                    int deletedNoteId = deletedNote.getId();
                    String deletedNoteTitle = deletedNote.getTitle();

                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete Note")
                            .setMessage("Are you sure you want to delete \"" + deletedNoteTitle + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                dbHelper.deleteNote(deletedNoteId);

                                // Remove from adapter list and update UI
                                Items.remove(currentPosition);
                                notifyDataSetChanged();

                                Toast.makeText(getContext(), "Note \"" + deletedNoteTitle + "\" deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }
        });


        iv_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Opening note: " + current_object.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, EditNotes.class);
                intent.putExtra("noteId", current_object.getId());
                intent.putExtra("noteTitle", current_object.getTitle());
                intent.putExtra("noteContent", current_object.getContent());
                intent.putExtra("folderId", current_object.getFolderId());
                context.startActivity(intent);
            }
        });

        return v;
    }
}