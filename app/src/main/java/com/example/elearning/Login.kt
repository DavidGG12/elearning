package com.example.elearning

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import java.io.StringBufferInputStream

class Login : AppCompatActivity()
{
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var toolbar: Toolbar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        //Save the session
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        email = findViewById(R.id.bxIniciarSesionCorreo)
        password = findViewById(R.id.bxIniciarSesionContrasena)

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

    @SuppressLint("Range")
    public fun login(v: View)
    {
        var res: functions = functions()

        db.isOpen()

        var email_login = email.text.toString()
        var password_login = password.text.toString()

        if(!res.emptyOrNot(email_login) && !res.emptyOrNot(password_login))
        {
            var query_login = "SELECT ID_USER, NUSER, PSURNAME, MSURNAME, DESCRIPTION, TYPE_USER_USER, INFORMATION_USER FROM USER WHERE EMAIL = ? AND PASSWORD = ?"
            var clauses = arrayOf(email_login, password_login)
            var cursor: Cursor = db.rawQuery(query_login, clauses)

            if(cursor.moveToFirst())
            {
                //Variables to save the data of the user that we can use it in others functions
                val idUser: String = cursor.getString(cursor.getColumnIndex("ID_USER"))
                val nameUser: String = cursor.getString(cursor.getColumnIndex("NUSER"))
                val pSurNameUser: String = cursor.getString(cursor.getColumnIndex("PSURNAME"))
                val mSurNameUser: String = cursor.getString(cursor.getColumnIndex("MSURNAME"))
                val descriptionUser: String? = cursor.getString(cursor.getColumnIndex("DESCRIPTION"))
                val typeUser : String = cursor.getString(cursor.getColumnIndex("TYPE_USER_USER"))
                val idInformation: String? = cursor.getString(cursor.getColumnIndex("INFORMATION_USER"))

                editor.putString("idUser", idUser)
                editor.putString("emailUser", email_login)
                editor.putString("passwordUser", password_login)
                editor.putString("nameUser", nameUser)
                editor.putString("pSurNameUser", pSurNameUser)
                editor.putString("mSurNameUser", mSurNameUser)
                editor.putString("descriptionUser", descriptionUser)
                editor.putString("typeUser", typeUser)
                editor.putString("idInformationUser", idInformation)
                editor.apply()

                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                //db.close()

                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            else
            {
                Toast.makeText(this, "No se encontraron registros", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(this, "Alguno de los campos está vacío", Toast.LENGTH_LONG).show()
        }
    }

    public fun vtnRegister(v: View)
    {
        val intent_register = Intent(this, Register::class.java)
        startActivity(intent_register)
    }
}