package com.example.notesapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import jp.wasabeef.richeditor.RichEditor;

public class EditNotes extends AppCompatActivity {

    Button btn_toggleEdit;

    private RichEditor mEditor;
    private TextView mPreview;

    private boolean isTextColorBlack = true; //the color is black first
    private boolean isEditing = true; //the editor is in edit mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes);

        mEditor = findViewById(R.id.editor);
        mPreview = findViewById(R.id.preview);

        setupEditor();
        setupToolbarButtons();
    }

    private void setupEditor() {
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(22);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder("Insert text here...");
        mEditor.setOnTextChangeListener(text -> mPreview.setText(text));
        mEditor.setInputEnabled(true); //edit mode is on by default
    }

    private void setupToolbarButtons() {
        findViewById(R.id.action_undo).setOnClickListener(v -> mEditor.undo());
        findViewById(R.id.action_redo).setOnClickListener(v -> mEditor.redo());
        findViewById(R.id.action_bold).setOnClickListener(v -> mEditor.setBold());
        findViewById(R.id.action_italic).setOnClickListener(v -> mEditor.setItalic());
        findViewById(R.id.action_underline).setOnClickListener(v -> mEditor.setUnderline());
        findViewById(R.id.action_heading1).setOnClickListener(v -> mEditor.setHeading(1));
        findViewById(R.id.action_heading2).setOnClickListener(v -> mEditor.setHeading(2));
        findViewById(R.id.action_indent).setOnClickListener(v -> mEditor.setIndent());
        findViewById(R.id.action_outdent).setOnClickListener(v -> mEditor.setOutdent());
        findViewById(R.id.action_align_left).setOnClickListener(v -> mEditor.setAlignLeft());
        findViewById(R.id.action_align_center).setOnClickListener(v -> mEditor.setAlignCenter());
        findViewById(R.id.action_align_right).setOnClickListener(v -> mEditor.setAlignRight());
        findViewById(R.id.action_insert_bullets).setOnClickListener(v -> mEditor.setBullets());
        findViewById(R.id.action_insert_numbers).setOnClickListener(v -> mEditor.setNumbers());
        findViewById(R.id.action_insert_checkbox).setOnClickListener(v -> mEditor.insertTodo());

        findViewById(R.id.action_txt_color).setOnClickListener(v -> {
            //toggles between color black and color red lang haha
            if (isTextColorBlack) {
                mEditor.setTextColor(Color.RED);
            } else {
                mEditor.setTextColor(Color.BLACK);
            }
            isTextColorBlack = !isTextColorBlack;
        });

        btn_toggleEdit = findViewById(R.id.btnToggleEdit);
        btn_toggleEdit.setOnClickListener(v -> {
            if (isEditing) {
                // Save and switch to reading mode
                String currentContent = mEditor.getHtml(); // get HTML content if needed
                // TODO: Save content (e.g., to database or file)

                mEditor.setInputEnabled(false); // disable editing
                btn_toggleEdit.setText("Edit");  // change button text
                isEditing = false;
            } else {
                // Switch back to editing mode
                mEditor.setInputEnabled(true);
                btn_toggleEdit.setText("Save");
                isEditing = true;
            }
        });

    }
}
