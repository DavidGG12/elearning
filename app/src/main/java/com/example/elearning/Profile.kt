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
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import org.w3c.dom.Text
import java.sql.SQLException

class Profile : AppCompatActivity()
{
    private lateinit var toolbar: Toolbar
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var txtWelcome: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtDescription: TextView
    private lateinit var lbTeacherDescription: TextView
    private lateinit var lbAddTeacher: TextView
    private lateinit var txtBxDescription: EditText
    private lateinit var spnrTeacherCourses: Spinner
    private lateinit var btnAddTeacher: Button

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

        txtDescription = findViewById(R.id.txtDescription)
        txtBxDescription = findViewById(R.id.txtBxDescription)

        val valueDescription = sharedPreferences.getString("descriptionUser", "")
        val validateTeacher = sharedPreferences.getString("typeUser", "")

        if(!valueDescription.isNullOrBlank())
        {
            txtDescription.setText(valueDescription)
        }
        else
        {
            txtDescription.setText("Sin Descripción")
        }

        if(validateTeacher == "3")
        {
            lbTeacherDescription.visibility = View.GONE
            lbAddTeacher.visibility = View.GONE
            btnAddTeacher.visibility = View.VISIBLE
            spnrTeacherCourses.visibility = View.VISIBLE
        }

        toolbar.setOnClickListener{
            val vtnMain = Intent(this, MainActivity::class.java)
            startActivity(vtnMain)
        }
    }

    override public fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)
        return true
        //return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        val validate = sharedPreferences.getString("emailUser", "")
        val validateType = sharedPreferences.getString("typeUser", "")

        when(item.itemId)
        {
            R.id.btnUser ->{
                if(!validate.isNullOrBlank())
                {
                    when(validateType)
                    {
                        "1" -> {
                            val vtnAdmin = Intent(this, Admin::class.java)
                            startActivity(vtnAdmin)
                        }

                        else -> {
                            val vtnProfile = Intent(this, Profile::class.java)
                            startActivity(vtnProfile)
                        }
                    }
                }
                else
                {
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    public fun addDescription(v: View)
    {
        if(txtDescription.visibility == View.VISIBLE)
        {
            txtDescription.visibility = View.GONE
            txtBxDescription.visibility = View.VISIBLE
        }
        else
        {
            var descriptionRegister: String? = txtBxDescription.text.toString()

            when(descriptionRegister.isNullOrBlank())
            {
                false -> {
                    var email = sharedPreferences.getString("emailUser", "")
                    var password = sharedPreferences.getString("passwordUser", "")

                    try
                    {
                        var queryDescriptionRegister = "UPDATE USER SET DESCRIPTION = ? WHERE EMAIL = ? AND PASSWORD = ?"
                        var clausesDescriptionRegister = arrayOf(descriptionRegister, email, password)

                        db.execSQL(queryDescriptionRegister, clausesDescriptionRegister)

                        editor.putString("descriptionUser", descriptionRegister)
                        editor.apply()
                        Toast.makeText(this, "Hecho", Toast.LENGTH_SHORT).show()

                        txtDescription.setText(descriptionRegister)
                        txtDescription.visibility = View.VISIBLE
                        txtBxDescription.visibility = View.GONE
                    }
                    catch (e: SQLException)
                    {
                        Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    txtDescription.visibility = View.VISIBLE
                    txtBxDescription.visibility = View.GONE
                }
            }
        }
    }

    public fun addPaymentMethod(v: View)
    {
        var intent = Intent(this, Browse::class.java)
        startActivity(intent)
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
        Toast.makeText(this, "¡Adiós!", Toast.LENGTH_SHORT).show()
        val vtnMain = Intent(this, MainActivity::class.java)
        startActivity(vtnMain)
    }

    public fun vtnTeacherRegister(v: View)
    {
        val vtnTeacherRegister = Intent(this, RegisterTeacher::class.java)
        startActivity(vtnTeacherRegister)
    }

}