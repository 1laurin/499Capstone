package com.cs360.project3;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 123;

    private DatabaseHelper dbHelper;

    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public DatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Check and request SMS permissions
        checkAndRequestSmsPermission();

        // Load the initial fragment
        loadLoginFragment();
    }

    private void checkAndRequestSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS Permission granted. You will receive notifications.", Toast.LENGTH_SHORT).show();

                // Proceed with your existing logic
                manageInventory();
            } else {
                // Permission denied
                Toast.makeText(this, "SMS Permission denied. Notifications will not be sent.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loadAddDataFragment() {
        Log.d("MainActivity", "Before transaction: loading AddDataFragment");

        AddDataFragment addDataFragment = new AddDataFragment();
        addDataFragment.setDatabaseHelper(dbHelper);  // Set the database helper if needed
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, addDataFragment)
                .addToBackStack(null)
                .commit();

        Log.d("MainActivity", "After transaction: AddDataFragment loaded");
    }

    public void loadDeleteFragment() {
        Log.d("MainActivity", "Before transaction: loading DeleteFragment");

        // Create an instance of your DeleteFragment (you might need to create this class)
        DeleteFragment deleteFragment = new DeleteFragment();
        deleteFragment.setDatabaseHelper(dbHelper);  // Set the database helper if needed

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, deleteFragment)
                .addToBackStack(null)
                .commit();

        Log.d("MainActivity", "After transaction: DeleteFragment loaded");
    }

    private void loadLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setDatabaseHelper(dbHelper);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, loginFragment)
                .commit();
    }

    public void loadRegisterFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    void loadInventoryFragment() {
        InventoryFragment inventoryFragment = new InventoryFragment();
        inventoryFragment.setDatabaseHelper(dbHelper);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, inventoryFragment)
                .commit();
    }

    public void loadPermissionPromptFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new PermissionPromptFragment())
                .addToBackStack(null)
                .commit();
    }

    public void loadUpdateQuantityFragment(long entryId) {
        UpdateQuantityFragment updateQuantityFragment = UpdateQuantityFragment.newInstance();
        updateQuantityFragment.setDatabaseHelper(dbHelper);
        updateQuantityFragment.setItemId(entryId);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, updateQuantityFragment)
                .addToBackStack(null)
                .commit();
    }

    private void manageInventory() {
        SQLiteDatabase db = null;
        Cursor inventoryCursor = null;

        try {
            db = dbHelper.getWritableDatabase();

            // Add item to inventory
            ContentValues itemValues = new ContentValues();
            itemValues.put(DatabaseHelper.COLUMN_ITEM_NAME, "Item A");
            itemValues.put(DatabaseHelper.COLUMN_ITEM_QUANTITY, 10);
            db.insert(DatabaseHelper.TABLE_INVENTORY, null, itemValues);

            // Read items from inventory and display in a grid
            inventoryCursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_INVENTORY, null);

            // Ensure the cursor is not null and move to the first row
            if (inventoryCursor != null && inventoryCursor.moveToFirst()) {
                // Update item quantity
                int itemIdColumnIndex = inventoryCursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID);

                if (itemIdColumnIndex != -1) {
                    int itemId = inventoryCursor.getInt(itemIdColumnIndex);
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(DatabaseHelper.COLUMN_ITEM_QUANTITY, 15);
                    db.update(DatabaseHelper.TABLE_INVENTORY, updateValues,
                            DatabaseHelper.COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
                }

                // Delete item from inventory
                itemIdColumnIndex = inventoryCursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID);

                if (itemIdColumnIndex != -1) {
                    int itemId = inventoryCursor.getInt(itemIdColumnIndex);
                    db.delete(DatabaseHelper.TABLE_INVENTORY,
                            DatabaseHelper.COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions
        } finally {
            // Close resources in a finally block to ensure they are closed
            if (inventoryCursor != null) {
                inventoryCursor.close();
            }

            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}
