package com.example.carcarego.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.UIUtils;
import com.example.carcarego.adapter.CarImageAdapter;
import com.example.carcarego.databinding.ActivityAddCarBinding;
import com.example.carcarego.model.Car;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddCarActivity extends AppCompatActivity {

    private ActivityAddCarBinding binding;
    private FirebaseFirestore db;
    private String uid;
    private List<String> base64Images = new ArrayList<>();
    private CarImageAdapter imageAdapter;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    handleImageSelection(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        imageAdapter = new CarImageAdapter(base64Images);
        binding.rvVehicleImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvVehicleImages.setAdapter(imageAdapter);

        binding.btnBackAddCar.setOnClickListener(v -> finish());

        binding.btnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        binding.btnAddVehicle.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        String model = binding.etVehicleModel.getText().toString().trim();
        String plate = binding.etLicensePlate.getText().toString().trim();
        String type = binding.etVehicleType.getText().toString().trim();

        if (model.isEmpty() || plate.isEmpty() || type.isEmpty()) {
            UIUtils.showCustomSnackbar(binding.getRoot(), "Please fill in all details", false);
            return;
        }

        Car car = new Car(null, model, plate, type, base64Images);

        binding.btnAddVehicle.setEnabled(false);

        db.collection("users")
                .document(uid)
                .collection("garage")
                .add(car)
                .addOnSuccessListener(documentReference -> {
                    documentReference.update("id", documentReference.getId());
                    UIUtils.showCustomSnackbar(binding.getRoot(), "Vehicle Added Successfully!", true);
                    binding.btnAddVehicle.postDelayed(this::finish, 2000);
                })
                .addOnFailureListener(e -> {
                    binding.btnAddVehicle.setEnabled(true);
                    UIUtils.showCustomSnackbar(binding.getRoot(), "Error: " + e.getMessage(), false);
                });
    }

    private void handleImageSelection(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            base64Images.add(encoded);
            imageAdapter.notifyDataSetChanged();

            UIUtils.showCustomSnackbar(binding.getRoot(), "Image added to list", true);

        } catch (Exception e) {
            UIUtils.showCustomSnackbar(binding.getRoot(), "Failed to add image", false);
        }
    }
}