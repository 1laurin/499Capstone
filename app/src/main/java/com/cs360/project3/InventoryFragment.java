package com.cs360.project3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import android.util.Log;

import androidx.fragment.app.Fragment;

import java.util.List;

public class InventoryFragment extends Fragment {

    private View view;
    private GridView dataGridView;
    private Button addDataButton;
    private Button deleteDataButton;

    private Button updateQuantityButton;

    private Button logoutButton;

    private DatabaseHelper dbHelper;

    public InventoryFragment() {
        // Required empty public constructor
    }

    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public static InventoryFragment newInstance(DatabaseHelper dbHelper) {
        InventoryFragment fragment = new InventoryFragment();
        fragment.setDatabaseHelper(dbHelper);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_inventory, container, false);

        dataGridView = view.findViewById(R.id.dataGridView);
        addDataButton = view.findViewById(R.id.addDataButton);
        deleteDataButton = view.findViewById(R.id.deleteDataButton);
        updateQuantityButton = view.findViewById(R.id.updateQuantityButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        addDataButton.setOnClickListener(v -> onAddDataButtonClick());
        deleteDataButton.setOnClickListener(v -> onDeleteDataButtonClick());
        updateQuantityButton.setOnClickListener(v -> onUpdateQuantityButtonClick());
        logoutButton.setOnClickListener(v -> onLogoutButtonClick());

        loadInventoryData();


        return view;
    }

    private void onAddDataButtonClick() {
        ((MainActivity) requireActivity()).loadAddDataFragment();
    }

    private void loadInventoryData() {
        try {
            if (dbHelper != null && isAdded()) {
                List<InventoryEntry> entries = dbHelper.getAllInventoryEntries();

                if (entries != null) {
                    // Initialize the adapter with entries
                    InventoryAdapter adapter = new InventoryAdapter(getContext(), entries);

                    // Set the item click listener to handle item selection
                    dataGridView.setOnItemClickListener((parent, view, position, id) -> {
                        adapter.toggleSelection(position);
                    });

                    // Set the adapter for the GridView
                    dataGridView.setAdapter(adapter);
                } else {
                    Log.e("InventoryFragment", "Error: List of entries is null");
                }
            } else {
                Log.e("InventoryFragment", "Error: dbHelper is not initialized or Fragment is not added");
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading inventory data", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            handleDataOperationError(e, "Error loading inventory data");
        }
    }

    private void onDeleteDataButtonClick() {
        try {
            InventoryAdapter adapter = (InventoryAdapter) dataGridView.getAdapter();

            if (adapter != null) {
                List<InventoryEntry> selectedEntries = adapter.getSelectedItems();

                Log.d("InventoryFragment", "Deleting selected items: " + selectedEntries.toString());

                for (InventoryEntry selectedEntry : selectedEntries) {
                    dbHelper.deleteInventoryEntry(selectedEntry.getId());

                    // Remove from list
                    adapter.removeItem(selectedEntry);
                }

                adapter.notifyDataSetChanged();

                // Use loadDeleteFragment instead of loadDelete
                ((MainActivity) requireActivity()).loadDeleteFragment();
            }
        } catch (Exception e) {
            handleDataOperationError(e, "Error deleting data");
        }
    }

    private void onUpdateQuantityButtonClick() {
        // Check if any item is selected
        InventoryAdapter adapter = (InventoryAdapter) dataGridView.getAdapter();
        if (adapter != null && adapter.getSelectedItems().size() == 1) {
            // Get the selected item and its ID
            InventoryEntry selectedEntry = adapter.getSelectedItems().get(0);
            long entryId = selectedEntry.getId();

            // Load the UpdateQuantityFragment with the selected entry ID
            ((MainActivity) requireActivity()).loadUpdateQuantityFragment(entryId);
        } else {
            // Inform the user that they should select exactly one item
            Toast.makeText(getContext(), "Please select exactly one item to update quantity", Toast.LENGTH_SHORT).show();
        }
    }

    private void onLogoutButtonClick() {
        Intent intent = new Intent(getActivity(), MainActivity.class);

        // Clear user session or any necessary data
        clearUserSession();

        // Start the MainActivity and clear the back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }


    private void clearUserSession() {
        SharedPreferences preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Clear user-related data
        editor.clear();
        editor.apply();
    }





    private void handleDataOperationError(Exception e, String errorMessage) {
        e.printStackTrace();
        Log.e("InventoryFragment", errorMessage + ": " + e.getMessage());
        if (isAdded()) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}