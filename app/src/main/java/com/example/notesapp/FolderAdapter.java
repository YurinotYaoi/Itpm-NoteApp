package com.example.notesapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast; // Make sure this import is present

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class FolderAdapter extends ArrayAdapter<Folder> {

    private Activity context;
    private ArrayList<Folder> Items;

    public FolderAdapter(Activity context, ArrayList<Folder> Items) {
        super(context, R.layout.item_folder, Items);
        this.context = context;
        this.Items = Items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View v = inflater.inflate(R.layout.item_folder, null, true);

        ImageView iv_fruit = v.findViewById(R.id.ivFolder); // Potentially the folder icon
        TextView tv_title = v.findViewById(R.id.tvTitle);
        ImageView iv_delete = v.findViewById(R.id.ivDelete);

        Folder current_object = Items.get(position);

        tv_title.setText("" + current_object.getName());

        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the correct position here, as 'position' in getView might change
                int adapterPosition = getAdapterPosition(view);
                if (adapterPosition != ListView.INVALID_POSITION) {
                    String deletedFolderName = Items.get(adapterPosition).getName();
                    Items.remove(adapterPosition);
                    notifyDataSetChanged();
                    Toast.makeText(getContext(), "Folder '" + deletedFolderName + "' deleted", Toast.LENGTH_SHORT).show();
                    // You might want to implement logic to actually delete the folder
                    // from your data storage (e.g., database, file system) here.
                }
            }

            // Helper method to get the correct adapter position
            private int getAdapterPosition(View view) {
                if (parent instanceof ListView) {
                    return ((ListView) parent).getPositionForView(view);
                }
                return -1; // Or handle other AdapterView types if needed
            }
        });

        return v;
    }
}