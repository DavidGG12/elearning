package com.example.elearning

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AddCourse : AppCompatActivity()
{
    //Variables
    private lateinit var ID_CATEGORY_SELECTED: String
    private lateinit var ID_SUBCATEGORY_SELECTED: String
    private lateinit var ID_TITLE_COURSE: String
    private lateinit var TITLE_COURSE: String
    private lateinit var ID_SECTION_SELECTED: String
    private lateinit var VIDEO_PATH: Uri
    private var RETURN_VALUE = 1

    //Firebase
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference

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
    //Resourcer of view "Section"
    private var clicksSection = 0
    private lateinit var txtSection: EditText

    //View to register the content of the course
    private lateinit var scrViewContent: ScrollView
    //Resources of view "Content"
    private lateinit var pgrsBar: ProgressBar
    private lateinit var spnrSection: Spinner
    private lateinit var txtNameContent: EditText
    private lateinit var lbVideoName: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        //Init the firebase
        FirebaseApp.initializeApp(this)

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
        fillSpnr(null, false)
        txtTitleCourse = findViewById(R.id.txtTitleCourse)
        txtCourseDescription = findViewById(R.id.txtCourseDescription)
        txtPrice = findViewById(R.id.txtPrice)

        //Sections
        scrViewSections = findViewById(R.id.scrSectionView)
        //Resources of view "Sections"
        txtSection = findViewById(R.id.txtSection)

        //Content
        scrViewContent = findViewById(R.id.scrContentView)
        //Resources of view "Content"
        pgrsBar = findViewById(R.id.prgBar)
        spnrSection = findViewById(R.id.spnrSection)
        txtNameContent = findViewById(R.id.txtNameContent)
        lbVideoName = findViewById(R.id.lbVideoUp)
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
                fillSpnr(null, true)
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
    private fun fillSpnr(idCategory: String?, sectionFlag: Boolean)
    {
        var fillList = mutableListOf<String>()
        var queryFill: String
        var cursorFill: Cursor

        if(idCategory == null && sectionFlag == false)
        {
            queryFill = "SELECT NCATEGORY FROM CATEGORY"
            cursorFill = db.rawQuery(queryFill, null)
        }
        else if(idCategory != null && sectionFlag == false)
        {
            queryFill = "SELECT NSUBCATEGORY FROM SUBCATEGORY WHERE CATEGORY_SUBCATEGORY = ?"
            cursorFill = db.rawQuery(queryFill, arrayOf(idCategory))
        }
        else
        {
            queryFill = "SELECT NSECTION FROM SECTION WHERE COURSE_SECTION = ?"
            cursorFill = db.rawQuery(queryFill, arrayOf(ID_TITLE_COURSE))
        }

        if(cursorFill.moveToFirst())
        {
            do
            {
                val nCategory: String

                if(idCategory.isNullOrEmpty() && sectionFlag == false) { nCategory = cursorFill.getString(cursorFill.getColumnIndex("NCATEGORY")) }
                else if(!idCategory.isNullOrEmpty() && sectionFlag == false) { nCategory = cursorFill.getString(cursorFill.getColumnIndex("NSUBCATEGORY")) }
                else { nCategory = cursorFill.getString(cursorFill.getColumnIndex("NSECTION")) }

                fillList.add(nCategory)
            }while(cursorFill.moveToNext())
        }
        cursorFill.close()

        val adapterSpnr = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, fillList)

        if(idCategory == null && sectionFlag == false) { spnrCategory.adapter = adapterSpnr }
        else if(idCategory != null && sectionFlag == false) { spnrSubCategory.adapter = adapterSpnr }
        else{ spnrSection.adapter = adapterSpnr }

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
                    fillSpnr(ID_CATEGORY_SELECT, false)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?)
            {
                TODO("Not yet implemented")
            }
        }

        if(idCategory != null && sectionFlag == false)
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
        else if(idCategory == null && sectionFlag == true)
        {
            spnrSection.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
            {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long)
                {
                    var selectedItemActions = parentView?.getItemAtPosition(position).toString()

                    val querySection = "SELECT ID_SECTION FROM SECTION WHERE NSECTION = ?"
                    val valuesSection = arrayOf(selectedItemActions)
                    val cursorSectionID = db.rawQuery(querySection, valuesSection)
                    if(cursorSectionID.moveToFirst())
                    {
                        ID_SECTION_SELECTED = cursorSectionID.getString(cursorSectionID.getColumnIndex("ID_SECTION"))
                    }
                    cursorSectionID.close()
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
                    put("NCOURSE", titleCourse.toString().toUpperCase())
                    put("DESCRIPTION", descriptionCourse.toString().toUpperCase())
                    put("PRICE", priceCourse.toString())
                    put("TEACHER_COURSE", idTeacher)
                    put("SUBCATEGORY_COURSE", ID_SUBCATEGORY_SELECTED)
                })
                ID_TITLE_COURSE = newRowId.toString()
                TITLE_COURSE = titleCourse.toString()
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

    fun registerSection(v: View)
    {
        clicksSection++
        val sectionRegister = "SECCIÓN $clicksSection: ${txtSection.text.toString().toUpperCase()}"
        if(!sectionRegister.isNullOrEmpty())
        {
            try
            {
                val querySection = "INSERT INTO SECTION (NSECTION, COURSE_SECTION) VALUES (?, ?)"
                val valuesSection = arrayOf(sectionRegister, ID_TITLE_COURSE)
                db.execSQL(querySection, valuesSection)
                Toast.makeText(this, "Registrado", Toast.LENGTH_SHORT).show()
            }
            catch (e: SQLException)
            {
                Toast.makeText(this, "Algo salió mal en el registro", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(this, "El campo está vacío", Toast.LENGTH_SHORT).show()
        }
    }

    fun chooseVideo(v: View)
    {
        val vtnChooseVideo = Intent(Intent.ACTION_GET_CONTENT)
        vtnChooseVideo.type = "video/mp4"
        startActivityForResult(Intent.createChooser(vtnChooseVideo, "Selecciona el vídeo: "), RETURN_VALUE)
    }

    override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RETURN_VALUE && resultCode == RESULT_OK && data != null && data.data != null)
        {
            val path: Uri = data.data!!
            val file = File(path.toString())
            val fileName = file.name

            lbVideoName.visibility = View.VISIBLE
            lbVideoName.text = fileName.toString()
            VIDEO_PATH = path
        }

        if(resultCode == RESULT_CANCELED)
        {

        }
    }

    @SuppressLint("Range")
    fun registerContent(v: View)
    {
        val videoName = txtNameContent.text
        pgrsBar.visibility = View.VISIBLE

        if(!videoName.isNullOrEmpty())
        {
            try
            {
                val emailTeacher = sharedPreferences.getString("emailUser", "")
                val sectionSelected = spnrSection.selectedItem.toString()
                var sectionId = ""
                val queryIDSection = "SELECT ID_SECTION FROM SECTION WHERE NSECTION = ?"
                val cursor = db.rawQuery(queryIDSection, arrayOf(sectionSelected))
                if (cursor.moveToFirst())
                {
                    sectionId = cursor.getString(cursor.getColumnIndex("ID_SECTION"))
                }
                val reference = "teachers/$emailTeacher/courses/$TITLE_COURSE/$sectionSelected/${videoName}.mp4"
                val contentReference = storageReference.child(reference)

                contentReference.putFile(VIDEO_PATH)
                    .addOnSuccessListener {
                        val queryContent = "INSERT INTO CONTENT (NCONTENT," +
                                "RUTH_CONTENT," +
                                "SECTION_CONTENT" +
                                ") VALUES (?, ?, ?)"
                        val valuesContent = arrayOf(videoName, reference, sectionId)
                        db.execSQL(queryContent, valuesContent)

                        Toast.makeText(this, "Registrado", Toast.LENGTH_SHORT).show()
                        pgrsBar.visibility = View.GONE
                    }
                    .addOnFailureListener{
                        Toast.makeText(this, "No se pudo subir tu video", Toast.LENGTH_SHORT).show()
                        pgrsBar.visibility = View.GONE
                    }
            }
            catch (e: SQLException)
            {
                Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(this, "El campo del título está vacío", Toast.LENGTH_SHORT).show()
        }
    }
}

