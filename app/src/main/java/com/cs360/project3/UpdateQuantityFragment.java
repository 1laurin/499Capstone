package com.cs360.project3;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class UpdateQuantityFragment extends Fragment {

    private View view;
    private EditText editTextQuantity;
    private Button buttonUpdateQuantity;
    private DatabaseHelper dbHelper;
    private long id;

    public UpdateQuantityFragment() {
        // Required empty public constructor
    }

    public static UpdateQuantityFragment newInstance() {
        return new UpdateQuantityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_update, container, false);

        editTextQuantity = view.findViewById(R.id.editTextQuantity);
        buttonUpdateQuantity = view.findViewById(R.id.buttonUpdateQuantity);

        buttonUpdateQuantity.setOnClickListener(v -> onUpdateQuantityButtonClick());

        return view;
    }

    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void setItemId(long id) {
        this.id = id;
    }

    private void onUpdateQuantityButtonClick() {
        int newQuantity = 0; // Declare newQuantity outside the try block

        if (dbHelper != null) {
            // Retrieve the new quantity from the EditText
            String newQuantityString = editTextQuantity.getText().toString();

            // Check if the new quantity is not empty
            if (!newQuantityString.isEmpty()) {
                try {
                    // Convert the string to an integer
                    newQuantity = Integer.parseInt(newQuantityString);

                    // Pass the new quantity to your database update method
                    dbHelper.updateQuantity(id, newQuantity);

                    // Go back to the previous fragment
                    requireActivity().getSupportFragmentManager().popBackStack();

                } catch (NumberFormatException e) {
                    // Handle the case where the input is not a valid integer
                    e.printStackTrace();
                    // Display an error message to the user
                    Toast.makeText(requireContext(), "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where the input is empty
                Toast.makeText(requireContext(), "Please enter a quantity", Toast.LENGTH_SHORT).show();
            }

            // Check if the new quantity is 0 and send an SMS notification
            if (newQuantity == 0) {
                sendSmsNotification("Inventory Update", "The quantity of an item has been reduced to 0");
            }
        } else {
            // Handle the case where dbHelper is null
            Toast.makeText(requireContext(), "Error: DatabaseHelper is not initialized", Toast.LENGTH_SHORT).show();
        }
    }
    private long getUserId() {
        if (dbHelper != null) {
            return dbHelper.getCurrentUserId();
        } else {
            // Handle the case where dbHelper is null
            Toast.makeText(requireContext(), "Error: DatabaseHelper is not initialized", Toast.LENGTH_SHORT).show();
            return -1; // Return a default value or handle accordingly
        }
    }



    private void sendSmsNotification(String title, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String phoneNumber = "1234567890";

            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception (e.g., log, show error message)
        }
    }

}
