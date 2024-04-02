package com.example.recipe;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditRecipeActivity extends AppCompatActivity {

    SQLiteDatabase recipeDatabase;
    EditText recipeNameEditText;
    EditText ingredientsEditText;
    EditText instructionsEditText;
    Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        recipeDatabase = openOrCreateDatabase("recipeDatabase", MODE_PRIVATE, null);

        recipeNameEditText = findViewById(R.id.editRecipeNameEditText);
        ingredientsEditText = findViewById(R.id.editIngredientsEditText);
        instructionsEditText = findViewById(R.id.editInstructionsEditText);
        updateButton = findViewById(R.id.updateButton);

        // Retrieve the recipe name passed from RecipeDetailsActivity
        final String recipeName = getIntent().getStringExtra("recipeName");

        // Fetch recipe details from the database
        Cursor cursor = recipeDatabase.rawQuery("SELECT * FROM recipes WHERE name=?", new String[]{recipeName});
        if (cursor.moveToFirst()) {
            recipeNameEditText.setText(cursor.getString(cursor.getColumnIndex("name")));
            ingredientsEditText.setText(cursor.getString(cursor.getColumnIndex("ingredients")));
            instructionsEditText.setText(cursor.getString(cursor.getColumnIndex("instructions")));
        }
        cursor.close();

        // Update button click listener
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get updated values from EditText fields
                String updatedName = recipeNameEditText.getText().toString();
                String updatedIngredients = ingredientsEditText.getText().toString();
                String updatedInstructions = instructionsEditText.getText().toString();

                // Update recipe in the database
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", updatedName);
                contentValues.put("ingredients", updatedIngredients);
                contentValues.put("instructions", updatedInstructions);

                int rowsAffected = recipeDatabase.update("recipes", contentValues, "name=?", new String[]{recipeName});
                if (rowsAffected > 0) {
                    Toast.makeText(EditRecipeActivity.this, "Recipe updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditRecipeActivity.this, "Error updating recipe", Toast.LENGTH_SHORT).show();
                }

                // Close the activity
                finish();
            }
        });
    }
}

