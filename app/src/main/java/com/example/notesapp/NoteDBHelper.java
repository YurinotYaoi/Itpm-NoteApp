package com.example.notesapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class NoteDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "NotesDB";
    private static final int DB_VERSION = 1;

    public NoteDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS Notes(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "content TEXT," +
                "folderId INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // You can handle migrations here if needed
        db.execSQL("DROP TABLE IF EXISTS Notes");
        onCreate(db);
    }

    // Fetch all notes
    public ArrayList<Note> getAllNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Notes", null);

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                String content = c.getString(c.getColumnIndexOrThrow("content"));
                int folderId = c.getInt(c.getColumnIndexOrThrow("folderId"));
                notes.add(new Note(id, title, content, folderId));
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }
    // Inside NoteDBHelper.java
    public Note getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Notes WHERE id = ?", new String[]{String.valueOf(id)});

        if (c.moveToFirst()) {
            String title = c.getString(c.getColumnIndexOrThrow("title"));
            String content = c.getString(c.getColumnIndexOrThrow("content"));
            int folderId = c.getInt(c.getColumnIndexOrThrow("folderId"));
            int noteId = c.getInt(c.getColumnIndexOrThrow("id"));
            c.close();
            return new Note(noteId, title, content, folderId);
        }
        c.close();
        return null;
    }


    // Insert a note
    public long addNote(String title, String content, int folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("folderId", folderId);
        return db.insert("Notes", null, values);
    }

    // Update a note
    public void updateNote(int id, String title, String content, int folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("folderId", folderId);
        db.update("Notes", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // Delete a note
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Notes", "id = ?", new String[]{String.valueOf(id)});
    }

    // Inside your NoteDBHelper.java file

    // Inside your NoteDBHelper.java file

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
}
