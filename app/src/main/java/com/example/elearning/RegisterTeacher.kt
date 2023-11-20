package com.example.elearning

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.icu.text.MessagePattern.ApostropheMode
import android.net.Uri
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date

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
    private lateinit var curpFile: Uri
    private lateinit var currencyFile: Uri
    private lateinit var arrayPaths: MutableList<String>

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
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_teacher)

        arrayPaths = mutableListOf("")

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

        val validateRegister = sharedPreferences.getString("idInformationUser", "")
        if(!validateRegister.isNullOrBlank())
        {
            scrViewRegisterTeacher.visibility = View.GONE
            scrViewWaitingResponse.visibility = View.VISIBLE
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


    fun addCurp(v: View)
    {
        RETURN_VALUE_BUTTON = 1

        val vtnChooseDocument = Intent(Intent.ACTION_GET_CONTENT)
        vtnChooseDocument.type = "application/pdf"
        startActivityForResult(Intent.createChooser(vtnChooseDocument, "Selecciona tu archivo: "), RETURN_VALUE)
    }

    fun addCurrency(v: View)
    {
        RETURN_VALUE_BUTTON = 2

        val vtnChooseDocument = Intent(Intent.ACTION_GET_CONTENT)
        vtnChooseDocument.type = "application/pdf"
        startActivityForResult(Intent.createChooser(vtnChooseDocument, "Selecciona tu archivo: "), RETURN_VALUE)
    }

    override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_CANCELED)
        {

        }
        if (requestCode == RETURN_VALUE && resultCode == RESULT_OK && data != null && data.data != null)
        {
            val path: Uri = data.data!!
            val file = File(path.toString())
            val fileName = file.name

            when(RETURN_VALUE_BUTTON)
            {
                1 ->{
                    //Toast.makeText(this, " "+path, Toast.LENGTH_LONG).show()
                    curpPDF.visibility = View.VISIBLE
                    curpPDF.text = fileName.toString()
                    curpFile = path
                }

                2 ->{
                    currencyPDF.visibility = View.VISIBLE
                    currencyPDF.text = fileName.toString()
                    currencyFile = path
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            val nameTeacher: String? = sharedPreferences.getString("nameUser", "")
            val emailTeacher: String? = sharedPreferences.getString("emailUser", "")

            scrViewRegisterTeacher.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            uploadDocument(curpFile, curpPDF.text.toString(), nameTeacher, emailTeacher)
            uploadDocument(currencyFile, currencyPDF.text.toString(), nameTeacher, emailTeacher)

            val uploadCurp = "teachers/$emailTeacher/documents/"
            val uploadCurrency = "teachers/$emailTeacher/documents/"

            try
            {
                val newRowId = db.insert("INFORMATION", null, ContentValues().apply {
                    put("RUTH_CURP", uploadCurp)
                    put("RUTH_CURRENCY", uploadCurrency)
                    put("LADA", ladaRegister)
                    put("PHONE_NUMBER", phoneNumber)
                })

                val queryUserUploadInformation = "UPDATE USER SET INFORMATION_USER = ? WHERE EMAIL = ? AND NUSER = ?"
                val valuesUpload = arrayOf(newRowId, emailTeacher, nameTeacher)

                db.execSQL(queryUserUploadInformation, valuesUpload)

                editor.putString("idInformationUser", newRowId.toString())
                editor.apply()

                progressBar.visibility = View.GONE
                scrViewSuccess.visibility = View.VISIBLE
            }
            catch (e: SQLException)
            {
                Toast.makeText(this, "Algo salió mal (db)", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                scrViewRegisterTeacher.visibility = View.VISIBLE
            }
        }
    }

    public fun uploadDocument(document: Uri, nameFile: String?, teacherName: String?, email: String?)
    {
        var path: String? = null
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.reference
        val reference = "teachers/$email/documents/" + System.currentTimeMillis().toString() + nameFile + ".pdf"
        val fileReference = storageReference.child(reference)

        fileReference.putFile(document)
            .addOnSuccessListener {

            }
            .addOnFailureListener{exception->
                Toast.makeText(this, "Error al subir el archivo: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}