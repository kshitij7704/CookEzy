package com.example.recipe;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class EditRecipeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_SEND_SMS = 1;

    SQLiteDatabase recipeDatabase;
    EditText recipeNameEditText;
    EditText ingredientsEditText;
    EditText instructionsEditText;
    Button updateButton;
    Button deleteButton;
    Button sendSmsButton;
    Button resetButton;

    private static final String CHANNEL_ID = "recipe_notification_channel";

    // Variables to store initial values of the recipe fields
    private String initialRecipeName;
    private String initialIngredients;
    private String initialInstructions;

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
        sendSmsButton = findViewById(R.id.sendSmsButton);
        resetButton = findViewById(R.id.resetButton);

        final String recipeName = getIntent().getStringExtra("recipeName");

        Cursor cursor = recipeDatabase.rawQuery("SELECT * FROM recipes WHERE name=?", new String[]{recipeName});
        if (cursor.moveToFirst()) {
            initialRecipeName = cursor.getString(cursor.getColumnIndex("name"));
            initialIngredients = cursor.getString(cursor.getColumnIndex("ingredients"));
            initialInstructions = cursor.getString(cursor.getColumnIndex("instructions"));
            recipeNameEditText.setText(initialRecipeName);
            ingredientsEditText.setText(initialIngredients);
            instructionsEditText.setText(initialInstructions);
        }
        cursor.close();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedName = recipeNameEditText.getText().toString().trim();
                String updatedIngredients = ingredientsEditText.getText().toString().trim();
                String updatedInstructions = instructionsEditText.getText().toString().trim();

                // Validation for updated fields
                if (updatedName.isEmpty() || updatedIngredients.isEmpty() || updatedInstructions.isEmpty()) {
                    Toast.makeText(EditRecipeActivity.this, "Recipe name, ingredients, and instructions cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (updatedName.matches("^\\s*$") || updatedIngredients.matches("^\\s*$") || updatedInstructions.matches("^\\s*$")) {
                    Toast.makeText(EditRecipeActivity.this, "Recipe name, ingredients, and instructions cannot consist only of spaces", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateRecipe(recipeName, updatedName, updatedIngredients, updatedInstructions);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog(recipeName);
            }
        });

        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });
    }

    private void showConfirmationDialog(final String recipeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this recipe?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRecipe(recipeName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void updateRecipe(String oldName, String newName, String newIngredients, String newInstructions) {
        String sql = "UPDATE recipes SET name='" + newName + "', ingredients='" + newIngredients + "', instructions='" + newInstructions + "' WHERE name='" + oldName + "'";
        try {
            recipeDatabase.execSQL(sql);
            displayNotification("Recipe Updated", "Recipe has been updated successfully.");
            Toast.makeText(EditRecipeActivity.this,"Recipe Updated",Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("recipeUpdated", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            Toast.makeText(EditRecipeActivity.this, "Error updating recipe", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void deleteRecipe(String recipeName) {
        String sql = "DELETE FROM recipes WHERE name='" + recipeName + "'";
        try {
            recipeDatabase.execSQL(sql);
            displayNotification("Recipe Deleted", "Recipe has been deleted successfully.");
            Toast.makeText(EditRecipeActivity.this,"Recipe Deleted",Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("recipeDeleted", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            Toast.makeText(EditRecipeActivity.this, "Error deleting recipe", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sendSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
        } else {
            showSmsDialog();
        }
    }

    private void showSmsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Phone Number");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String phoneNumber = input.getText().toString().trim();
                if (!phoneNumber.isEmpty()) {
                    sendSmsMessage(phoneNumber);
                } else {
                    Toast.makeText(EditRecipeActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sendSmsMessage(String phoneNumber) {
        String recipeName = recipeNameEditText.getText().toString();
        String ingredients = ingredientsEditText.getText().toString();
        String instructions = instructionsEditText.getText().toString();
        String message ="Recipe Details are as follows:\n"+ "Recipe: " + recipeName + "\nIngredients: " + ingredients + "\nInstructions: " + instructions;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(EditRecipeActivity.this, "SMS sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(EditRecipeActivity.this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.e("SMS", "Error: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSmsDialog();
            } else {
                Toast.makeText(this, "Permission denied. Cannot send SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.start)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Notification ID allows you to update or cancel the notification later on
        int notificationId = 1; // Change this to a unique ID for each notification

        // Display the notification
        notificationManager.notify(notificationId, builder.build());
    }

    // Method to reset the fields to their initial values
    private void resetFields() {
        recipeNameEditText.setText(initialRecipeName);
        ingredientsEditText.setText(initialIngredients);
        instructionsEditText.setText(initialInstructions);
        Toast.makeText(EditRecipeActivity.this, "Fields reset to initial values", Toast.LENGTH_SHORT).show();
    }
}
