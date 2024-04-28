package com.example.latlongcoordmapper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cordListRV: RecyclerView = findViewById(R.id.rv_cord_list)
        //val cordEntryET: EditText = findViewById(R.id.et_cord_entry)
        val addCordBtn: Button = findViewById(R.id.btn_add_cord)
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator_layout)

        cordListRV.layoutManager = LinearLayoutManager(this)
        cordListRV.setHasFixedSize(true)

        val adapter = CordAdapter()
        cordListRV.adapter = adapter

        // Location Manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val deletedCord = adapter.deleteCord(position)

                val snackbar = Snackbar.make(
                    coordinatorLayout,
                    "Deleted: ${deletedCord.text}",
                    Snackbar.LENGTH_LONG
                ).apply {
                    setAction("UNDO") {
                        adapter.addCord(deletedCord, position)
                    }
                }
                snackbar.show()
            }
        }

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(cordListRV)

        addCordBtn.setOnClickListener {
            getLocation(adapter)
        }
    }

    private fun getLocation(adapter: CordAdapter) {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, this)
            var location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val coordinates = "Lat=${location?.latitude} Long=${location?.longitude}"
            val cordListRV: RecyclerView = findViewById(R.id.rv_cord_list)
            val adapter = cordListRV.adapter as CordAdapter
            adapter.addCord(Cord(location?.latitude, location?.longitude , coordinates))
            cordListRV.scrollToPosition(0)
        }
    }

    override fun onLocationChanged(location: Location) {
        val coordinates = "Lat=${location.latitude} Long=${location.longitude}"
        val cordListRV: RecyclerView = findViewById(R.id.rv_cord_list)
        val adapter = cordListRV.adapter as CordAdapter
        adapter.addCord(Cord(0, coordinates))
        cordListRV.scrollToPosition(0)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}