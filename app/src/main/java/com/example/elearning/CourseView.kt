package com.example.elearning

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
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
    private lateinit var spnrSections: Spinner
    private lateinit var tableSections: ListView
    private lateinit var tableContent: ListView

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
        spnrSections = findViewById(R.id.spnrSection)
        tableSections = findViewById(R.id.listSections)
        tableContent = findViewById(R.id.listContent)

        //Function to fill the data of the course
        fillData(title.toString(), teacher.toString())
    }

    override public fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)

        val searchItem = menu?.findItem(R.id.app_bar_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                var courseSearch = query.toString()
                goBrowse(courseSearch, true)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return true
            }
        })

        return true
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

            R.id.btnShoppingCar ->{
                val vtnCar = Intent(this, ShoppingCar::class.java)
                startActivity(vtnCar)
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
        {
            btnDelete.visibility = View.VISIBLE
            btnBuy.visibility = View.GONE
        }
        else
        {
            btnDelete.visibility = View.GONE
            btnBuy.visibility = View.VISIBLE

            val queryCar = "SELECT COURSE_CAR, STATUS FROM SHOPPING_CAR WHERE USER_CAR = ?"
            val values = arrayOf(sharedPreferences.getString("idUser", ""))
            val cursor = db.rawQuery(queryCar, values)
            if(cursor.moveToFirst())
            {
                val idValidate = cursor.getString(cursor.getColumnIndex("COURSE_CAR"))
                val boughtCourse = cursor.getString(cursor.getColumnIndex("STATUS"))

                if(idValidate == idCourse && boughtCourse == "PENDING")
                {
                    btnBuy.isEnabled = false
                }
                val queryPurchased = "SELECT * FROM SHOPPING_CAR WHERE USER_CAR = ? AND COURSE_CAR = ?"
                val valuesPurchased = arrayOf(sharedPreferences.getString("idUser", ""), idCourse)
                val cursorPurchased = db.rawQuery(queryPurchased, valuesPurchased)
                if(cursorPurchased.moveToFirst())
                {
                    val boughtCourse = cursor.getString(cursor.getColumnIndex("STATUS"))
                    if(boughtCourse == "PURCHASED")
                    {
                        tableSections.visibility = View.GONE
                        btnBuy.visibility = View.GONE
                        priceCourse.visibility = View.GONE
                        spnrSections.visibility = View.VISIBLE
                        tableContent.visibility = View.VISIBLE
                        fillSpnrSections(idCourse)
                    }
                }
            }
        }
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

    @SuppressLint("Range")
    private fun fillSpnrSections(idCourse: String)
    {
        var section = mutableListOf<String>()
        val queryFill = "SELECT NSECTION FROM SECTION WHERE COURSE_SECTION = ?"
        val valueFill = arrayOf(idCourse)
        val cursor = db.rawQuery(queryFill, valueFill)

        if(cursor.moveToFirst())
        {
            do
            {
                val nSection = cursor.getString(cursor.getColumnIndex("NSECTION"))
                section.add(nSection)
            }while (cursor.moveToNext())
        }

        cursor.close()

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                section
            )
        spnrSections.adapter = adapter
        spnrSections.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long)
            {
                var selectedItemActions = parentView?.getItemAtPosition(position).toString()
                val querySection = "SELECT ID_SECTION FROM SECTION WHERE NSECTION = ?"
                val valuesSection = arrayOf(selectedItemActions)
                val cursor = db.rawQuery(querySection, valuesSection)

                if(cursor.moveToFirst())
                {
                    val idSection = cursor.getString(cursor.getColumnIndex("ID_SECTION"))
                    fillTableContent(idSection)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?)
            {
                //goBrowse(null)
            }
        }
    }

    @SuppressLint("Range")
    private fun fillTableContent(idSection: String)
    {
        var listTableContent = mutableListOf<Documents_Teachers>()
        val queryTable = "SELECT NCONTENT FROM CONTENT WHERE SECTION_CONTENT  = ?"
        val valueTable = arrayOf(idSection)
        val cursorTable = db.rawQuery(queryTable, valueTable)

        if(cursorTable.moveToFirst())
        {
            do
            {
                val nContent = cursorTable.getString(cursorTable.getColumnIndex("NCONTENT"))
                val content = Documents_Teachers(nContent)
                listTableContent.add(content)
            }while (cursorTable.moveToNext())
            cursorTable.close()
        }
        val adapterTable = DocumentsAdapter(this, R.layout.documents_table, listTableContent)
        tableContent.adapter = adapterTable

        tableContent
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

    fun addShop(v: View)
    {
        val idUser = sharedPreferences.getString("idUser", "")
        val emailUser = sharedPreferences.getString("emailUser", "")
        val passwordUser = sharedPreferences.getString("passwordUser", "")

        if(idUser.isNullOrBlank() || emailUser.isNullOrBlank() || passwordUser.isNullOrBlank())
        {
            val vtnLogin = Intent(this, Login::class.java)
            startActivity(vtnLogin)
        }
        else
        {
            db.insert("SHOPPING_CAR", null, ContentValues().apply {
                put("USER_CAR", idUser)
                put("COURSE_CAR", idCourse)
                put("STATUS", "PENDING")
            })

            Toast.makeText(this, "Agregado a tu carrito", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goBrowse(browse: String?, spnrOrSearch: Boolean)
    {
        if(browse != null && spnrOrSearch)
        {
            val vtnBrowse = Intent(this, Browse::class.java)
            vtnBrowse.putExtra("courseSearch", browse)
            startActivity(vtnBrowse)
        }
    }
}