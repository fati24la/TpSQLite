package com.example.tpsqlite;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tpsqlite.classes.Etudiant;
import com.example.tpsqlite.service.EtudiantService;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nom;
    private EditText prenom;
    private EditText dateNaissance;
    private EditText photoPath;
    private Button btnChoisirPhoto;
    private Button add;

    private EditText id;
    private Button rechercher;
    private Button supprimer;
    private TextView res;

    private Button listerEtudiants;
    private String selectedPhotoPath = "";

    //Méthode pour vider les champs après l'ajout
    void clear(){
        nom.setText("");
        prenom.setText("");
        dateNaissance.setText("");
        photoPath.setText("");
        selectedPhotoPath = "";
        id.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EtudiantService es = new EtudiantService(this);

        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        dateNaissance = findViewById(R.id.dateNaissance);
        photoPath = findViewById(R.id.photoPath);
        btnChoisirPhoto = findViewById(R.id.btnChoisirPhoto);
        add = findViewById(R.id.bn);

        id = findViewById(R.id.id);
        rechercher = findViewById(R.id.rechercher);
        supprimer = findViewById(R.id.supprimer);
        res = findViewById(R.id.res);

        listerEtudiants = findViewById(R.id.listerEtudiants);

        // Date picker dialog pour la date de naissance
        dateNaissance.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        String date = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                        dateNaissance.setText(date);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // Photo selection listener
        btnChoisirPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Add student listener
        add.setOnClickListener(v -> {
            // Validate inputs
            if (nom.getText().toString().isEmpty() || prenom.getText().toString().isEmpty()) {
                Toast.makeText(this, "Nom et Prénom sont obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create student object
            Etudiant etudiant = new Etudiant(
                    nom.getText().toString(),
                    prenom.getText().toString(),
                    dateNaissance.getText().toString(),
                    selectedPhotoPath
            );

            // Insert student
            es.create(etudiant);
            clear();
            Toast.makeText(this, "Étudiant ajouté", Toast.LENGTH_SHORT).show();
        });

        // Rechercher button listener
        rechercher.setOnClickListener(v -> {
            if (id.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Veuillez saisir un ID", Toast.LENGTH_SHORT).show();
                return;
            }

            int etudiantId = Integer.parseInt(id.getText().toString());
            Etudiant etudiant = es.findById(etudiantId);

            if (etudiant != null) {
                res.setText("Nom: " + etudiant.getNom() +
                        "\nPrénom: " + etudiant.getPrenom() +
                        "\nDate de naissance: " + etudiant.getDateNaissance());
            } else {
                res.setText("Aucun étudiant trouvé avec cet ID");
            }
        });

        // Supprimer button listener
        supprimer.setOnClickListener(v -> {
            if (id.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Veuillez saisir un ID", Toast.LENGTH_SHORT).show();
                return;
            }

            int etudiantId = Integer.parseInt(id.getText().toString());
            Etudiant etudiant = es.findById(etudiantId);

            if (etudiant != null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirmation")
                        .setMessage("Voulez-vous vraiment supprimer cet étudiant?")
                        .setPositiveButton("Oui", (dialog, which) -> {
                            es.delete(etudiant);
                            clear();
                            res.setText("");
                            Toast.makeText(MainActivity.this, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Non", null)
                        .show();
            } else {
                Toast.makeText(MainActivity.this, "Aucun étudiant trouvé avec cet ID", Toast.LENGTH_SHORT).show();
            }
        });

        // Lister étudiants button listener
        listerEtudiants.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListStudentsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Get the actual path of the image
                selectedPhotoPath = getRealPathFromURI(selectedImageUri);
                photoPath.setText(selectedPhotoPath);
            }
        }
    }

    // Helper method to get real path from URI
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        android.database.Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);

        if (cursor == null) return contentUri.getPath();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}