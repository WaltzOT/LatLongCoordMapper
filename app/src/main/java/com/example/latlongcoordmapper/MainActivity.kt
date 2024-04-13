package com.example.latlongcoordmapper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cordListTV: TextView = findViewById(R.id.tv_cord_list)
        val cordEntryET = findViewById<EditText>(R.id.et_cord_entry)
        val addCordBtn = findViewById<Button>(R.id.btn_add_cord)

        val cordList = mutableListOf<String>()

        addCordBtn.setOnClickListener{
            val newCord = cordEntryET.text.toString()
            if(!TextUtils.isEmpty(newCord)) {
                cordEntryET.setText("")
                cordList.add(0, newCord)
                cordListTV.text = cordList.joinToString(separator = "\n\n")
            }
        }


        //cordListTV.text = "Lat=0.0 Long=0.0"
    }
}