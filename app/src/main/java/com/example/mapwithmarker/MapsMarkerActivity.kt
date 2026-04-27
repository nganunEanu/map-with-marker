package com.example.mapwithmarker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.*
import android.content.res.ColorStateList

class MapsMarkerActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var tvBpm: TextView
    private lateinit var tvMapLink: TextView
    private lateinit var btnAman: TextView

    private var lastUpdateTime: Long = 0
    private val timeoutMillis = 3 * 60 * 1000L // 3 minutes
    private val handler = Handler(Looper.getMainLooper())

    private val CHANNEL_ID = "firebase_updates"
    private val NOTIFICATION_ID = 101
    private val TAG = "MapsMarkerActivity"

    private val timeoutRunnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            if (now - lastUpdateTime >= timeoutMillis) {
                tvBpm.text = "-"
                tvMapLink.text = "-"
                btnAman.text = "Aman"
                btnAman.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#00ff00"))// bright green
            }
            handler.postDelayed(this, 60 * 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        tvBpm = findViewById(R.id.tvBpm)
        tvMapLink = findViewById(R.id.tvMapLink)
        btnAman = findViewById(R.id.btnAman)

        database = FirebaseDatabase.getInstance().reference
            .child("locations")
            .child("sydney")

        createNotificationChannel()
        setupFirebaseListener()
        handler.post(timeoutRunnable)
    }

    private fun setupFirebaseListener() {
        try {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(
                            this@MapsMarkerActivity,
                            "⚠️ Tidak ada data dari Firebase!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val bpm = snapshot.child("bpm").getValue(Int::class.java)
                    val latitude = snapshot.child("latitude").getValue(Double::class.java)
                    val longitude = snapshot.child("longitude").getValue(Double::class.java)

                    if (bpm != null && latitude != null && longitude != null) {
                        lastUpdateTime = System.currentTimeMillis()

                        tvBpm.text = "$bpm"
                        val mapLink = "https://www.google.com/maps/place/$latitude,$longitude"
                        tvMapLink.text = mapLink
                        tvMapLink.autoLinkMask = Linkify.WEB_URLS
                        tvMapLink.movementMethod = LinkMovementMethod.getInstance()

                        btnAman.text = "Bahaya"
                        btnAman.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#ff0000"))// bright green

                        // 🔔 Notification with Firebase data
                        sendNotification(
                            "Update Data",
                            "BPM: $bpm | Lat: $latitude | Lon: $longitude | Status: ${btnAman.text}"
                        )

                    } else {
                        Toast.makeText(
                            this@MapsMarkerActivity,
                            "⚠️ Data tidak lengkap!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read value", error.toException())
                }
            })
        } catch (e: Exception) {
            Log.e("Firebase", "Error setting listener", e)
        }
    }

    private fun sendNotification(title: String, message: String) {
        Log.d(TAG, "Sending notification: $title - $message")

        val intent = Intent(this, MapsMarkerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // ✅ Request permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
            return
        }

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Firebase Updates"
            val descriptionText = "Notifikasi update data Firebase"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeoutRunnable)
    }
}
