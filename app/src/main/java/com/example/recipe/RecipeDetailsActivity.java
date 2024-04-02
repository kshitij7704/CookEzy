package com.example.recipe;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class RecipeDetailsActivity extends AppCompatActivity {

    SQLiteDatabase recipeDatabase;
    ListView listView;
    Button clearDatabaseButton;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        recipeDatabase = openOrCreateDatabase("recipeDatabase", MODE_PRIVATE, null);
        listView = findViewById(R.id.listView);
        clearDatabaseButton = findViewById(R.id.clearDatabaseButton);
        backButton = findViewById(R.id.backButton);

        ArrayList<String> recipeNames = new ArrayList<>();

        // Fetch all recipe names from the database
        Cursor cursor = recipeDatabase.rawQuery("SELECT name FROM recipes", null);
        if (cursor.moveToFirst()) {
            do {
                recipeNames.add(cursor.getString(cursor.getColumnIndex("name")));
            } while (cursor.moveToNext());
        }

        cursor.close();

        // Populate ListView with recipe names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recipeNames);
        listView.setAdapter(adapter);

        // Set item click listener on ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Fetch instructions and ingredients for the clicked recipe
                String recipeName = (String) parent.getItemAtPosition(position);
                Cursor cursor = recipeDatabase.rawQuery("SELECT * FROM recipes WHERE name=?", new String[]{recipeName});
                if (cursor.moveToFirst()) {
                    String ingredients = cursor.getString(cursor.getColumnIndex("ingredients"));
                    String instructions = cursor.getString(cursor.getColumnIndex("instructions"));
                    // Display ingredients and instructions in toast message
                    String message = "Ingredients: " + ingredients + "\n" + "Instructions: " + instructions;
                    Toast.makeText(RecipeDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                }
                cursor.close();
            }
        });

        // Set click listener on Clear Database button
        clearDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDatabase();
                // Clear the ListView
                recipeNames.clear();
                adapter.notifyDataSetChanged();
            }
        });

        // Set click listener on Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void clearDatabase() {
        recipeDatabase.execSQL("DELETE FROM recipes");
        Toast.makeText(this, "Database cleared", Toast.LENGTH_SHORT).show();
    }


}
