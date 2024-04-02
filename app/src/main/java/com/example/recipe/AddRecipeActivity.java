package com.example.recipe;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddRecipeActivity extends AppCompatActivity {

    SQLiteDatabase recipeDatabase;

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
                String name = recipeNameEditText.getText().toString();
                String ingredients = ingredientsEditText.getText().toString();
                String instructions = instructionsEditText.getText().toString();

                ContentValues contentValues = new ContentValues();
                contentValues.put("name", name);
                contentValues.put("ingredients", ingredients);
                contentValues.put("instructions", instructions);

                long result = recipeDatabase.insert("recipes", null, contentValues);
                if (result != -1) {
                    Toast.makeText(AddRecipeActivity.this, "Recipe added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Error adding recipe", Toast.LENGTH_SHORT).show();
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
    }
}
