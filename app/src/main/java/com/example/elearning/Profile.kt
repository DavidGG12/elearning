package com.example.elearning

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class Profile : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var txtWelcome: TextView
    private lateinit var txtEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //Save the session
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        txtWelcome = findViewById(R.id.txtWelcome)
        txtWelcome.setText("¡Bienvenido " + sharedPreferences.getString("nameUser", "") + "!")

        txtEmail = findViewById(R.id.txtEmail)
        txtEmail.setText(sharedPreferences.getString("emailUser", ""))
    }

    override public fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
        //return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val validate = sharedPreferences.getString("emailUser", "")

        when (item.itemId) {
            R.id.btnUser -> {
                if (!validate.isNullOrBlank()) {
                    val vtnProfile = Intent(this, Profile::class.java)
                    startActivity(vtnProfile)
                } else {
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public fun logout(v: View)
    {
        editor.remove("emailUser")
        editor.remove("passwordUser")
        editor.remove("nameUser")
        editor.remove("pSurNameUser")
        editor.remove("mSurNameUser")
        editor.remove("typeUser")
        editor.apply()
        val vtnMain = Intent(this, MainActivity::class.java)
        startActivity(vtnMain)
    }

}