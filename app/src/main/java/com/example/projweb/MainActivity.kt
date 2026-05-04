package com.example.projweb

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projweb.data.DatabaseHelper
import com.example.projweb.data.VehicleAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        chargerVehicules()

        // Bouton ajouter un véhicule
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(Intent(this, AjoutVehiculeActivity::class.java))
        }

        // Bouton déconnexion
        findViewById<Button>(R.id.btnDeconnexion).setOnClickListener {
            // Effacer la session
            val session = getSharedPreferences("session", MODE_PRIVATE)
            session.edit().putBoolean("connecte", false).apply()

            // Retourner à l'écran de connexion
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        chargerVehicules()
    }

    private fun chargerVehicules() {
        val vehicles = db.getAllVehicles()
        recyclerView.adapter = VehicleAdapter(vehicles) { vehicle ->
            val intent = Intent(this, DetailVehiculeActivity::class.java)
            intent.putExtra("id", vehicle.id)
            intent.putExtra("marque", vehicle.marque)
            intent.putExtra("modele", vehicle.modele)
            intent.putExtra("annee", vehicle.annee)
            intent.putExtra("immatriculation", vehicle.immatriculation)
            intent.putExtra("latitude", vehicle.latitude)
            intent.putExtra("longitude", vehicle.longitude)
            startActivity(intent)
        }
    }
}