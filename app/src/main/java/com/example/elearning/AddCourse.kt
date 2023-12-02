package com.example.elearning

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class AddCourse : AppCompatActivity()
{
    //Variables
    private lateinit var ID_CATEGORY_SELECTED: String
    private lateinit var ID_SUBCATEGORY_SELECTED: String

    //Connection
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase

    //SharedPreference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    //Toolbar
    private lateinit var toolbarCourse: Toolbar

    //Views
    //View of the data of the course
    private lateinit var scrViewData: ScrollView
    //Resources of view "Data Course"
    private lateinit var spnrCategory: Spinner
    private lateinit var spnrSubCategory: Spinner
    private lateinit var txtTitleCourse: EditText
    private lateinit var txtCourseDescription: com.google.android.material.textfield.TextInputEditText
    private lateinit var txtPrice: EditText

    //View to register the sections of the course
    private lateinit var scrViewSections: ScrollView

    //View to register the content of the course
    private lateinit var scrViewContent: ScrollView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        //Connection
        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        //SharedPreference
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        //Init Toolbar
        toolbarCourse = findViewById(R.id.toolbarCourse)
        setSupportActionBar(toolbarCourse)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        //Views
        //Data Course
        scrViewData = findViewById(R.id.scrDataView)
        //Resources of view "Data Course"
        spnrCategory = findViewById(R.id.spnrCategory)
        spnrSubCategory = findViewById(R.id.spnrSubCategory)
        fillSpnr(null)
        txtTitleCourse = findViewById(R.id.txtTitleCourse)
        txtCourseDescription = findViewById(R.id.txtCourseDescription)
        txtPrice = findViewById(R.id.txtPrice)

        //Sections
        scrViewSections = findViewById(R.id.scrSectionView)

        //Content
        scrViewContent = findViewById(R.id.scrContentView)
    }

    fun next(v: View)
    {
        when(v.id)
        {
            R.id.btnDataNext -> {
                scrViewData.visibility = View.GONE
                scrViewSections.visibility = View.VISIBLE
            }

            R.id.btnNextSection -> {
                scrViewSections.visibility = View.GONE
                scrViewContent.visibility = View.VISIBLE
            }

            R.id.btnFinish -> {
                Toast.makeText(this, "Curso registrado correctamente", Toast.LENGTH_SHORT).show()
                val vtnProfile = Intent(this, Profile::class.java)
                startActivity(vtnProfile)
            }
        }
    }

    fun comeBack(v: View)
    {
        when(v.id)
        {
            R.id.txtBackSection -> {
                scrViewData.visibility = View.VISIBLE
                scrViewSections.visibility = View.GONE
            }

            R.id.txtBackContent -> {
                scrViewSections.visibility = View.VISIBLE
                scrViewContent.visibility = View.GONE
            }
        }
    }

    @SuppressLint("Range")
    private fun fillSpnr(idCategory: String?)
    {
        var fillList = mutableListOf<String>()
        var queryFill: String
        var cursorFill: Cursor

        if(idCategory == null)
        {
            queryFill = "SELECT NCATEGORY FROM CATEGORY"
            cursorFill = db.rawQuery(queryFill, null)
        }
        else
        {
            queryFill = "SELECT NSUBCATEGORY FROM SUBCATEGORY WHERE CATEGORY_SUBCATEGORY = ?"
            cursorFill = db.rawQuery(queryFill, arrayOf(idCategory))
        }

        if(cursorFill.moveToFirst())
        {
            do
            {
                val nCategory: String

                if(idCategory.isNullOrEmpty()) { nCategory = cursorFill.getString(cursorFill.getColumnIndex("NCATEGORY")) }
                else { nCategory = cursorFill.getString(cursorFill.getColumnIndex("NSUBCATEGORY")) }

                fillList.add(nCategory)
            }while(cursorFill.moveToNext())
        }
        cursorFill.close()

        val adapterSpnr = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, fillList)

        if(idCategory == null) { spnrCategory.adapter = adapterSpnr }
        else { spnrSubCategory.adapter = adapterSpnr }

        spnrCategory.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long)
            {
                var selectedItemActions = parentView?.getItemAtPosition(position).toString()

                val query_SubcategoryFill = "SELECT ID_CATEGORY FROM CATEGORY WHERE NCATEGORY = ?"
                val values_SubcategoryFill = arrayOf(selectedItemActions)
                val cursor_ItemSelected: Cursor = db.rawQuery(query_SubcategoryFill, values_SubcategoryFill)

                if(cursor_ItemSelected.moveToFirst())
                {
                    var ID_CATEGORY_SELECT = cursor_ItemSelected.getString(cursor_ItemSelected.getColumnIndex("ID_CATEGORY"))
                    cursor_ItemSelected.close()
                    ID_CATEGORY_SELECTED = ID_CATEGORY_SELECT
                    fillSpnr(ID_CATEGORY_SELECT)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?)
            {
                TODO("Not yet implemented")
            }
        }

        if(idCategory != null)
        {
            spnrSubCategory.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
            {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long)
                {
                    var selectedItemActions = parentView?.getItemAtPosition(position).toString()

                    val querySubCategory = "SELECT ID_SUBCATEGORY FROM SUBCATEGORY WHERE NSUBCATEGORY = ?"
                    val valuesSubcategory = arrayOf(selectedItemActions)
                    val cursorSearchID = db.rawQuery(querySubCategory, valuesSubcategory)

                    if(cursorSearchID.moveToFirst())
                    {
                        ID_SUBCATEGORY_SELECTED = cursorSearchID.getString(cursorSearchID.getColumnIndex("ID_SUBCATEGORY"))
                    }
                    cursorSearchID.close()
                }

                override fun onNothingSelected(p0: AdapterView<*>?)
                {
                    TODO("Not yet implemented")
                }

            }
        }
    }

    fun registerDataCourse(v: View)
    {
        //Init all the values of the register
        val titleCourse = txtTitleCourse.text
        val descriptionCourse: String?
        if(txtCourseDescription.text.isNullOrEmpty()){ descriptionCourse = null }
        else{ descriptionCourse = txtCourseDescription.text.toString() }
        val priceCourse = txtPrice.text
        val idTeacher = sharedPreferences.getString("idUser", "")

        //Validate if the title and the price aren´t null
        if(!titleCourse.isNullOrEmpty() || !priceCourse.isNullOrEmpty())
        {
            try
            {
                val newRowId = db.insert("COURSE", null, ContentValues().apply {
                    put("NCOURSE", titleCourse)
                    put("DESCRIPTION", descriptionCourse)
                    put("PRICE", priceCourse)
                    put("TEACHER_COURSE", idTeacher)
                    put("SUBCATEGORY_COURSE", ID_SUBCATEGORY_SELECTED)
                })
                Toast.makeText(this, "Registrado", Toast.LENGTH_SHORT).show()

            }
            catch (e: SQLException)
            {
                Toast.makeText(this, "Algo salió mal al registrarlo", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(this, "No se ha agregado nada en el título o en precio", Toast.LENGTH_LONG).show()
        }
    }
}