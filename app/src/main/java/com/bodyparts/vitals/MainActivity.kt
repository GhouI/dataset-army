package com.bodyparts.vitals

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bodyparts.vitals.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.bodyPartsView.onBodyPartSelectedListener = { bodyPart ->
            Toast.makeText(
                this,
                "${bodyPart.name} ${if (bodyPart.isSelected) "selected" else "deselected"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnNext.setOnClickListener {
            val selectedParts = binding.bodyPartsView.getSelectedBodyParts()
            if (selectedParts.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please select at least one body part",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(this, VitalsEntryActivity::class.java)
                intent.putExtra(
                    "selected_body_parts",
                    ArrayList(selectedParts.map { it.id })
                )
                startActivity(intent)
            }
        }

        binding.btnClear.setOnClickListener {
            binding.bodyPartsView.clearSelections()
            Toast.makeText(this, "Selections cleared", Toast.LENGTH_SHORT).show()
        }
    }
}
