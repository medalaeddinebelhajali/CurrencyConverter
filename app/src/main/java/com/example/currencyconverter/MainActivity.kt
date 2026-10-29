package com.example.currencyconverter

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.currencyconverter.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // View Binding : accès aux vues sans findViewById()
    private lateinit var binding: ActivityMainBinding

    // Taux fixes fournis dans le TP, exprimés pour 1 EUR
    private val taux = mapOf(
        "EUR" to 1.0,
        "USD" to 1.17,
        "TND" to 3.40,
        "GBP" to 0.87,
        "JPY" to 172.50,
        "CAD" to 1.62,
    )

    private var premierChiffre = true
    private var resultatAffiche = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurerDevises()
        configurerClavier()
        configurerConversion()
        configurerInversion()
    }

    // Remplir les deux Spinner avec le tableau devises
    private fun configurerDevises() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.devises,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spDeviseSource.adapter = adapter
        binding.spDeviseCible.adapter = adapter

        // Valeurs par défaut : USD vers EUR
        binding.spDeviseSource.setSelection(0)
        binding.spDeviseCible.setSelection(1)
    }

    // Configurer les boutons du clavier numérique
    private fun configurerClavier() {
        val boutonsChiffres = listOf(
            binding.btn0 to "0",
            binding.btn1 to "1",
            binding.btn2 to "2",
            binding.btn3 to "3",
            binding.btn4 to "4",
            binding.btn5 to "5",
            binding.btn6 to "6",
            binding.btn7 to "7",
            binding.btn8 to "8",
            binding.btn9 to "9",
            binding.btnPoint to "."
        )

        boutonsChiffres.forEach { (bouton, caractere) ->
            bouton.setOnClickListener {
                ajouterCaractere(caractere)
            }
        }

        binding.btnC.setOnClickListener {
            binding.tvMontant.text = "0"
            binding.tvResultat.text = ""
            premierChiffre = true
            resultatAffiche = false
        }
    }

    // Ajouter un chiffre ou un point décimal au montant
    private fun ajouterCaractere(c: String) {
        var montantActuel = binding.tvMontant.text.toString()

        // Après une conversion, une nouvelle saisie recommence
        // avec le caractère tapé.
        if (resultatAffiche) {
            montantActuel = "0"
            binding.tvResultat.text = ""
            resultatAffiche = false
            premierChiffre = true
        }

        // Le premier chiffre remplace le zéro initial
        if (premierChiffre) {
            if (c == ".") {
                binding.tvMontant.text = "0."
                premierChiffre = false
            } else {
                binding.tvMontant.text = c
                premierChiffre = false
            }
            return
        }

        // Ignorer une deuxième virgule décimale (point)
        if (c == "." && montantActuel.contains(".")) {
            return
        }

        // Maximum de 10 caractères
        if (montantActuel.length >= 10) {
            return
        }

        // Éviter les zéros inutiles au début
        if (montantActuel == "0" && c != ".") {
            binding.tvMontant.text = c
        } else {
            binding.tvMontant.text = montantActuel + c
        }
    }

    // Configurer le bouton de conversion
    private fun configurerConversion() {
        binding.btnConvertir.setOnClickListener {
            effectuerConversion()
        }
    }

    // Convertir le montant de la devise source vers la devise cible
    private fun effectuerConversion() {
        val montantTexte = binding.tvMontant.text.toString()
        val montant = montantTexte.toDoubleOrNull() ?: 0.0

        if (montant == 0.0) {
            Toast.makeText(
                this,
                getString(R.string.montant_vide),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val source = binding.spDeviseSource.selectedItem.toString()
        val cible = binding.spDeviseCible.selectedItem.toString()

        val resultat = convertir(montant, source, cible)

        binding.tvResultat.text = String.format(
            Locale.US,
            "%.2f",
            resultat
        )

        resultatAffiche = true
        premierChiffre = false
    }

    // Conversion en passant par l'euro comme devise pivot
    private fun convertir(
        montant: Double,
        source: String,
        cible: String
    ): Double {
        if (source == cible) {
            return montant
        }

        val tauxSource = taux[source] ?: return montant
        val tauxCible = taux[cible] ?: return montant

        val montantEnEuro = montant / tauxSource
        return montantEnEuro * tauxCible
    }

    // Échanger les devises source et cible
    private fun configurerInversion() {
        binding.btnInverser.setOnClickListener {
            val ancienneSource = binding.spDeviseSource.selectedItemPosition
            val ancienneCible = binding.spDeviseCible.selectedItemPosition

            binding.spDeviseSource.setSelection(ancienneCible)
            binding.spDeviseCible.setSelection(ancienneSource)

            // Refaire la conversion si un résultat était affiché
            if (resultatAffiche) {
                effectuerConversion()
            }
        }
    }
}