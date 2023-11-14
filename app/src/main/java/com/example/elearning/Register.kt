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
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import java.lang.Exception
import java.sql.SQLException

class Register : AppCompatActivity()
{
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase
    private lateinit var toolbar: Toolbar
    private lateinit var name: EditText
    private lateinit var pSurName: EditText
    private lateinit var mSurName: EditText
    private lateinit var email: EditText
    private lateinit var mainPassword: EditText
    private lateinit var repeatPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        //Connection
        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        //Identification of the TextBox
        name = findViewById(R.id.bxNameRegister)
        pSurName = findViewById(R.id.bxPSNameRegister)
        mSurName = findViewById(R.id.bxMSNameRegister)
        email = findViewById(R.id.bxEmailRegister)
        mainPassword = findViewById(R.id.bxMainPasswordRegister)
        repeatPassword = findViewById(R.id.bxRepeatPasswordRegister)
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

        when(item.itemId)
        {
            R.id.btnUser ->{
                if(!validate.isNullOrBlank())
                {
                    val vtnProfile = Intent(this, Profile::class.java)
                    startActivity(vtnProfile)
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

    public fun register(v: View)
    {
        var res: functions = functions()

        val typeUser: Int = 1
        var nameRegister = name.text.toString()
        var pSurNameRegister = pSurName.text.toString()
        var mSurNameRegister = mSurName.text.toString()
        var emailRegister = email.text.toString()
        var mainPasswordRegister = mainPassword.text.toString()
        var repeatPasswordRegister = repeatPassword.text.toString()
        var validatePasswords = res.validatePassword(mainPasswordRegister, repeatPasswordRegister)

        if(res.emptyOrNot(nameRegister) && res.emptyOrNot(pSurNameRegister)
            && res.emptyOrNot(mSurNameRegister) && res.emptyOrNot(emailRegister)
            && res.emptyOrNot(mainPasswordRegister) && res.emptyOrNot(repeatPasswordRegister))
        {
            Toast.makeText(this, "Alguno de los campos está vacío", Toast.LENGTH_LONG).show()
        }
        else
        {
            if(!res.validateEmail(emailRegister)){Toast.makeText(this, "Correo Inválido", Toast.LENGTH_LONG).show()}
            else
            {
                if(validatePasswords == "Contraseñas no coinciden" || validatePasswords == "La contraseña debe tener al menos 8 caracteres"){Toast.makeText(this, validatePasswords, Toast.LENGTH_LONG).show()}
                else
                {
                    try
                    {
                        var query_register = "INSERT INTO USER (NUSER, PSURNAME, MSURNAME, EMAIL, PASSWORD, TYPE_USER_USER) VALUES (?, ?, ?, ?, ?, ?)"
                        var values = arrayOf(nameRegister, pSurNameRegister, mSurNameRegister, emailRegister, mainPasswordRegister, typeUser)

                        db.execSQL(query_register, values)

                        val vtnMain = Intent(this, Login::class.java)
                        startActivity(vtnMain)
                    }
                    catch (e: SQLException)
                    {
                        Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}