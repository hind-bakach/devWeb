package com.example.projweb

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projweb.data.DatabaseHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DetailVehiculeActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var db: DatabaseHelper

    private var vehicleId = 0
    private var voitureLatitude = 0.0
    private var voitureLongitude = 0.0

    val PERMISSION_GPS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_detail_vehicule)

        db = DatabaseHelper(this)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        // Bouton retour
        findViewById<Button>(R.id.btnRetour).setOnClickListener { finish() }

        // Récupérer les données du véhicule
        vehicleId        = intent.getIntExtra("id", 0)
        val marque       = intent.getStringExtra("marque") ?: ""
        val modele       = intent.getStringExtra("modele") ?: ""
        val annee        = intent.getStringExtra("annee") ?: ""
        val immat        = intent.getStringExtra("immatriculation") ?: ""
        voitureLatitude  = intent.getDoubleExtra("latitude", 0.0)
        voitureLongitude = intent.getDoubleExtra("longitude", 0.0)

        // Afficher les infos
        findViewById<TextView>(R.id.tvDetailMarque).text = "Marque : $marque"
        findViewById<TextView>(R.id.tvDetailModele).text = "Modèle : $modele"
        findViewById<TextView>(R.id.tvDetailAnnee).text = "Année : $annee"
        findViewById<TextView>(R.id.tvDetailImmat).text = "Immatriculation : $immat"

        // Initialiser la carte
        map = findViewById(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.0)
        map.controller.setCenter(GeoPoint(33.5731, -7.5898))

        // Préparer le callback GPS
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                afficherMarqueurMoi(loc.latitude, loc.longitude)
                fusedClient.removeLocationUpdates(this)
            }
        }

        // Afficher la position sauvegardée de la voiture (si elle existe)
        mettreAJourAffichageVoiture()

        // Bouton sauvegarder la position
        findViewById<Button>(R.id.btnSauvegarderPosition).setOnClickListener {
            sauvegarderPositionVoiture()
        }

        // Demander permission GPS pour afficher "ma position"
        demanderPermissionGPS()
    }

    // Affiche le marqueur rouge de la voiture + met à jour le statut
    fun mettreAJourAffichageVoiture() {
        val tvStatut = findViewById<TextView>(R.id.tvStatutPosition)
        if (voitureLatitude != 0.0 || voitureLongitude != 0.0) {
            tvStatut.text = "Position sauvegardée : (${"%.4f".format(voitureLatitude)}, ${"%.4f".format(voitureLongitude)})"
            tvStatut.setTextColor(android.graphics.Color.parseColor("#10B981"))
            afficherMarqueurVoiture(voitureLatitude, voitureLongitude)
            // Centrer la carte sur la voiture
            map.controller.setCenter(GeoPoint(voitureLatitude, voitureLongitude))
        } else {
            tvStatut.text = "Aucune position sauvegardée"
        }
    }

    // Marqueur rouge = position de la voiture
    fun afficherMarqueurVoiture(lat: Double, lng: Double) {
        val point = GeoPoint(lat, lng)
        val marqueur = Marker(map)
        marqueur.position = point
        marqueur.title = "Ma voiture"
        marqueur.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        // Retirer l'ancien marqueur voiture si existant
        map.overlays.removeAll { it is Marker && (it as Marker).title == "Ma voiture" }
        map.overlays.add(marqueur)
        map.invalidate()
    }

    // Marqueur bleu = position de l'utilisateur
    fun afficherMarqueurMoi(lat: Double, lng: Double) {
        val point = GeoPoint(lat, lng)
        val marqueur = Marker(map)
        marqueur.position = point
        marqueur.title = "Ma position"
        marqueur.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.removeAll { it is Marker && (it as Marker).title == "Ma position" }
        map.overlays.add(marqueur)
        map.invalidate()
    }

    // Sauvegarder la position GPS actuelle comme position de la voiture
    fun sauvegarderPositionVoiture() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission GPS requise", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Localisation en cours...", Toast.LENGTH_SHORT).show()

        // Forcer une nouvelle lecture GPS fraîche (ignorer le cache)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setMaxUpdates(1)
            .setMinUpdateIntervalMillis(0)
            .build()

        val callbackSauvegarde = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    voitureLatitude = location.latitude
                    voitureLongitude = location.longitude
                    db.sauvegarderPosition(vehicleId, voitureLatitude, voitureLongitude)
                    mettreAJourAffichageVoiture()
                    Toast.makeText(
                        this@DetailVehiculeActivity,
                        "Position sauvegardée !",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@DetailVehiculeActivity,
                        "GPS pas encore prêt, réessaie",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                fusedClient.removeLocationUpdates(this)
            }
        }

        fusedClient.requestLocationUpdates(request, callbackSauvegarde, Looper.getMainLooper())
    }

    fun demanderPermissionGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            obtenirPositionUtilisateur()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_GPS
            )
        }
    }

    fun obtenirPositionUtilisateur() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                afficherMarqueurMoi(location.latitude, location.longitude)
                fusedClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_GPS
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenirPositionUtilisateur()
        } else {
            Toast.makeText(this, "Permission GPS refusée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        fusedClient.removeLocationUpdates(locationCallback)
    }
}