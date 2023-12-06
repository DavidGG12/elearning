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
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class MainActivity : AppCompatActivity()
{
    //Var
    private lateinit var courseSearch: String
    private var controlSpinner = 0

    //Shared Preference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    //Connection
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase

    //Resources
    private lateinit var spnrCategories: Spinner
    private lateinit var gallery: ImageCarousel
    private lateinit var tableCourses: ListView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Shared Preferences
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        //Connection
        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        //Resources
        spnrCategories = findViewById(R.id.spnrCategories)
        fillSpnr()
        gallery = findViewById(R.id.imgPresentation)
        gallery.registerLifecycle(lifecycle)
        fillGallery()
        tableCourses = findViewById(R.id.tableCourses)
        fillCourses()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setOnClickListener{
            val vtnMain = Intent(this, MainActivity::class.java)
            startActivity(vtnMain)
        }
    }

    override public fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)

        val searchItem = menu?.findItem(R.id.app_bar_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                courseSearch = query.toString()
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
    private fun fillSpnr()
    {
        var categoriesList = mutableListOf<String>()
        val queryFill = "SELECT NSUBCATEGORY FROM SUBCATEGORY"
        val cursor: Cursor = db.rawQuery(queryFill, null)

        if(cursor.moveToFirst())
        {
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

            spnrCategories.adapter = adapter
            spnrCategories.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
            {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long)
                {
                    var selectedItemActions = parentView?.getItemAtPosition(position).toString()

                    goBrowse(selectedItemActions, false)
                }

                override fun onNothingSelected(p0: AdapterView<*>?)
                {
                    //goBrowse(null)
                }
            }
        }
    }

    private fun goBrowse(browse: String?, spnrOrSearch: Boolean)
    {
        if(browse != null && controlSpinner > 0 && !spnrOrSearch)
        {
            val vtnBrowse = Intent(this, Browse::class.java)
            vtnBrowse.putExtra("subcategorySelected", browse)
            startActivity(vtnBrowse)
        }
        else if(browse != null && spnrOrSearch)
        {
            val vtnBrowse = Intent(this, Browse::class.java)
            vtnBrowse.putExtra("courseSearch", browse)
            startActivity(vtnBrowse)
        }
        controlSpinner++
    }

    private fun fillGallery()
    {
        val fillList = mutableListOf<CarouselItem>()

        fillList.add(
            CarouselItem(
                imageDrawable = R.drawable.one
            )
        )
        fillList.add(
            CarouselItem(
                imageDrawable = R.drawable.two
            )
        )
        fillList.add(
            CarouselItem(
                imageDrawable = R.drawable.three
            )
        )
        fillList.add(
            CarouselItem(
                imageDrawable = R.drawable.four
            )
        )
        fillList.add(
            CarouselItem(
                imageDrawable = R.drawable.five
            )
        )
        fillList.add(
            CarouselItem(
                imageDrawable = R.drawable.six
            )
        )

        gallery.setData(fillList)
    }

    @SuppressLint("Range")
    private fun fillCourses()
    {
        var listCourses = mutableListOf<Courses>()
        val queryFillCourses = "SELECT NCOURSE, DESCRIPTION, PRICE, TEACHER_COURSE FROM COURSE"
        val cursor = db.rawQuery(queryFillCourses, null)

        if(cursor.moveToFirst())
        {
            do
            {
                val titleCourse = cursor.getString(cursor.getColumnIndex("NCOURSE"))
                val descriptionCourse = cursor.getString(cursor.getColumnIndex("DESCRIPTION"))
                val priceCourse = cursor.getDouble(cursor.getColumnIndex("PRICE"))
                val idTeacher = cursor.getString(cursor.getColumnIndex("TEACHER_COURSE"))

                val courses = Courses(titleCourse, descriptionCourse, priceCourse, idTeacher)
                listCourses.add(courses)
            }while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = CoursesAdapter(this, R.layout.courses_table, true, listCourses)
        tableCourses.adapter = adapter

        tableCourses.setOnItemClickListener { _, _, position, _ ->
            val clickedCourse = listCourses[position]
            val titleCourse = clickedCourse.titleCourse.toString()
            val idTeacher = clickedCourse.teacherID.toString()

            val vtnCourse = Intent(this, CourseView::class.java)
            vtnCourse.putExtra("title", titleCourse)
            vtnCourse.putExtra("teacherID", idTeacher)
            startActivity(vtnCourse)
        }
    }
}