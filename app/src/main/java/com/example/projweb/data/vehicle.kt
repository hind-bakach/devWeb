package com.example.projweb.data

data class Vehicle(
    val id: Int = 0,
    val marque: String,
    val modele: String,
    val annee: String,
    val immatriculation: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
