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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar

class Browse : AppCompatActivity()
{
    //Var
    private var controlSpinner = 0
    private lateinit var subcategorySelected: String
    private lateinit var ID_SUBCATEGORY: String
    private lateinit var courseSearch: String

    //Shared Preference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    //Connection
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase

    //Resources
    private lateinit var spnrSubcategories: Spinner
    private lateinit var tableCourse: ListView
    private lateinit var noFounded: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)

        //Init the extra resources
        subcategorySelected = intent.getStringExtra("subcategorySelected").toString()
        courseSearch = intent.getStringExtra("courseSearch").toString()

        //Shared Preferences
        sharedPreferences = getSharedPreferences("Session", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        //Connection
        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        //Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Resoources
        noFounded = findViewById(R.id.lbCoursesNoFounded)
        spnrSubcategories = findViewById(R.id.spnrSubCategories)
        fillSpnr()
        tableCourse = findViewById(R.id.tableCourses)
        fillTable(subcategorySelected, courseSearch)
    }

    override public fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)

        val searchItem = menu?.findItem(R.id.app_bar_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                courseSearch = query.toString()
                fillTable(subcategorySelected, courseSearch)
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
    private fun fillSpnr()
    {
        var categoriesList = mutableListOf<String>()
        val queryFill = "SELECT NSUBCATEGORY FROM SUBCATEGORY"
        val cursor: Cursor = db.rawQuery(queryFill, null)

        if (cursor.moveToFirst()) {
            do {
                val nCategory = cursor.getString(cursor.getColumnIndex("NSUBCATEGORY"))
                categoriesList.add(nCategory)
            } while (cursor.moveToNext())

            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    categoriesList
                )

            spnrSubcategories.adapter = adapter

            //This condition is to have the spinner on the selection the user sends when search the course

            if(subcategorySelected != null)
            {
                val index = categoriesList.indexOf(subcategorySelected)
                spnrSubcategories.setSelection(index)
            }

            spnrSubcategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    var selectedItemActions = parentView?.getItemAtPosition(position).toString()
                    if(controlSpinner > 0)
                    {
                        courseSearch = "null"
                        subcategorySelected = selectedItemActions
                        fillTable(subcategorySelected, courseSearch)
                    }
                    controlSpinner++
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //goBrowse(null)
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun fillTable(subcategory: String?, courseName: String?)
    {
        tableCourse.visibility = View.VISIBLE
        noFounded.visibility = View.GONE

        var courseList = mutableListOf<Courses>()

        if(subcategory != null)
        {
            val queryIdSubcategory = "SELECT ID_SUBCATEGORY FROM SUBCATEGORY WHERE NSUBCATEGORY = ?"
            val valuesIdSubcategory = arrayOf(subcategory)
            val cursorIdSubcategory = db.rawQuery(queryIdSubcategory, valuesIdSubcategory)

            if(cursorIdSubcategory.moveToFirst())
            {
                ID_SUBCATEGORY = cursorIdSubcategory.getString(cursorIdSubcategory.getColumnIndex("ID_SUBCATEGORY"))
            }
        }

        var querySearch = ""
        var valuesSearch = emptyArray<String>()
        if (courseName == "null" || courseName == null)
        {
            querySearch = "SELECT NCOURSE, DESCRIPTION, PRICE, TEACHER_COURSE FROM COURSE WHERE SUBCATEGORY_COURSE = ?"
            valuesSearch = arrayOf(ID_SUBCATEGORY)
        }
        else
        {
            querySearch = "SELECT NCOURSE, DESCRIPTION, PRICE, TEACHER_COURSE FROM COURSE WHERE NCOURSE LIKE UPPER(?)"
            valuesSearch = arrayOf("%$courseSearch%")
        }

        val cursorSearch = db.rawQuery(querySearch, valuesSearch)

        if(cursorSearch.moveToFirst())
        {
            do
            {
                val title = cursorSearch.getString(cursorSearch.getColumnIndex("NCOURSE"))
                val description = cursorSearch.getString(cursorSearch.getColumnIndex("DESCRIPTION"))
                val price = cursorSearch.getDouble(cursorSearch.getColumnIndex("PRICE"))
                val idTeacher = cursorSearch.getString(cursorSearch.getColumnIndex("TEACHER_COURSE"))

                val courses = Courses(title, description, price, idTeacher)
                courseList.add(courses)
            }while (cursorSearch.moveToNext())
        }
        else
        {
            tableCourse.visibility = View.GONE
            noFounded.visibility = View.VISIBLE
        }

        cursorSearch.close()

        val adapter = CoursesAdapter(this, R.layout.courses_table, true, courseList)
        tableCourse.adapter = adapter

        tableCourse.setOnItemClickListener { _, _, position, _ ->
            val clickedCourse = courseList[position]
            val titleCourse = clickedCourse.titleCourse.toString()
            val idTeacher = clickedCourse.teacherID.toString()

            val vtnCourse = Intent(this, CourseView::class.java)
            vtnCourse.putExtra("title", titleCourse)
            vtnCourse.putExtra("teacherID", idTeacher)
            startActivity(vtnCourse)
        }
    }
}