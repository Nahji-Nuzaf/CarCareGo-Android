package com.example.carcarego.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carcarego.R;
import com.example.carcarego.adapter.ProductSliderAdapter;
import com.example.carcarego.adapter.TopTrendingProductsSectionAdapter;
import com.example.carcarego.databinding.ActivityProductDetailBinding;
import com.example.carcarego.fragments.ShopFragment;
import com.example.carcarego.model.CartItem;
import com.example.carcarego.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private String productId;

    private int quantity = 1;
    private int avbQuantity;

    private boolean isWishlisted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent() != null && getIntent().hasExtra("productId")) {
            productId = getIntent().getStringExtra("productId");

            fetchProductDetails(productId);

            loadTopTrendingProducts();

            binding.productDetailsBtnMinus.setOnClickListener(view -> {
                if(quantity > 1){
                    quantity--;
                    binding.productDetailsQuantity.setText(String.valueOf(quantity));
                }
            });

            binding.productDetailsBtnPlus.setOnClickListener(view -> {
                if(quantity < avbQuantity){
                    quantity++;
                    binding.productDetailsQuantity.setText(String.valueOf(quantity));
                }
            });


            binding.btnAddToCart.setOnClickListener(view -> {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(ProductDetailActivity.this, SignupActivity.class);
                    startActivity(intent);
                    return;
                }

                String uid = firebaseAuth.getCurrentUser().getUid();

                com.google.firebase.firestore.CollectionReference cartRef =
                        db.collection("users").document(uid).collection("cart");

                cartRef.whereEqualTo("productId", productId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

                        com.google.firebase.firestore.DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        int existingQuantity = doc.getLong("quantity").intValue();

                        doc.getReference().update("quantity", existingQuantity + quantity)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Cart Updated!", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        CartItem cartItem = new CartItem(productId, quantity);
                        cartRef.add(cartItem).addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.e("CART_ERROR", e.getMessage());
                        });
                    }
                });
            });

            binding.btnAddToWishlist.setOnClickListener(v -> {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null) {
                    // Redirect to Login/Signup if not logged in
                    startActivity(new Intent(this, SignupActivity.class));
                    return;
                }

                String uid = auth.getUid();
//                FirebaseFirestore db = FirebaseFirestore.getInstance();

                if (isWishlisted) {
                    db.collection("users").document(uid).collection("wishlist")
                            .document(productId).delete().addOnSuccessListener(aVoid -> {
                                isWishlisted = false;
                                binding.ivWishlistHeart.setImageResource(R.drawable.heartoutlined);
                                Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    if (currentProduct != null) {
                        Map<String, Object> wishlistItem = new HashMap<>();
                        wishlistItem.put("productId", productId);
                        wishlistItem.put("title", currentProduct.getTitle());
                        wishlistItem.put("image", currentProduct.getImages().get(0));
                        wishlistItem.put("price", currentProduct.getPrice());

                        db.collection("users").document(uid).collection("wishlist")
                                .document(productId).set(wishlistItem).addOnSuccessListener(aVoid -> {
                                    isWishlisted = true;
                                    binding.ivWishlistHeart.setImageResource(R.drawable.heartfilled);
                                    Toast.makeText(this, "Added to Wishlist!", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            });


            binding.btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(ProductDetailActivity.this, ShopFragment.class);
                startActivity(intent);
            });

            binding.btnBack.setOnClickListener(v -> {
                finish();
            });

        }



    }

    private void getFinalSelection() {



    }

    private void loadTopTrendingProducts() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products")
                .whereNotEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if(!qds.isEmpty()){
                            List<Product> products = qds.toObjects(Product.class);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.HORIZONTAL, false);

                            binding.productDetailsTopTrendingProductSection.itemSectionContainer.setLayoutManager(layoutManager);

                            TopTrendingProductsSectionAdapter adapter =  new TopTrendingProductsSectionAdapter(products, product -> {
                                Intent intent = new Intent(ProductDetailActivity.this, ProductDetailActivity.class);
                                intent.putExtra("productId", product.getProductId());
                                startActivity(intent);
                                finish();
                            });

                            binding.productDetailsTopTrendingProductSection.itemSectionTitle.setText("Top Trending Products");
                            binding.productDetailsTopTrendingProductSection.itemSectionContainer.setAdapter(adapter);
                        }
                    }
                });

    }

private Product currentProduct;

    private void fetchProductDetails(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        db.collection("products")
                .whereEqualTo("productId", id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            currentProduct = queryDocumentSnapshots.getDocuments().get(0).toObject(Product.class);

                            if (currentProduct != null) {
                                ProductSliderAdapter adapter = new ProductSliderAdapter(currentProduct.getImages());
                                binding.productImageSlider.setAdapter(adapter);
                                binding.dotsIndicator.attachTo(binding.productImageSlider);

                                binding.productDetailsTitle.setText(currentProduct.getTitle());
                                binding.productDetailsPrice.setText(String.format(Locale.US, "LKR %,.2f", currentProduct.getPrice()));
                                binding.productDetailsAvbQty.setText(currentProduct.getStockCount() + " Items Available");
                                avbQuantity = currentProduct.getStockCount();
                                binding.productDetailsRating.setRating((float) currentProduct.getRating());
                                binding.tvDetailProductDescription.setText(currentProduct.getDescription());

                                String uid = FirebaseAuth.getInstance().getUid(); // Make sure this is declared here

                                if (uid != null) {
                                    db.collection("users").document(uid).collection("wishlist")
                                            .document(id)
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    isWishlisted = true;
//                                                    binding.btnAddToWishlist.setImageResource(R.drawable.favorite_24px);
                                                } else {
                                                    isWishlisted = false;
//                                                    binding.btnAddToWishlist.setImageResource(R.drawable.favorite_24px);
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }
}