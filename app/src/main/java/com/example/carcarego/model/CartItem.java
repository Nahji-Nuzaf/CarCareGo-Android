package com.example.carcarego.model;

import com.google.firebase.firestore.Exclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Getter(onMethod_ = {@Exclude})
    @Setter(onMethod_ = {@Exclude})
    private String documentId;

    private String productId;
    private int quantity;

    public CartItem(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}