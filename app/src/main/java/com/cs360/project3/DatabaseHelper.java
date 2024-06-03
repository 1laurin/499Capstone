package com.cs360.project3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 5;

    // Table for user logins
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_SMS_OPT_IN  = "sms_opt";
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COLUMN_ITEM_ID = "_id";
    public static final String COLUMN_ITEM_NAME = "item_name";
    public static final String COLUMN_ITEM_QUANTITY = "quantity";

    public static final String COLUMN_ROLE = "role";


    private static long currentUserId = -1; // Initialize with an invalid value

    public static void setCurrentUserId(long userId) {
        currentUserId = userId;
    }

    public static long getCurrentUserId() {
        return currentUserId;
    }

    public static final String COLUMN_SALT ="salt";

    // Constraints
    // Updated user table creation SQL
    private static final String USER_TABLE_CONSTRAINTS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_SALT + " TEXT, " +  // Added salt column
                    COLUMN_PHONE_NUMBER + " TEXT, " +
                    COLUMN_SMS_OPT_IN + " INTEGER DEFAULT 0, " +
                    COLUMN_ROLE + " TEXT DEFAULT 'User')";




    private static final String INVENTORY_TABLE_CONSTRAINTS =
            "CREATE TABLE " + TABLE_INVENTORY + " (" +
                    COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ITEM_NAME + " TEXT, " +
                    COLUMN_ITEM_QUANTITY + " INTEGER)";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DatabaseHelper", "Constructor called");
    }

    // Creating tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USER_TABLE_CONSTRAINTS);
        db.execSQL(INVENTORY_TABLE_CONSTRAINTS);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {  // Check the old version to conditionally upgrade
            // Add the role column if it doesn't exist
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ROLE + " TEXT DEFAULT 'User'");
        } else {
            // Drop the old tables and recreate them if not upgrading from an old version
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
            onCreate(db);
        }
    }


    // Insert a new inventory entry
    public long insertInventoryEntry(String itemName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_ITEM_QUANTITY, quantity);
        long newRowId = db.insert(TABLE_INVENTORY, null, values);

        return newRowId;
    }

    // Retrieve user's phone number based on user ID
    public String getUserPhoneNumber(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String phoneNumber = null;

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_PHONE_NUMBER}, // Specify the correct column name
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_PHONE_NUMBER);
            if (columnIndex != -1) {
                phoneNumber = cursor.getString(columnIndex);
            } else {
                // Handle the case where the specified column is not found
                Log.e("DatabaseHelper", "Column not found: " + COLUMN_PHONE_NUMBER);
            }

            cursor.close();
        }

        return phoneNumber;
    }

    // Get all inventory entries
    public List<InventoryEntry> getAllInventoryEntries() {
        List<InventoryEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_INVENTORY,
                new String[]{COLUMN_ITEM_ID, COLUMN_ITEM_NAME, COLUMN_ITEM_QUANTITY},
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long idColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_ID);
                long id = idColumnIndex != -1 ? cursor.getLong((int) idColumnIndex) : -1;

                int nameColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_NAME);
                String itemName = nameColumnIndex != -1 ? cursor.getString(nameColumnIndex) : "";

                int quantityColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_QUANTITY);
                int quantity = quantityColumnIndex != -1 ? cursor.getInt(quantityColumnIndex) : -1;

                entries.add(new InventoryEntry(id, itemName, quantity));
            }
            cursor.close();
        }

        return entries;
    }

    // Delete an inventory entry by Id
    public void deleteInventoryEntry(long entryId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int deletedRows = db.delete(TABLE_INVENTORY, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(entryId)});

            if (deletedRows > 0) {
                Log.d("DatabaseHelper", "Deleted entry with ID: " + entryId);
            } else {
                Log.e("DatabaseHelper", "Failed to delete entry with ID: " + entryId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting entry with ID: " + entryId, e);
        } finally {
            db.close();
        }
    }

    // Method to update the user role
    public void updateUserRole(long userId, String newRole) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ROLE, newRole);

            int updatedRows = db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[]{String.valueOf(userId)});

            if (updatedRows > 0) {
                Log.d("DatabaseHelper", "Updated role for user with ID: " + userId);
            } else {
                Log.e("DatabaseHelper", "Failed to update role for user with ID: " + userId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating role for user with ID: " + userId, e);
        } finally {
            db.close();
        }
    }


    public void updateQuantity(long entryId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_QUANTITY, newQuantity);

            int updatedRows = db.update(TABLE_INVENTORY, values, COLUMN_ITEM_ID + "=?",
                    new String[]{String.valueOf(entryId)});

            if (updatedRows > 0) {
                Log.d("DatabaseHelper", "Updated quantity for entry with ID: " + entryId);
            } else {
                Log.e("DatabaseHelper", "Failed to update quantity for entry with ID: " + entryId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating quantity for entry with ID: " + entryId, e);
        } finally {
            db.close();
        }
    }

}
