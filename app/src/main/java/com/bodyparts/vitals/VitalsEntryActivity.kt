package com.bodyparts.vitals

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bodyparts.vitals.databinding.ActivityVitalsEntryBinding
import com.bodyparts.vitals.model.BodyPart
import com.bodyparts.vitals.model.BodyPartsVitalsSubmission
import com.bodyparts.vitals.model.Vitals
import com.bodyparts.vitals.repository.VitalsRepository
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class VitalsEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVitalsEntryBinding
    private val repository = VitalsRepository()
    private val bodyPartVitals = mutableMapOf<String, Vitals>()
    private lateinit var selectedBodyPartIds: List<String>

    private val bodyPartNames = mapOf(
        "head" to "Head",
        "chest" to "Chest",
        "abdomen" to "Abdomen",
        "left_arm" to "Left Arm",
        "right_arm" to "Right Arm",
        "left_leg" to "Left Leg",
        "right_leg" to "Right Leg"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVitalsEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Enter Vitals"

        selectedBodyPartIds = intent.getStringArrayListExtra("selected_body_parts") ?: emptyList()

        if (selectedBodyPartIds.isEmpty()) {
            Toast.makeText(this, "No body parts selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        // Create input forms for each selected body part
        for (bodyPartId in selectedBodyPartIds) {
            addBodyPartVitalsForm(bodyPartId)
        }

        binding.btnSubmit.setOnClickListener {
            if (validateAndCollectData()) {
                submitToAI()
            }
        }
    }

    private fun addBodyPartVitalsForm(bodyPartId: String) {
        val inflater = LayoutInflater.from(this)
        val formView = inflater.inflate(R.layout.vitals_form_item, binding.vitalsContainer, false)

        val titleText = formView.findViewById<TextView>(R.id.tvBodyPartTitle)
        val temperatureInput = formView.findViewById<TextInputLayout>(R.id.tilTemperature)
        val heartRateInput = formView.findViewById<TextInputLayout>(R.id.tilHeartRate)
        val bloodPressureInput = formView.findViewById<TextInputLayout>(R.id.tilBloodPressure)
        val painLevelInput = formView.findViewById<TextInputLayout>(R.id.tilPainLevel)
        val notesInput = formView.findViewById<TextInputLayout>(R.id.tilNotes)

        titleText.text = bodyPartNames[bodyPartId] ?: bodyPartId

        formView.tag = bodyPartId
        binding.vitalsContainer.addView(formView)
    }

    private fun validateAndCollectData(): Boolean {
        bodyPartVitals.clear()

        for (i in 0 until binding.vitalsContainer.childCount) {
            val formView = binding.vitalsContainer.getChildAt(i)
            val bodyPartId = formView.tag as String

            val temperature = formView.findViewById<TextInputLayout>(R.id.tilTemperature)
                .editText?.text.toString()
            val heartRate = formView.findViewById<TextInputLayout>(R.id.tilHeartRate)
                .editText?.text.toString()
            val bloodPressure = formView.findViewById<TextInputLayout>(R.id.tilBloodPressure)
                .editText?.text.toString()
            val painLevel = formView.findViewById<TextInputLayout>(R.id.tilPainLevel)
                .editText?.text.toString()
            val notes = formView.findViewById<TextInputLayout>(R.id.tilNotes)
                .editText?.text.toString()

            // At least one field should be filled
            if (temperature.isBlank() && heartRate.isBlank() && bloodPressure.isBlank() &&
                painLevel.isBlank() && notes.isBlank()
            ) {
                Toast.makeText(
                    this,
                    "Please enter at least one vital for ${bodyPartNames[bodyPartId]}",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            bodyPartVitals[bodyPartId] = Vitals(
                bodyPartId = bodyPartId,
                temperature = temperature,
                heartRate = heartRate,
                bloodPressure = bloodPressure,
                painLevel = painLevel,
                notes = notes
            )
        }

        return true
    }

    private fun submitToAI() {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Submitting to AI...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            try {
                val bodyParts = selectedBodyPartIds.map { id ->
                    BodyPart(
                        id = id,
                        name = bodyPartNames[id] ?: id,
                        region = android.graphics.RectF(),
                        isSelected = true,
                        vitals = bodyPartVitals[id]
                    )
                }

                val submission = BodyPartsVitalsSubmission(
                    timestamp = System.currentTimeMillis(),
                    selectedBodyParts = bodyParts,
                    vitalsData = bodyPartVitals.values.toList()
                )

                val result = repository.submitToAI(submission)

                progressDialog.dismiss()

                result.onSuccess { response ->
                    showResultDialog(response.message, response.diagnosis, response.recommendations)
                }.onFailure { exception ->
                    showErrorDialog(exception.message ?: "Unknown error occurred")
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                showErrorDialog(e.message ?: "Error submitting data")
            }
        }
    }

    private fun showResultDialog(message: String, diagnosis: String?, recommendations: List<String>?) {
        val dialogMessage = buildString {
            append(message)
            diagnosis?.let {
                append("\n\nDiagnosis:\n$it")
            }
            recommendations?.let {
                if (it.isNotEmpty()) {
                    append("\n\nRecommendations:\n")
                    it.forEachIndexed { index, rec ->
                        append("${index + 1}. $rec\n")
                    }
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle("AI Analysis Result")
            .setMessage(dialogMessage)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
