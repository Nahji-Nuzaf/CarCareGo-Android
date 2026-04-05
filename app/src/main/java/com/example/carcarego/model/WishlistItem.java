package com.example.carcarego.model;

import com.google.firebase.firestore.Exclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistItem {
    @Exclude
    private String documentId;
    private String productId;
    private String title;
    private String image;
    private double price;
}