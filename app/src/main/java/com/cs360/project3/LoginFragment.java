package com.cs360.project3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LoginFragment extends Fragment {

    private View view;

    public static final int SMS_PERMISSION_REQUEST_CODE = 123;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private DatabaseHelper dbHelper;

    public LoginFragment() {
        // Required empty public constructor
    }

    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);

        // Access UI elements
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);

        dbHelper = new DatabaseHelper(requireContext()); // Initialize the DatabaseHelper

        // Set up click listener for the login button
        loginButton.setOnClickListener(v -> onLoginButtonClick());

        Button createAccountButton = view.findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(v -> onCreateAccountButtonClick(v));

        return view;
    }

    private void onLoginButtonClick() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Check if SMS permission is granted
        if (isSmsPermissionGranted()) {
            // If SMS permission is granted, proceed with login
            performLogin(username, password);
        } else {
            // If SMS permission is not granted, request it
            requestSmsPermission();
        }
    }

    private void requestSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request SMS permission
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            // Handle devices with SDK version less than M
            Toast.makeText(requireContext(), "SMS Permission granted on older devices.", Toast.LENGTH_SHORT).show();
            performLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(requireContext(), "SMS Permission granted. You will receive notifications.", Toast.LENGTH_SHORT).show();
                // Proceed with your existing logic (e.g., login)
                performLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "SMS Permission denied. Notifications will not be sent.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performLogin(String username, String password) {
        // Ensure dbHelper is not null before accessing its methods
        if (dbHelper != null) {
            try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
                // Query to retrieve the hashed password and salt based on the entered username
                Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_PASSWORD + ", " + DatabaseHelper.COLUMN_SALT +
                                " FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USERNAME + "=?",
                        new String[]{username});

                if (cursor.moveToFirst()) {
                    // Retrieve the hashed password and salt from the database
                    @SuppressLint("Range") String hashedPasswordFromDb = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD));
                    @SuppressLint("Range") String salt = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SALT));

                    // Convert the salt string to a byte array
                    byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

                    // Hash the entered password using the retrieved salt
                    String hashedPassword = Arrays.toString(PasswordHashing.hashPassword(password, saltBytes));


                    // Compare the hashed passwords
                    if (hashedPasswordFromDb.equals(hashedPassword)) {
                        // Passwords match, login successful
                        // Retrieve the user ID and role
                        int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                        int roleColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE);

                        if (idColumnIndex != -1 && roleColumnIndex != -1) {
                            long userId = cursor.getLong(idColumnIndex);
                            String userRole = cursor.getString(roleColumnIndex);

                            // Save the user ID and role to shared preferences
                            saveUserIdToPreferences(userId);
                            saveUserRoleToPreferences(userRole);

                            // Grant access based on user role
                            grantAccess(userRole);

                            // Login successful
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                // Login failed
                Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                // Handle exceptions, log an error, etc.
            }
        } else {
            // Handle dbHelper being null
        }
    }


    private void grantAccess(String userRole) {
        if (userRole.equals("Admin")) {
            // Grant access to admin features
            loadAdminFragment();
        } else if (userRole.equals("Manager")) {
            // Grant access to manager features
            loadInventoryFragment();
        } else {
            // Grant access to basic user features
            loadInventoryFragment(); // Load the inventory fragment for regular users
        }
    }


    private void continueLogin(String username, String password) {
        // Ensure dbHelper is not null before accessing its methods
        if (dbHelper != null) {
            try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
                // Query to check if the user with the given username and password exists
                Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                        " WHERE " + DatabaseHelper.COLUMN_USERNAME + "=? AND " +
                        DatabaseHelper.COLUMN_PASSWORD + "=?", new String[]{username, password});

                if (cursor.getCount() > 0) {
                    // User found, retrieve the user ID and role
                    if (cursor.moveToFirst()) {
                        int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                        int roleColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE);

                        // Check if the column index is valid
                        if (idColumnIndex != -1 && roleColumnIndex != -1) {
                            long userId = cursor.getLong(idColumnIndex);
                            String userRole = cursor.getString(roleColumnIndex);

                            // Save the user ID and role to shared preferences
                            saveUserIdToPreferences(userId);
                            saveUserRoleToPreferences(userRole);

                            // Login successful
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show();

                            // Grant access based on user role
                            grantAccess(userRole);
                        } else {
                            // Handle the case where the column index is not found
                            Log.e("LoginFragment", "Column not found: " + DatabaseHelper.COLUMN_ID);
                        }
                    }
                } else {
                    // Login failed
                    Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle exceptions, log an error, etc.
            }
        } else {
            // Handle dbHelper being null
        }
    }

    private void saveUserRoleToPreferences(String userRole) {
        SharedPreferences preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Save the user role
        editor.putString("user_role", userRole);
        editor.apply();
    }


    private void loadAdminFragment() {
        // Load the admin fragment or activity
        ((MainActivity) requireActivity()).loadAdminFragment();
    }

    private void loadManagerFragment() {
        // Load the manager fragment or activity
        ((MainActivity) requireActivity()).loadManagerFragment();
    }


    private boolean isSmsPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if SMS permission is granted
            return ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // On older devices, assume permission is always granted
            return true;
        }
    }


    private void saveUserIdToPreferences(long userId) {
        SharedPreferences preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Save the user ID
        editor.putLong("user_id", userId);
        editor.apply();
    }

    private void loadInventoryFragment() {
        ((MainActivity) requireActivity()).loadInventoryFragment();
    }

    public void onCreateAccountButtonClick(View view) {
        ((MainActivity) requireActivity()).loadRegisterFragment();
    }
}
