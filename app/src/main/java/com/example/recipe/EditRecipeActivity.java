package com.example.recipe;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditRecipeActivity extends AppCompatActivity {

    SQLiteDatabase recipeDatabase;
    EditText recipeNameEditText;
    EditText ingredientsEditText;
    EditText instructionsEditText;
    Button updateButton;
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        recipeDatabase = openOrCreateDatabase("recipeDatabase", MODE_PRIVATE, null);

        recipeNameEditText = findViewById(R.id.editRecipeNameEditText);
        ingredientsEditText = findViewById(R.id.editIngredientsEditText);
        instructionsEditText = findViewById(R.id.editInstructionsEditText);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

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
                updateRecipe(recipeName, updatedName, updatedIngredients, updatedInstructions);
            }
        });

        // Delete button click listener
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before deleting
                showConfirmationDialog(recipeName);
            }
        });
    }

    // Method to show confirmation dialog before deleting
    private void showConfirmationDialog(final String recipeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this recipe?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the recipe from the database
                deleteRecipe(recipeName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Dismiss the dialog
            }
        });
        builder.show();
    }

    // Method to update a recipe in the database
    private void updateRecipe(String oldName, String newName, String newIngredients, String newInstructions) {
        String sql = "UPDATE recipes SET name='" + newName + "', ingredients='" + newIngredients + "', instructions='" + newInstructions + "' WHERE name='" + oldName + "'";
        try {
            recipeDatabase.execSQL(sql);
            Toast.makeText(EditRecipeActivity.this, "Recipe updated", Toast.LENGTH_SHORT).show();
            // Set the result to indicate that the recipe was updated
            Intent resultIntent = new Intent();
            resultIntent.putExtra("recipeUpdated", true);
            setResult(RESULT_OK, resultIntent);
            finish(); // Finish the activity
        } catch (Exception e) {
            Toast.makeText(EditRecipeActivity.this, "Error updating recipe", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Method to delete a recipe from the database
    private void deleteRecipe(String recipeName) {
        String sql = "DELETE FROM recipes WHERE name='" + recipeName + "'";
        try {
            recipeDatabase.execSQL(sql);
            Toast.makeText(EditRecipeActivity.this, "Recipe deleted", Toast.LENGTH_SHORT).show();
            // Set the result to indicate that the recipe was deleted
            Intent resultIntent = new Intent();
            resultIntent.putExtra("recipeDeleted", true);
            setResult(RESULT_OK, resultIntent);
            finish(); // Finish the activity
        } catch (Exception e) {
            Toast.makeText(EditRecipeActivity.this, "Error deleting recipe", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
