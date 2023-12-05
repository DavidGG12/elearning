package com.example.elearning

import android.annotation.SuppressLint
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
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage

class CourseView : AppCompatActivity()
{
    //Shared Preference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    //Firebase
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference

    //Connection
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase

    //Toolbar
    private lateinit var toolbar: Toolbar

    //Variables to save the data of the course
    private var titleData: String = ""
    private var descriptionData: String? = ""
    private var priceData: String = "$ "
    private var idCourse: String = ""

    //Resources of the activity
    private lateinit var titleCourse: TextView
    private lateinit var descriptionCourse: TextView
    private lateinit var priceCourse: TextView
    private lateinit var btnBuy: ImageButton
    private lateinit var btnDelete: Button
    private lateinit var tableSections: ListView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_view)

        //Recover the title of the course we want to see
        val title = intent.getStringExtra("title")
        val teacher = intent.getStringExtra("teacherID")

        //Shared Preference
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        //Init the firebase
        FirebaseApp.initializeApp(this)

        //Connection
        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        //Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Resources
        titleCourse = findViewById(R.id.lbTitleCourse)
        descriptionCourse = findViewById(R.id.lbDescriptionCourse)
        priceCourse = findViewById(R.id.lbPriceCourse)
        btnBuy = findViewById(R.id.btnBuy)
        btnDelete = findViewById(R.id.btnDelete)
        tableSections = findViewById(R.id.listSections)

        //Function to fill the data of the course
        fillData(title.toString(), teacher.toString())
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
    private fun fillData(title: String, teacher: String)
    {
        val idUser = sharedPreferences.getString("idUser", "")
        val queryFillData = "SELECT * FROM COURSE WHERE NCOURSE = ? AND TEACHER_COURSE = ?"
        val valuesFillData = arrayOf(title, teacher)
        val cursor = db.rawQuery(queryFillData, valuesFillData)

        if(cursor.moveToFirst())
        {
            idCourse = cursor.getString(cursor.getColumnIndex("ID_COURSE"))
            titleData = cursor.getString(cursor.getColumnIndex("NCOURSE"))
            descriptionData = cursor.getString(cursor.getColumnIndex("DESCRIPTION"))
            priceData = priceData + cursor.getString(cursor.getColumnIndex("PRICE"))
        }
        cursor.close()

        if(teacher == idUser)
        { btnDelete.visibility = View.VISIBLE }
        else
        { btnBuy.visibility = View.VISIBLE }

        titleCourse.text = titleData
        descriptionCourse.text = descriptionData
        priceCourse.text = priceData

        fillList(idCourse)
    }

    @SuppressLint("Range")
    private fun fillList(idCourse: String)
    {
        var sections = mutableListOf<Documents_Teachers>() //I use the data of Documents_Teachers because is the closest of what we need
        val queryFillList = "SELECT NSECTION FROM SECTION WHERE COURSE_SECTION = ?"
        val valuesFillList = arrayOf(idCourse)
        val cursor = db.rawQuery(queryFillList, valuesFillList)

        if(cursor.moveToFirst())
        {
            do
            {
                val nSection = cursor.getString(cursor.getColumnIndex("NSECTION"))

                val sectionData = Documents_Teachers(nSection)
                sections.add(sectionData)
            }while(cursor.moveToNext())
        }
        cursor.close()

        val adapter = DocumentsAdapter(this, R.layout.documents_table, sections)
        tableSections.adapter = adapter
    }

    fun deleteCourse(v: View)
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar")
        builder.setMessage("¿Deseas eliminar este curso?")
        builder.setPositiveButton("Aceptar"){dialog, _ ->
            deleteContent()
        }
        builder.setNegativeButton("Cancelar"){dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("Range")
    private fun deleteContent()
    {
        /*
        val query = "DELETE FROM SECTION WHERE ID_COURSE = ?"
        val values = arrayOf(idCourse)
        db.execSQL(query, values)
        val vtnProfile = Intent(this, Profile::class.java)
        startActivity(vtnProfile)
        */

        var listSections = mutableListOf<String>()
        var listRuth = mutableListOf<String>()

        var queryRuths = "SELECT ID_SECTION FROM SECTION WHERE COURSE_SECTION = ?"
        var valuesRuths = arrayOf(idCourse)
        var cursorRuth = db.rawQuery(queryRuths, valuesRuths)

        if(cursorRuth.moveToFirst())
        {
            do
            {
                val idSection = cursorRuth.getString(cursorRuth.getColumnIndex("ID_SECTION"))
                listSections.add(idSection)
            }while (cursorRuth.moveToNext())
        }

        for(i in listSections)
        {
            queryRuths = "SELECT RUTH_CONTENT FROM CONTENT WHERE SECTION_CONTENT = ?"
            valuesRuths = arrayOf(i)

            cursorRuth = db.rawQuery(queryRuths, valuesRuths)

            if (cursorRuth.moveToFirst())
            {
                do
                {
                    val ruths = cursorRuth.getString(cursorRuth.getColumnIndex("RUTH_CONTENT"))
                    listRuth.add(ruths)
                }while (cursorRuth.moveToNext())
            }
        }

        for(j in listRuth)
        {
            queryRuths = "DELETE FROM CONTENT WHERE RUTH_CONTENT = ?"
            valuesRuths = arrayOf(j)
            db.execSQL(queryRuths, valuesRuths)

            val deleteContent = storageReference.child(j)

            deleteContent.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "No se pudo borrar el contenido", Toast.LENGTH_SHORT).show()
                }
        }
        deleteAllCourse()
    }

    private fun deleteAllCourse()
    {
        var queryDelete = "DELETE FROM SECTION WHERE COURSE_SECTION = ?"
        var valuesDelete = arrayOf(idCourse)
        db.execSQL(queryDelete, valuesDelete)

        queryDelete = "DELETE FROM COURSE WHERE ID_COURSE = ?"
        valuesDelete = arrayOf(idCourse)
        db.execSQL(queryDelete, valuesDelete)

        Toast.makeText(this, "Curso eliminado", Toast.LENGTH_SHORT).show()

        val vtnProfile = Intent(this, Profile::class.java)
        startActivity(vtnProfile)
    }

}