package com.cs360.project3;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class DeleteFragment extends Fragment {

    private DatabaseHelper dbHelper;

    // Add a default constructor
    public DeleteFragment() {
        // Required empty public constructor
    }

    // Create a method to set the DatabaseHelper
    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public static DeleteFragment newInstance(DatabaseHelper dbHelper) {
        DeleteFragment fragment = new DeleteFragment();
        fragment.setDatabaseHelper(dbHelper);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delete, container, false);

        TextView confirmationMessageTextView = view.findViewById(R.id.confirmationMessageTextView);
        confirmationMessageTextView.setText("Items deleted successfully!");


        // Add a button to navigate back to the InventoryFragment
        Button backToInventoryButton = view.findViewById(R.id.backToInventoryButton);
        backToInventoryButton.setOnClickListener(v -> navigateBackToInventoryFragment());

        return view;
    }

    // method to navigate back to the InventoryFragment
    private void navigateBackToInventoryFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        InventoryFragment inventoryFragment = new InventoryFragment();
        inventoryFragment.setDatabaseHelper(dbHelper);
        fragmentTransaction.replace(R.id.fragmentContainer, inventoryFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
