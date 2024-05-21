package com.cs360.project3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class PermissionPromptFragment extends Fragment {

    private static final String PERMISSION_SMS = android.Manifest.permission.SEND_SMS;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted
                    Toast.makeText(requireContext(), "SMS Permission granted. You will receive notifications.", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "SMS Permission denied. Notifications will not be sent.", Toast.LENGTH_SHORT).show();
                    showPermissionSettings();
                }
            });

    public PermissionPromptFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission_prompt, container, false);

        TextView infoTextView = view.findViewById(R.id.infoTextView);
        Button requestPermissionButton = view.findViewById(R.id.requestPermissionButton);

        requestPermissionButton.setOnClickListener(v -> requestSmsPermission());


        return view;
    }


    private void requestSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if SMS permission is already granted
            if (ActivityCompat.checkSelfPermission(requireContext(), PERMISSION_SMS) == PackageManager.PERMISSION_GRANTED) {
                // Permission already granted
                Toast.makeText(requireContext(), "SMS Permission already granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Request SMS permission
                requestPermissionLauncher.launch(PERMISSION_SMS);
            }
        } else {
            // Handle devices with SDK version less than M
            Toast.makeText(requireContext(), "SMS Permission granted on older devices.", Toast.LENGTH_SHORT).show();
        }
    }


    private void showPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
