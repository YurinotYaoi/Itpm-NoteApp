package com.example.notesapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class NoteDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "NotesDB";
    // IMPORTANT: Increment DB_VERSION to trigger onUpgrade when schema changes!
    private static final int DB_VERSION = 4;

    // Table Names
    private static final String TABLE_NOTES = "Notes";
    private static final String TABLE_FOLDERS = "Folders";

    // Notes Table Columns
    private static final String COLUMN_NOTE_ID = "id";
    private static final String COLUMN_NOTE_TITLE = "title";
    private static final String COLUMN_NOTE_CONTENT = "content";
    private static final String COLUMN_NOTE_FOLDER_ID = "folderId";

    // Folders Table Columns
    private static final String COLUMN_FOLDER_ID = "id";
    private static final String COLUMN_FOLDER_NAME = "name";


    public NoteDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Folders Table
        String CREATE_FOLDERS_TABLE = "CREATE TABLE " + TABLE_FOLDERS + "("
                + COLUMN_FOLDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FOLDER_NAME + " TEXT UNIQUE NOT NULL" // Folder names should be unique
                + ")";
        db.execSQL(CREATE_FOLDERS_TABLE);

        // Create Notes Table with Foreign Key
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTE_TITLE + " TEXT,"
                + COLUMN_NOTE_CONTENT + " TEXT,"
                + COLUMN_NOTE_FOLDER_ID + " INTEGER DEFAULT 0," // Default to folderId 0 for 'unfiled' notes
                + " FOREIGN KEY(" + COLUMN_NOTE_FOLDER_ID + ") REFERENCES " + TABLE_FOLDERS + "(" + COLUMN_FOLDER_ID + ") ON DELETE SET NULL"
                + ")";
        db.execSQL(CREATE_NOTES_TABLE);

        // Optional: Insert a default "Unfiled" or "All Notes" folder with ID 0
        // This is good practice if you default new notes to folderId 0
        ContentValues defaultFolderValues = new ContentValues();
        defaultFolderValues.put(COLUMN_FOLDER_ID, 0); // Explicitly set ID to 0
        defaultFolderValues.put(COLUMN_FOLDER_NAME, "All Notes"); // Or "Unfiled Notes"
        db.insert(TABLE_FOLDERS, null, defaultFolderValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // WARNING: This strategy deletes all existing data.
        // For production apps, you'd write ALTER TABLE statements for proper migration.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
        onCreate(db);
    }

    // --- Notes Table Operations ---

    // Fetch all notes
    public ArrayList<Note> getAllNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // SELECT * FROM Notes;
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NOTES, null);

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow(COLUMN_NOTE_ID));
                String title = c.getString(c.getColumnIndexOrThrow(COLUMN_NOTE_TITLE));
                String content = c.getString(c.getColumnIndexOrThrow(COLUMN_NOTE_CONTENT));
                // Handle potential null folderId if you allowed it (due to ON DELETE SET NULL)
                int folderId = c.isNull(c.getColumnIndexOrThrow(COLUMN_NOTE_FOLDER_ID)) ? 0 : c.getInt(c.getColumnIndexOrThrow(COLUMN_NOTE_FOLDER_ID));
                notes.add(new Note(id, title, content, folderId));
            } while (c.moveToNext());
        }
        c.close();
        db.close(); // Close the database after use
        return notes;
    }

    // Fetch notes by folderId
    public ArrayList<Note> getNotesByFolder(int folderId) {
        ArrayList<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(
                TABLE_NOTES,
                new String[]{COLUMN_NOTE_ID, COLUMN_NOTE_TITLE, COLUMN_NOTE_CONTENT, COLUMN_NOTE_FOLDER_ID},
                COLUMN_NOTE_FOLDER_ID + " = ?",
                new String[]{String.valueOf(folderId)},
                null, null, null
        );

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow(COLUMN_NOTE_ID));
                String title = c.getString(c.getColumnIndexOrThrow(COLUMN_NOTE_TITLE));
                String content = c.getString(c.getColumnIndexOrThrow(COLUMN_NOTE_CONTENT));
                // Handle potential null folderId
                int currentFolderId = c.isNull(c.getColumnIndexOrThrow(COLUMN_NOTE_FOLDER_ID)) ? 0 : c.getInt(c.getColumnIndexOrThrow(COLUMN_NOTE_FOLDER_ID));
                notes.add(new Note(id, title, content, currentFolderId));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return notes;
    }


    // Get a specific note by ID
    public Note getNote(int id) { // You can rename this to getNoteById if you prefer, and remove your existing getNoteById
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "Notes", // <-- Corrected: Use your existing table name string literal
                new String[]{"id", "title", "content", "folderId"}, // <-- Corrected: Use your existing column name string literals
                "id" + "=?", // <-- Corrected: Use your existing column name string literal
                new String[]{String.valueOf(id)},
                null, null, null, null
        );

        Note note = null;
        if (cursor != null && cursor.moveToFirst()) {
            note = new Note(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("content")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("folderId"))
            );
            cursor.close();
        }
        db.close(); // Important to close the database connection
        return note;
    }


    // Insert a note
    public long addNote(String title, String content, int folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_TITLE, title);
        values.put(COLUMN_NOTE_CONTENT, content);
        // Only put folderId if it's not the default 0 or a valid ID
        if (folderId != 0) { // Assuming 0 is your default 'unfiled'
            values.put(COLUMN_NOTE_FOLDER_ID, folderId);
        } else {
            // If folderId is 0, let the column default handle it (which is 0)
            // or explicitly put null if you prefer notes to truly have no folder
            values.putNull(COLUMN_NOTE_FOLDER_ID); // If you want notes without a folder to be NULL
        }
        long newRowId = db.insert(TABLE_NOTES, null, values);
        db.close();
        return newRowId;
    }

    // Update a note
    public void updateNote(int id, String title, String content, int folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_TITLE, title);
        values.put(COLUMN_NOTE_CONTENT, content);
        if (folderId != 0) { // Assuming 0 is your default 'unfiled'
            values.put(COLUMN_NOTE_FOLDER_ID, folderId);
        } else {
            values.putNull(COLUMN_NOTE_FOLDER_ID); // Set to NULL if 0 is passed, or if you explicitly want unfiled notes as NULL
        }
        db.update(TABLE_NOTES, values, COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete a note
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // --- Folders Table Operations ---

    // Add a new folder
    public long addFolder(String folderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOLDER_NAME, folderName);
        long newRowId = db.insert(TABLE_FOLDERS, null, values);
        db.close();
        return newRowId;
    }

    // Get all folders
    public ArrayList<Folder> getAllFolders() {
        ArrayList<Folder> folders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FOLDERS, null);

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow(COLUMN_FOLDER_ID));
                String name = c.getString(c.getColumnIndexOrThrow(COLUMN_FOLDER_NAME));
                folders.add(new Folder(id, name));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return folders;
    }

    // Get folder by ID
    public Folder getFolderById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(
                TABLE_FOLDERS,
                new String[]{COLUMN_FOLDER_ID, COLUMN_FOLDER_NAME},
                COLUMN_FOLDER_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null, null
        );

        Folder folder = null;
        if (c != null && c.moveToFirst()) {
            int folderId = c.getInt(c.getColumnIndexOrThrow(COLUMN_FOLDER_ID));
            String name = c.getString(c.getColumnIndexOrThrow(COLUMN_FOLDER_NAME));
            folder = new Folder(folderId, name);
        }
        if (c != null) {
            c.close();
        }
        db.close();
        return folder;
    }

    // Update a folder's name
    public void updateFolder(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOLDER_NAME, newName);
        db.update(TABLE_FOLDERS, values, COLUMN_FOLDER_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete a folder
    public void deleteFolder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOLDERS, COLUMN_FOLDER_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}