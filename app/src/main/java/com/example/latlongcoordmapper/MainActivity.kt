package com.example.latlongcoordmapper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    lateinit var dataList: MutableList<Cord>
    private lateinit var dataManager: DataManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cordListRV: RecyclerView = findViewById(R.id.rv_cord_list)
        val cordEntryET = findViewById<EditText>(R.id.et_cord_entry)
        val addCordBtn = findViewById<Button>(R.id.btn_add_cord)
        val saveDataBtn = findViewById<Button>(R.id.btn_save_data)
        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinator_layout)

//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
//        }

        //val cordList = mutableListOf<String>()

        cordListRV.layoutManager = LinearLayoutManager(this)
        cordListRV.setHasFixedSize(true)

        val adapter = CordAdapter()
        cordListRV.adapter = adapter

        val itemTouchCallback =  object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val deletedCord = adapter.deleteCord(position)

                val snackbar = Snackbar.make(
                    coordinatorLayout,
                    "Deleted: ${deletedCord.text}",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setAction("UNDO") {
                    adapter.addCord(deletedCord, position)
                }

                snackbar.show()
            }

        }

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(cordListRV)


        addCordBtn.setOnClickListener{
            val newCord = cordEntryET.text.toString()

            if(!TextUtils.isEmpty(newCord)) {
                adapter.addCord(Cord(0, newCord))
                cordListRV.scrollToPosition(0)
                cordEntryET.setText("")
                //cordList.add(0, newCord)
                //cordListTV.text = cordList.joinToString(separator = "\n\n")
            }
        }

//        suspend fun saveDataToFile(data: MutableList<Cord>, file: File) {
//            withContext(Dispatchers.IO) {
//                file.writeText(data.toCsvString())
//            }
//        }
//        fun OutputStream.writeCsv(cords: MutableList<Cord>){
//            val writer = bufferedWriter()
//            writer.write("\"\"\"Lat\", \"Long\"\"\"")
//            writer.newLine()
//            cords.forEach{
//                writer.write("${it.id},${it.text}\"")
//                writer.newLine()
//            }
//            writer.flush()
//        }

        saveDataBtn.setOnClickListener {
            //dataList = adapter.cords
            //createFile(this)
            //FileOutputStream("cordData.csv").apply{writeCsv(adapter.cords)}
        }
    }



    private fun MutableList<Cord>.toCsvString(): String{
        val header = "Latitude,Longitude\n"
        return this.joinToString(separator = "", prefix = header) { cord ->
            "${cord.id},${cord.text}\n"
        }
    }

    private fun createFile(activity: Activity){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply{
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "cordData.csv")
        }
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

}