package com.example.elearning

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import java.io.File

class RegisterTeacher : AppCompatActivity()
{

    private lateinit var toolbar: Toolbar
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var RETURN_VALUE = 1
    private var RETURN_VALUE_BUTTON = 0
    private val VALUE_TEXTVIEW = "TextView"
    private lateinit var progressBar: ProgressBar

    //containers
    private lateinit var scrViewRegisterTeacher: ScrollView
    private lateinit var scrViewSuccess: ScrollView
    private lateinit var scrViewWaitingResponse: ScrollView

    //resources
    private lateinit var btnCurp: Button
    private lateinit var btnCurrency: Button
    private lateinit var curpPDF: TextView
    private lateinit var currencyPDF: TextView
    private lateinit var lada: TextView
    private lateinit var phoneNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_teacher)

        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        progressBar = findViewById(R.id.progressBar)

        //containers
        scrViewRegisterTeacher = findViewById(R.id.scrollCreationRegister)
        scrViewSuccess = findViewById(R.id.scrollViewSuccess)
        scrViewWaitingResponse = findViewById(R.id.scrollViewWaiting)

        //resources srcViewRegisterTeacher
        btnCurp = findViewById(R.id.btnAddCurp)
        btnCurrency = findViewById(R.id.btnAddCurrency)
        curpPDF = findViewById(R.id.lbCurpNameDocument)
        currencyPDF = findViewById(R.id.lbCurrencyNameDocument)
        lada = findViewById(R.id.txtLada)
        phoneNumber = findViewById(R.id.txtPhoneNumber)
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


    fun addCurp(v: View)
    {
        RETURN_VALUE_BUTTON = 1

        val vtnChooseDocument: Intent = Intent(Intent.ACTION_GET_CONTENT)
        vtnChooseDocument.type = "application/pdf"
        startActivityForResult(Intent.createChooser(vtnChooseDocument, "Selecciona tu archivo: "), RETURN_VALUE)
    }

    fun addCurrency(v: View)
    {
        RETURN_VALUE_BUTTON = 2

        val vtnChooseDocument: Intent = Intent(Intent.ACTION_GET_CONTENT)
        vtnChooseDocument.type = "application/pdf"
        startActivityForResult(Intent.createChooser(vtnChooseDocument, "Selecciona tu archivo: "), RETURN_VALUE)
    }

    override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_CANCELED)
        {

        }
        if((resultCode == RESULT_OK) && (requestCode == RETURN_VALUE))
        {
            val path = data?.data

            if(path != null)
            {
                val file = File(path.path)
                val fileName = file.name

                when(RETURN_VALUE_BUTTON)
                {
                    1 ->{
                        curpPDF.visibility = View.VISIBLE
                        curpPDF.text = fileName.toString()
                    }

                    2 ->{
                        currencyPDF.visibility = View.VISIBLE
                        currencyPDF.text = fileName.toString()
                    }
                }
            }
        }
    }

    fun teacherRegister(v: View)
    {
        val ladaRegister = lada.text.toString()
        val phoneNumber = phoneNumber.text.toString()
        val curpDocumentRegister = curpPDF.text.toString()
        val currencyDocumentRegister = currencyPDF.text.toString()

        if(ladaRegister.isNullOrBlank() || phoneNumber.isNullOrBlank())
        {
            Toast.makeText(this, "Numero inválido", Toast.LENGTH_SHORT).show()
        }
        else if(curpDocumentRegister == VALUE_TEXTVIEW || currencyDocumentRegister == VALUE_TEXTVIEW)
        {
            Toast.makeText(this, "Agrega los documentos correspondientes", Toast.LENGTH_SHORT).show()
        }
        else
        {
            scrViewRegisterTeacher.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }
    }
}