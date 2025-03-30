package com.example.tpsqlite.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpsqlite.R;
import com.example.tpsqlite.classes.Etudiant;
import com.example.tpsqlite.service.EtudiantService;

import java.io.File;
import java.util.List;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
    private List<Etudiant> etudiants;
    private Context context;
    private OnEtudiantActionListener actionListener;

    public interface OnEtudiantActionListener {
        void onEditEtudiant(Etudiant etudiant);
    }

    public EtudiantAdapter(Context context, List<Etudiant> etudiants, OnEtudiantActionListener listener) {
        this.context = context;
        this.etudiants = etudiants;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant etudiant = etudiants.get(position);
        holder.textViewNom.setText(etudiant.getNom());
        holder.textViewPrenom.setText(etudiant.getPrenom());
        holder.textViewId.setText("ID: " + etudiant.getId());
        holder.textViewDateNaissance.setText("Né(e) le: " + etudiant.getDateNaissance());

        // Set photo
        if (etudiant.getPhotoPath() != null && !etudiant.getPhotoPath().isEmpty()) {
            File imgFile = new File(etudiant.getPhotoPath());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.imageViewPhoto.setImageBitmap(myBitmap);
            }
        }

        // Configuration des actions sur l'élément
        holder.itemView.setOnClickListener(v -> {
            // Afficher un dialogue avec des options
            new AlertDialog.Builder(context)
                    .setTitle("Actions pour " + etudiant.getNom() + " " + etudiant.getPrenom())
                    .setItems(new CharSequence[]{"Modifier", "Supprimer"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // Modifier
                                if (actionListener != null) {
                                    actionListener.onEditEtudiant(etudiant);
                                }
                                break;
                            case 1: // Supprimer
                                confirmerSuppression(etudiant);
                                break;
                        }
                    })
                    .create()
                    .show();
        });
    }

    private void confirmerSuppression(Etudiant etudiant) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmation de suppression")
                .setMessage("Voulez-vous vraiment supprimer " + etudiant.getNom() + " " + etudiant.getPrenom() + " ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    EtudiantService service = new EtudiantService(context);
                    service.delete(etudiant);

                    // Supprimer de la liste et mettre à jour
                    int index = etudiants.indexOf(etudiant);
                    if (index != -1) {
                        etudiants.remove(index);
                        notifyItemRemoved(index);
                    }

                    Toast.makeText(context, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNom;
        TextView textViewPrenom;
        TextView textViewId;
        TextView textViewDateNaissance;
        ImageView imageViewPhoto;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNom = itemView.findViewById(R.id.textViewNom);
            textViewPrenom = itemView.findViewById(R.id.textViewPrenom);
            textViewId = itemView.findViewById(R.id.textViewId);
            textViewDateNaissance = itemView.findViewById(R.id.textViewDateNaissance);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
        }
    }
}