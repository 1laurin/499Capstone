package com.cs360.project3;

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
        // Check if SMS permission is granted
        if (isSmsPermissionGranted()) {
            // Continue with the login process
            continueLogin(username, password);
        } else {
            // If SMS permission is not granted, show the permission prompt
            ((MainActivity) requireActivity()).loadPermissionPromptFragment();
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
                    // User found, retrieve the user ID
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);

                        // Check if the column index is valid
                        if (columnIndex != -1) {
                            long userId = cursor.getLong(columnIndex);

                            // Save the user ID to shared preferences
                            saveUserIdToPreferences(userId);

                            // Login successful
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show();

                            // Navigate to the InventoryFragment
                            loadInventoryFragment();
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
