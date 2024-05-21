package com.cs360.project3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import android.widget.Toast;
import android.util.Log;

public class AddDataFragment extends Fragment {

    private View view;
    private EditText itemNameEditText;
    private EditText quantityEditText;
    private Button addButton;
    private DatabaseHelper dbHelper;

    public AddDataFragment() {
        // Required empty public constructor
    }

    public AddDataFragment(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Factory method to create an instance of AddDataFragment with a DatabaseHelper
    public static AddDataFragment newInstance(DatabaseHelper dbHelper) {
        AddDataFragment fragment = new AddDataFragment();
        fragment.setDatabaseHelper(dbHelper);
        return fragment;
    }

    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_data, container, false);


        // Access UI elements
        itemNameEditText = view.findViewById(R.id.itemNameEditText);
        quantityEditText = view.findViewById(R.id.quantityEditText);
        addButton = view.findViewById(R.id.addButton);

        // Set up click listener for the add button
        addButton.setOnClickListener(v -> onAddButtonClick());

        // Set dbHelper in MainActivity
        ((MainActivity) requireActivity()).setDatabaseHelper(dbHelper);

        return view;
    }

    private void onAddButtonClick() {
        String itemName = itemNameEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();

        try {
            if (dbHelper != null && !itemName.trim().isEmpty() && !quantityString.trim().isEmpty()) {
                // Check if quantityString is a valid integer
                if (quantityString.matches("\\d+")) {
                    int quantity = Integer.parseInt(quantityString);

                    if (quantity > 0) {
                        // Insert new data into the inventory
                        dbHelper.insertInventoryEntry(itemName, quantity);

                        // Notify the user and go back to the InventoryFragment
                        Toast.makeText(getContext(), "Data added to inventory", Toast.LENGTH_SHORT).show();

                        // Pop the back stack to return to the previous fragment
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    } else {
                        // Log a message indicating that the quantity is not positive
                        Log.d("AddDataFragment", "Quantity is not a positive integer: " + quantity);

                        // Handle non-positive quantity
                        Toast.makeText(getContext(), "Quantity must be a positive integer", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Log a message indicating that quantityString is not a valid integer
                    Log.d("AddDataFragment", "Quantity is not a valid integer: " + quantityString);

                    // Handle invalid quantity
                    Toast.makeText(getContext(), "Invalid quantity format", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Log a message indicating that one of the conditions failed
                Log.d("AddDataFragment", "One of the conditions failed. dbHelper: " + (dbHelper != null) +
                        ", itemName.isEmpty(): " + itemName.isEmpty() +
                        ", quantityString.isEmpty(): " + quantityString.isEmpty());

                // Handle empty fields or dbHelper not initialized
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            // Log the exception
            Log.e("AddDataFragment", "NumberFormatException: " + e.getMessage(), e);

            // Handle the case where the quantity is not a valid integer
            Toast.makeText(getContext(), "Invalid quantity format", Toast.LENGTH_SHORT).show();
        }
    }
}
