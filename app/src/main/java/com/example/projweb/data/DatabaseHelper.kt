package com.example.projweb.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "vehicules.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE Vehicules (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                marque TEXT,
                modele TEXT,
                annee TEXT,
                immatriculation TEXT,
                latitude REAL DEFAULT 0.0,
                longitude REAL DEFAULT 0.0
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Ajouter les colonnes latitude/longitude si mise à jour depuis version 1
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE Vehicules ADD COLUMN latitude REAL DEFAULT 0.0")
            db?.execSQL("ALTER TABLE Vehicules ADD COLUMN longitude REAL DEFAULT 0.0")
        }
    }

    fun ajouterVehicule(marque: String, modele: String, annee: String, immatriculation: String): Boolean {
        val db = this.writableDatabase
        val valeurs = ContentValues()
        valeurs.put("marque", marque)
        valeurs.put("modele", modele)
        valeurs.put("annee", annee)
        valeurs.put("immatriculation", immatriculation)

        val resultat = db.insert("Vehicules", null, valeurs)
        return resultat != -1L
    }

    // Sauvegarder la position GPS du véhicule
    fun sauvegarderPosition(id: Int, latitude: Double, longitude: Double) {
        val db = this.writableDatabase
        val valeurs = ContentValues()
        valeurs.put("latitude", latitude)
        valeurs.put("longitude", longitude)
        db.update("Vehicules", valeurs, "id = ?", arrayOf(id.toString()))
    }

    fun getAllVehicles(): List<Vehicle> {
        val list = mutableListOf<Vehicle>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Vehicules", null)

        while (cursor.moveToNext()) {
            val vehicle = Vehicle(
                id              = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                marque          = cursor.getString(cursor.getColumnIndexOrThrow("marque")),
                modele          = cursor.getString(cursor.getColumnIndexOrThrow("modele")),
                annee           = cursor.getString(cursor.getColumnIndexOrThrow("annee")),
                immatriculation = cursor.getString(cursor.getColumnIndexOrThrow("immatriculation")),
                latitude        = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                longitude       = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
            )
            list.add(vehicle)
        }

        cursor.close()
        return list
    }
}