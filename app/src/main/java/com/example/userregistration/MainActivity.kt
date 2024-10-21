package com.example.userregistration

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
class MainActivity : AppCompatActivity() {
    // Firebase Database reference
    private lateinit var database: DatabaseReference
    // UI elements
    private lateinit var nameEditText: EditText
    private lateinit var idEditText: EditText
    private lateinit var mobileEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var courseSpinner: Spinner
    private lateinit var submitButton: Button
    // Course Options
    private val courseOptions = arrayOf("NVQ 5", "NVQ 4", "NVQ 3", "Other")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize UI elements
        nameEditText = findViewById(R.id.nameEditText)
        idEditText = findViewById(R.id.idEditText)
        mobileEditText = findViewById(R.id.mobileEditText)
        addressEditText = findViewById(R.id.addressEditText)
        courseSpinner = findViewById(R.id.courseSpinner)
        submitButton = findViewById(R.id.submitButton)
        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference
        // Set up Spinner for course selection
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = adapter
        // Check SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }
        // Handle Submit Button Click
        submitButton.setOnClickListener {
            submitStudentDetails()
        }
    }
    // Function to submit student details and send SMS
    private fun submitStudentDetails() {
        val studentName = nameEditText.text.toString()
        val studentId = idEditText.text.toString()
        val studentMobile = mobileEditText.text.toString()
        val studentAddress = addressEditText.text.toString()
        val selectedCourse = courseSpinner.selectedItem.toString()
        // Validate inputs
        if (studentName.isNotEmpty() && studentId.isNotEmpty() && studentMobile.isNotEmpty() &&
            studentAddress.isNotEmpty()) {
            // Generate a unique reference number (for simplicity, using timestamp)
            val refNumber = System.currentTimeMillis().toString()
            // Save data in Firebase
            val studentData = Student(studentName, studentId, studentMobile, studentAddress,
                selectedCourse, refNumber)
            database.child("students").child(refNumber).setValue(studentData)
            // Send SMS to student
            val studentMessage = "You have applied for $selectedCourse with reference number$refNumber."
            sendSMS(studentMobile, studentMessage)
            // Send SMS to teacher (use hardcoded teacher number)
            val teacherMobile = "1234567890" // Teacher's phone number
            val teacherMessage = "Student: $studentName (ID: $studentId, Mobile: $studentMobile) hasapplied for $selectedCourse. Ref number: $refNumber."
            sendSMS(teacherMobile, teacherMessage)
            Toast.makeText(this, "Student registered successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }
    // Function to send SMS
    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SMS sent.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "SMS failed to send.", Toast.LENGTH_SHORT).show()
        }
    }
    // Data class for student
    data class Student(
        val name: String,
        val id: String,
        val mobile: String,
        val address: String,
        val course: String,
        val refNumber: String
    )
}
