package com.example.tpsqlite;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpsqlite.Adapter.EtudiantAdapter;
import com.example.tpsqlite.classes.Etudiant;
import com.example.tpsqlite.service.EtudiantService;

import java.util.List;

public class ListStudentsActivity extends AppCompatActivity implements EtudiantAdapter.OnEtudiantActionListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EtudiantService etudiantService;
    private List<Etudiant> etudiants;
    private EtudiantAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyListMessage;
    private Etudiant currentEtudiantToEdit;
    private String selectedPhotoPath;
    private AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_students);

        etudiantService = new EtudiantService(this);
        recyclerView = findViewById(R.id.recyclerViewEtudiants);
        emptyListMessage = findViewById(R.id.emptyListMessage);

        // Charger et afficher les étudiants
        loadEtudiants();
    }

    private void loadEtudiants() {
        etudiants = etudiantService.findAll();

        // Gérer l'affichage de la liste vide
        if (etudiants.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyListMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyListMessage.setVisibility(View.GONE);

            adapter = new EtudiantAdapter(this, etudiants, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    @Override
    public void onEditEtudiant(Etudiant etudiant) {
        // Créer une boîte de dialogue pour modifier l'étudiant
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_etudiant, null);
        EditText editNom = dialogView.findViewById(R.id.editNom);
        EditText editPrenom = dialogView.findViewById(R.id.editPrenom);
        EditText editDateNaissance = dialogView.findViewById(R.id.editDateNaissance);
        EditText editPhotoPath = dialogView.findViewById(R.id.editPhotoPath);
        ImageView imageViewPhoto = dialogView.findViewById(R.id.imageViewPhoto);
        Button btnChoisirPhoto = dialogView.findViewById(R.id.btnChoisirPhoto);

        // Pré-remplir les champs
        editNom.setText(etudiant.getNom());
        editPrenom.setText(etudiant.getPrenom());
        editDateNaissance.setText(etudiant.getDateNaissance());
        editPhotoPath.setText(etudiant.getPhotoPath());

        // Configuration du bouton de sélection de photo
        currentEtudiantToEdit = etudiant;
        selectedPhotoPath = etudiant.getPhotoPath();

        // Afficher la photo existante si elle existe
        if (selectedPhotoPath != null && !selectedPhotoPath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(selectedPhotoPath);
            imageViewPhoto.setImageBitmap(bitmap);
        }

        btnChoisirPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Modifier l'étudiant")
                .setView(dialogView)
                .setPositiveButton("Enregistrer", null)
                .setNegativeButton("Annuler", null);

        currentDialog = builder.create();
        currentDialog.show();

        // Set the positive button click listener to handle form validation
        currentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate input fields
            if (editNom.getText().toString().isEmpty() || editPrenom.getText().toString().isEmpty()) {
                Toast.makeText(this, "Nom et Prénom sont obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update student information
            etudiant.setNom(editNom.getText().toString());
            etudiant.setPrenom(editPrenom.getText().toString());
            etudiant.setDateNaissance(editDateNaissance.getText().toString());
            etudiant.setPhotoPath(selectedPhotoPath);

            etudiantService.update(etudiant);

            // Reload student list
            loadEtudiants();

            Toast.makeText(this, "Étudiant modifié", Toast.LENGTH_SHORT).show();
            currentDialog.dismiss();
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

                if (currentDialog != null && currentDialog.isShowing()) {
                    View dialogView = currentDialog.findViewById(android.R.id.content);
                    if (dialogView != null) {
                        ImageView imageViewPhoto = dialogView.findViewById(R.id.imageViewPhoto);
                        EditText editPhotoPath = dialogView.findViewById(R.id.editPhotoPath);

                        if (imageViewPhoto != null) {
                            Bitmap bitmap = BitmapFactory.decodeFile(selectedPhotoPath);
                            imageViewPhoto.setImageBitmap(bitmap);
                        }

                        if (editPhotoPath != null) {
                            editPhotoPath.setText(selectedPhotoPath);
                        }
                    }
                }
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