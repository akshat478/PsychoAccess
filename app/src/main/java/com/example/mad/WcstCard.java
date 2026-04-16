package com.example.mad;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageView;

public class WcstCard {
    public enum Shape { TRIANGLE, STAR, CROSS, CIRCLE }
    public enum ColorName { RED, GREEN, YELLOW, BLUE }

    public final Shape shape;
    public final ColorName color;
    public final int quantity;

    public WcstCard(Shape shape, ColorName color, int quantity) {
        this.shape = shape;
        this.color = color;
        this.quantity = quantity;
    }

    public void render(Context context, GridLayout container) {
        container.removeAllViews();
        
        int hexColor;
        switch (color) {
            case RED: hexColor = 0xFFD32F2F; break;
            case GREEN: hexColor = 0xFF388E3C; break;
            case YELLOW: hexColor = 0xFFFBC02D; break;
            case BLUE: hexColor = 0xFF1976D2; break;
            default: hexColor = Color.BLACK;
        }

        int shapeRes;
        switch (shape) {
            case TRIANGLE: shapeRes = android.R.drawable.ic_media_play; break;
            case STAR: shapeRes = android.R.drawable.btn_star_big_on; break;
            case CROSS: shapeRes = android.R.drawable.ic_menu_add; break;
            case CIRCLE: shapeRes = android.R.drawable.presence_online; break;
            default: shapeRes = android.R.drawable.ic_delete;
        }

        for (int i = 0; i < quantity; i++) {
            ImageView img = new ImageView(context);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            img.setLayoutParams(params);
            
            img.setImageResource(shapeRes);
            img.setColorFilter(hexColor);
            img.setPadding(4, 4, 4, 4);
            container.addView(img);
        }
    }
}