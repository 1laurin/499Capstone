package com.cs360.project3;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;


import androidx.fragment.app.Fragment;

public class RegisterFragment extends Fragment {

    private View view;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private DatabaseHelper dbHelper;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_register, container, false);

        // Access UI elements
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        registerButton = view.findViewById(R.id.registerButton);

        dbHelper = new DatabaseHelper(requireContext()); // Initialize the DatabaseHelper

        // Set up click listener for the register button
        registerButton.setOnClickListener(v -> registerUser());

        return view;
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Call a method to perform user registration with the provided details
        performRegistration(username, password);
    }

    private void performRegistration(String username, String password) {
        // Ensure dbHelper is not null before accessing its methods
        if (dbHelper != null) {
            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                // Add your user registration logic here...
                // For example, you can insert the user details into the database
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_USERNAME, username);
                values.put(DatabaseHelper.COLUMN_PASSWORD, password);

                long newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values);

                if (newRowId != -1) {
                    // Registration successful
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    // Navigate to the login screen
                    loadLoginFragment();
                } else {
                    // Registration failed
                    Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Handle exceptions, log an error, etc.
            }
        } else {
            // Handle dbHelper being null (e.g., log an error)
            Log.e("RegisterFragment", "dbHelper is null. Registration failed.");
            Toast.makeText(requireContext(), "Registration failed due to an internal error.", Toast.LENGTH_SHORT).show();
        }
    }


    // Add this method to navigate to the login screen
    private void loadLoginFragment() {
        // Replace the contents of fragmentContainer with the login screen
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();
    }
}

