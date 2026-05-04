package com.example.projweb

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    // Identifiants fixes (simple pour débutant)
    val EMAIL_CORRECT = "admin@gmail.com"
    val MOT_DE_PASSE_CORRECT = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Si l'utilisateur est déjà connecté, on va directement à l'accueil
        val session = getSharedPreferences("session", MODE_PRIVATE)
        if (session.getBoolean("connecte", false)) {
            allerVersAccueil()
            return
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvErreur = findViewById<TextView>(R.id.tvErreur)
        val btnConnecter = findViewById<Button>(R.id.btnConnecter)

        btnConnecter.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val motDePasse = etPassword.text.toString().trim()

            // Vérification champs vides
            if (email.isEmpty()) {
                tvErreur.text = "Veuillez entrer votre email."
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                tvErreur.text = "Format d'email invalide."
                return@setOnClickListener
            }

            if (motDePasse.isEmpty()) {
                tvErreur.text = "Veuillez entrer votre mot de passe."
                return@setOnClickListener
            }

            // Vérification identifiants
            if (email == EMAIL_CORRECT && motDePasse == MOT_DE_PASSE_CORRECT) {
                // Sauvegarder la session
                session.edit().putBoolean("connecte", true).apply()
                allerVersAccueil()
            } else {
                tvErreur.text = "Email ou mot de passe incorrect."
            }
        }
    }

    fun allerVersAccueil() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // ferme LoginActivity pour ne pas revenir en arrière
    }
}