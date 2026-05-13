package com.example.projweb

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projweb.data.DatabaseHelper
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class DetailVehiculeActivity : AppCompatActivity() {

    // --- Variables principales ---
    private lateinit var map: MapView
    private lateinit var db: DatabaseHelper

    private var vehicleId = 0
    private var voitureLat = 0.0
    private var voitureLng = 0.0

    // Position par défaut (Casablanca) si GPS indisponible
    private val POSITION_DEFAUT = GeoPoint(33.5731, -7.5898)
    private val CODE_PERMISSION_GPS = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration nécessaire pour osmdroid (cache des tuiles de carte)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_detail_vehicule)
        db = DatabaseHelper(this)

        // 1) Récupérer les infos envoyées par MainActivity
        vehicleId  = intent.getIntExtra("id", 0)
        voitureLat = intent.getDoubleExtra("latitude", 0.0)
        voitureLng = intent.getDoubleExtra("longitude", 0.0)

        findViewById<TextView>(R.id.tvDetailMarque).text = "Marque : ${intent.getStringExtra("marque")}"
        findViewById<TextView>(R.id.tvDetailModele).text = "Modèle : ${intent.getStringExtra("modele")}"
        findViewById<TextView>(R.id.tvDetailAnnee).text  = "Année : ${intent.getStringExtra("annee")}"
        findViewById<TextView>(R.id.tvDetailImmat).text  = "Immatriculation : ${intent.getStringExtra("immatriculation")}"

        // 2) Bouton retour
        findViewById<Button>(R.id.btnRetour).setOnClickListener { finish() }

        // 3) Préparer la carte (centre + clic long pour placer la voiture)
        preparerCarte()

        // 4) Si une position de voiture est déjà enregistrée, on l'affiche
        if (voitureLat != 0.0 || voitureLng != 0.0) {
            ajouterMarqueurVoiture(voitureLat, voitureLng)
            map.controller.setCenter(GeoPoint(voitureLat, voitureLng))
            afficherStatut()
        }

        // 5) Bouton : utiliser ma position GPS comme position de la voiture
        findViewById<Button>(R.id.btnSauvegarderPosition).setOnClickListener {
            sauvegarderMaPositionCommeVoiture()
        }

        // 6) Afficher ma position (pin bleu)
        if (permissionGPSAccordee()) {
            afficherMaPosition()
        } else {
            demanderPermissionGPS()
        }
    }


    // ============ CARTE ============

    private fun preparerCarte() {
        map = findViewById(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.0)
        map.controller.setCenter(POSITION_DEFAUT)

        // Quand on fait un CLIC LONG sur la carte → on place la voiture à cet endroit
        val ecouteur = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?) = false

            override fun longPressHelper(point: GeoPoint?): Boolean {
                if (point != null) {
                    voitureLat = point.latitude
                    voitureLng = point.longitude
                    db.sauvegarderPosition(vehicleId, voitureLat, voitureLng)
                    ajouterMarqueurVoiture(voitureLat, voitureLng)
                    afficherStatut()
                    Toast.makeText(this@DetailVehiculeActivity, "Voiture placée ici", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        map.overlays.add(MapEventsOverlay(ecouteur))
    }

    // Pin ROUGE = position de la voiture
    private fun ajouterMarqueurVoiture(lat: Double, lng: Double) {
        map.overlays.removeAll { it is Marker && it.title == "Ma voiture" }

        val m = Marker(map)
        m.position = GeoPoint(lat, lng)
        m.title = "Ma voiture"
        m.icon = ContextCompat.getDrawable(this, R.drawable.ic_marker_red)
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(m)
        map.invalidate()
    }

    // Pin BLEU = ma position
    private fun ajouterMarqueurMoi(lat: Double, lng: Double) {
        map.overlays.removeAll { it is Marker && it.title == "Ma position" }

        val m = Marker(map)
        m.position = GeoPoint(lat, lng)
        m.title = "Ma position"
        m.icon = ContextCompat.getDrawable(this, R.drawable.ic_marker_blue)
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(m)
        map.invalidate()
    }

    private fun afficherStatut() {
        findViewById<TextView>(R.id.tvStatutPosition).text =
            "Position sauvegardée : (${"%.4f".format(voitureLat)}, ${"%.4f".format(voitureLng)})"
    }


    // ============ GPS (très simple) ============

    private fun afficherMaPosition() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        if (!permissionGPSAccordee()) return

        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                ajouterMarqueurMoi(location.latitude, location.longitude)
            } else {
                // Pas de position GPS dispo → on affiche la position par défaut
                ajouterMarqueurMoi(POSITION_DEFAUT.latitude, POSITION_DEFAUT.longitude)
                Toast.makeText(this, "GPS indisponible, position par défaut affichée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sauvegarderMaPositionCommeVoiture() {
        if (!permissionGPSAccordee()) {
            Toast.makeText(this, "Permission GPS refusée", Toast.LENGTH_SHORT).show()
            return
        }

        val client = LocationServices.getFusedLocationProviderClient(this)
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                voitureLat = location.latitude
                voitureLng = location.longitude
                db.sauvegarderPosition(vehicleId, voitureLat, voitureLng)
                ajouterMarqueurVoiture(voitureLat, voitureLng)
                map.controller.setCenter(GeoPoint(voitureLat, voitureLng))
                afficherStatut()
                Toast.makeText(this, "Position sauvegardée !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "GPS pas prêt, fais un clic long sur la carte à la place", Toast.LENGTH_LONG).show()
            }
        }
    }


    // ============ PERMISSIONS ============

    private fun permissionGPSAccordee(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun demanderPermissionGPS() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            CODE_PERMISSION_GPS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_PERMISSION_GPS &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            afficherMaPosition()
        } else {
            Toast.makeText(this, "Permission refusée, GPS désactivé", Toast.LENGTH_SHORT).show()
        }
    }


    // ============ CYCLE DE VIE DE LA CARTE ============

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}