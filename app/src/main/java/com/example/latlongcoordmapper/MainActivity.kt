package com.example.latlongcoordmapper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var dataList: MutableList<Cord>
    private lateinit var appBarConfig: AppBarConfiguration
    private val locationPermissionCode = 2
    private val CREATE_FILE = 101
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cordListRV: RecyclerView = findViewById(R.id.rv_cord_list)
        //val cordEntryET: EditText = findViewById(R.id.et_cord_entry)
        val addCordBtn: Button = findViewById(R.id.btn_add_cord)
        val saveDataBtn: Button = findViewById(R.id.btn_save_data)
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator_layout)

        cordListRV.layoutManager = LinearLayoutManager(this)
        cordListRV.setHasFixedSize(true)

        val adapter = CordAdapter()
        cordListRV.adapter = adapter

        // Location Manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkPermissionAndStartLocationUpdates()

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
            currentLocation?.let { location ->
                val coordinates = "Lat=${location.latitude} Long=${location.longitude}"
                adapter.addCord(Cord(location.latitude, location.longitude, coordinates))
                cordListRV.scrollToPosition(0)
            } ?: Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
        }

        saveDataBtn.setOnClickListener {
            saveData(adapter)
        }
    }

    private fun saveData(adapter: CordAdapter){
        //var dataList: MutableList<Cord> = adapter.getAllCords()
        dataList = adapter.getAllCords()
        createFile()
    }

    private fun MutableList<Cord>.toCsvString(): String{
        val header = "Latitude,Longitude\n"
        return this.joinToString(separator = "", prefix = header) { cord ->
            "${cord.lat},${cord.long}\n"
        }
    }
    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "cordData.csv")

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, CREATE_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 101 && resultCode == Activity.RESULT_OK){
            data?.data?.also{uri ->
                writeCsvToFile(uri)
            }
        }
    }

    private fun writeCsvToFile(uri: Uri){
        val contentResolver = applicationContext.contentResolver
        contentResolver.openFileDescriptor(uri, "w")?.use{ parcelFileDescriptor->
            FileOutputStream(parcelFileDescriptor.fileDescriptor).bufferedWriter().use{
                it.write(getCsvData())
            }
        }
    }

    private fun getCsvData(): String {
        return dataList.toCsvString()
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
    }

    private fun checkPermissionAndStartLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000, // Interval in milliseconds
            0.0001f, // Distance in meters
            this
        )
    }


    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.0001f, this)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

}