package com.example.projweb

import android.os.Bundle
import android.content.Intent
import com.example.projweb.data.DatabaseHelper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AjoutVehiculeActivity : AppCompatActivity() {  // ← nom changé

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajout_vehicule) // ← layout changé

        dbHelper = DatabaseHelper(this)

        // Bouton retour
        findViewById<android.widget.Button>(R.id.btnRetour).setOnClickListener {
            finish()
        }

        val etMarque = findViewById<EditText>(R.id.etMarque)
        val etModele = findViewById<EditText>(R.id.etModele)
        val etAnnee = findViewById<EditText>(R.id.etAnnee)
        val etImmatriculation = findViewById<EditText>(R.id.etImmatriculation)
        val btnAjouter = findViewById<Button>(R.id.btnAjouter)

        btnAjouter.setOnClickListener {
            val marque = etMarque.text.toString().trim()
            val modele = etModele.text.toString().trim()
            val annee = etAnnee.text.toString().trim()
            val immatriculation = etImmatriculation.text.toString().trim()

            if (marque.isEmpty() || modele.isEmpty() || annee.isEmpty() || immatriculation.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show()
            } else {
                val succes = dbHelper.ajouterVehicule(marque, modele, annee, immatriculation)
                if (succes) {
                    Toast.makeText(this, "Véhicule ajouté avec succès !", Toast.LENGTH_LONG).show()
                    finish() // ← retour vers la liste
                } else {
                    Toast.makeText(this, "Erreur lors de l'ajout.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}