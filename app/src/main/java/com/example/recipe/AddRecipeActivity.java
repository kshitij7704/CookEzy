package com.example.recipe;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class AddRecipeActivity extends AppCompatActivity {

    SQLiteDatabase recipeDatabase;
    private static final String CHANNEL_ID = "recipe_notification_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        recipeDatabase = openOrCreateDatabase("recipeDatabase", MODE_PRIVATE, null);
        recipeDatabase.execSQL("CREATE TABLE IF NOT EXISTS recipes (id INTEGER PRIMARY KEY, name TEXT, ingredients TEXT, instructions TEXT)");

        final EditText recipeNameEditText = findViewById(R.id.recipeNameEditText);
        final EditText ingredientsEditText = findViewById(R.id.ingredientsEditText);
        final EditText instructionsEditText = findViewById(R.id.instructionsEditText);

        Button saveButton = findViewById(R.id.saveButton);
        Button viewRecipeButton = findViewById(R.id.viewRecipeButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = recipeNameEditText.getText().toString().trim();
                String ingredients = ingredientsEditText.getText().toString().trim();
                String instructions = instructionsEditText.getText().toString().trim();

                // Validate recipe name
                if (name.isEmpty()) {
                    Toast.makeText(AddRecipeActivity.this, "Recipe name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (name.matches("^\\s*$")) {
                    Toast.makeText(AddRecipeActivity.this, "Recipe name cannot consist only of spaces", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate ingredients
                if (ingredients.isEmpty()) {
                    Toast.makeText(AddRecipeActivity.this, "Ingredients cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ingredients.matches("^\\s*$")) {
                    Toast.makeText(AddRecipeActivity.this, "Ingredients cannot consist only of spaces", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate instructions
                if (instructions.isEmpty()) {
                    Toast.makeText(AddRecipeActivity.this, "Instructions cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (instructions.matches("^\\s*$")) {
                    Toast.makeText(AddRecipeActivity.this, "Instructions cannot consist only of spaces", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the recipe name already exists in the database
                Cursor cursor = recipeDatabase.rawQuery("SELECT * FROM recipes WHERE name=?", new String[]{name});
                if (cursor.getCount() > 0) {
                    cursor.close();
                    displayNotification("Recipe already exists", "A recipe with this name already exists");
                    Toast.makeText(AddRecipeActivity.this, "A recipe with this name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("name", name);
                    contentValues.put("ingredients", ingredients);
                    contentValues.put("instructions", instructions);

                    long result = recipeDatabase.insert("recipes", null, contentValues);
                    if (result != -1) {
                        displayNotification("Recipe added", "Your recipe has been added successfully.");
                        Toast.makeText(AddRecipeActivity.this, "Recipe Added", Toast.LENGTH_SHORT).show();
                    } else {
                        displayNotification("Error", "Error adding recipe");
                        Toast.makeText(AddRecipeActivity.this, "Error Adding Recipe", Toast.LENGTH_SHORT);
                    }
                }
            }
        });

        viewRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start RecipeDetailsActivity
                Intent intent = new Intent(AddRecipeActivity.this, RecipeDetailsActivity.class);
                startActivity(intent);
            }
        });

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void displayNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.start)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification ID allows you to update or cancel the notification later on
        int notificationId = 1; // Change this to a unique ID for each notification

        // Display the notification
        notificationManager.notify(notificationId, builder.build());
    }
}
