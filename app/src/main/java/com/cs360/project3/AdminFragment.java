package com.cs360.project3;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.cs360.project3.DatabaseHelper;
import com.cs360.project3.R;

import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText usernameEditText, passwordEditText, phoneNumberEditText;
    private Button addButton;
    private ListView usersListView;
    private ArrayAdapter<String> usersAdapter;
    private CheckBox smsOptInCheckBox;
    private Spinner roleSpinner;

    public AdminFragment() {
        // Required empty public constructor
    }

    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        // Initialize UI elements
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText);
        smsOptInCheckBox = view.findViewById(R.id.smsOptInCheckBox);
        roleSpinner = view.findViewById(R.id.roleSpinner);
        addButton = view.findViewById(R.id.addButton);
        usersListView = view.findViewById(R.id.usersListView);

        // Initialize database helper
        dbHelper = new DatabaseHelper(requireContext());

        // Set up list view adapter
        List<String> userList = getUsersFromDatabase();
        usersAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, userList);
        usersListView.setAdapter(usersAdapter);

        // Set up click listener for add button
        addButton.setOnClickListener(v -> onAddButtonClick());

        // Set up role spinner adapter
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.role_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        return view;
    }

    private void onAddButtonClick() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        boolean smsOptIn = smsOptInCheckBox.isChecked();
        String role = roleSpinner.getSelectedItem().toString();

        // Check if username is not empty
        if (!username.isEmpty()) {
            // Add the user to the database
            long newRowId = addUserToDatabase(username, password, phoneNumber, smsOptIn, role);
            if (newRowId != -1) {
                Toast.makeText(requireContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                refreshUserList();
            } else {
                Toast.makeText(requireContext(), "Failed to add user", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }

    private long addUserToDatabase(String username, String password, String phoneNumber, boolean smsOptIn, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(DatabaseHelper.COLUMN_SMS_OPT_IN, smsOptIn ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_ROLE, role);
        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    private List<String> getUsersFromDatabase() {
        List<String> userList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_USERNAME + " FROM " + DatabaseHelper.TABLE_USERS, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
                userList.add(username);
            }
            cursor.close();
        }
        return userList;
    }

    private void refreshUserList() {
        List<String> userList = getUsersFromDatabase();
        usersAdapter.clear();
        usersAdapter.addAll(userList);
        usersAdapter.notifyDataSetChanged();
    }
}
