package com.example.latlongcoordmapper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cordListRV: RecyclerView = findViewById(R.id.rv_cord_list)
        val cordEntryET = findViewById<EditText>(R.id.et_cord_entry)
        val addCordBtn = findViewById<Button>(R.id.btn_add_cord)
        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinator_layout)

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


        //cordListTV.text = "Lat=0.0 Long=0.0"
    }
}