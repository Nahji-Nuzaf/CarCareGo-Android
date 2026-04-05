package com.example.carcarego.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.carcarego.R;
import com.example.carcarego.UIUtils;
import com.example.carcarego.databinding.ActivityEditProfileBinding;
import com.example.carcarego.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private String uid;
    private Uri selectedImageUri;
    private String currentPhotoPath;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null && result.getData().getData() != null) {
                        selectedImageUri = result.getData().getData();
                    }

                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.account_circle_24px)
                            .into(binding.ivEditProfilePicture);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        loadCurrentUserData();

        binding.btnChangePic.setOnClickListener(v -> showImageSourceDialog());

        binding.btnUpdateProfile.setOnClickListener(v -> {

            String newName = binding.etEditName.getText().toString();

            if (selectedImageUri != null) {
                uploadImageAndSaveData();
                saveLastUpdateTimestamp();
                saveUserLocally(newName);
            } else {
                saveDataToFirestore(null);
                saveLastUpdateTimestamp();
                saveUserLocally(newName);
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Update Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                }).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void openCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            androidx.core.app.ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, 100);
        } else {
            launchCameraIntent();
        }
    }

    private void launchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e("CAMERA_ERROR", ex.getMessage());
        }

        if (photoFile != null) {
            selectedImageUri = FileProvider.getUriForFile(this,
                    "com.example.carcarego.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
            imagePickerLauncher.launch(takePictureIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadCurrentUserData() {
        db.collection("users").document(uid).get().addOnSuccessListener(ds -> {
            if (ds.exists()) {
                binding.etEditName.setText(ds.getString("name"));
                binding.etEditEmail.setText(ds.getString("email"));
                binding.etEditPhone.setText(ds.getString("mobileNumber"));

                String base64Image = ds.getString("profilePicUrl");
                if (base64Image != null && !base64Image.isEmpty()) {
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Glide.with(this).asBitmap().load(decodedString).into(binding.ivEditProfilePicture);
                }
            }
        });
    }

    private void uploadImageAndSaveData() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

            String base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            saveDataToFirestore(base64Image);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDataToFirestore(String newImageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", binding.etEditName.getText().toString());
        updates.put("mobileNumber", binding.etEditPhone.getText().toString());
        updates.put("email", binding.etEditEmail.getText().toString());

        if (newImageUrl != null) {
            updates.put("profilePicUrl", newImageUrl);
        }

        db.collection("users").document(uid).update(updates).addOnSuccessListener(aVoid -> {
            UIUtils.showCustomSnackbar(binding.getRoot(), "Profile Updated!", true);
            NotificationHelper.showBookingNotification(this, "Profile Updated!", "Success!");
            finish();
        });
    }

    private void saveUserLocally(String name) {
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_name", name);

        editor.apply();
        Log.d("SHARED_PREF", "Name saved locally: " + name);
    }

    private void saveLastUpdateTimestamp() {
        String fileName = "last_update.txt";
        String timestamp = "Profile last updated on: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

        try (java.io.FileOutputStream fos = openFileOutput(fileName, android.content.Context.MODE_PRIVATE)) {
            fos.write(timestamp.getBytes());
            Log.d("INTERNAL_STORAGE", "Timestamp saved to file");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private String readLastUpdateTimestamp() {
        try (java.io.FileInputStream fis = openFileInput("last_update.txt");
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis))) {
            return reader.readLine();
        } catch (java.io.IOException e) {
            return "No recent updates found.";
        }
    }
}