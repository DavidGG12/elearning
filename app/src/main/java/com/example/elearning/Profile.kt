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
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import org.w3c.dom.Text
import java.sql.SQLException

class Profile : AppCompatActivity()
{
    private lateinit var toolbar: Toolbar
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var txtWelcome: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtBxDescription: EditText

    private lateinit var btnAddTeacher: Button
    private lateinit var teacherRegisterResources: LinearLayout

    private lateinit var listViewCourses: ListView
    private lateinit var lbUserCourses: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //Save the session
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        txtWelcome = findViewById(R.id.txtWelcome)
        txtWelcome.setText("¡Bienvenido " + sharedPreferences.getString("nameUser", "") + "!")

        txtEmail = findViewById(R.id.txtEmail)
        txtEmail.setText(sharedPreferences.getString("emailUser", ""))

        txtDescription = findViewById(R.id.txtDescription)
        txtBxDescription = findViewById(R.id.txtBxDescription)

        listViewCourses = findViewById(R.id.listViewCourses)
        lbUserCourses = findViewById(R.id.lbUserCourses)

        teacherRegisterResources = findViewById(R.id.teacherRegister)
        btnAddTeacher = findViewById(R.id.btnAddCourse)

        val valueDescription = sharedPreferences.getString("descriptionUser", "")
        val validateTeacher = sharedPreferences.getString("typeUser", "")

        if(!valueDescription.isNullOrBlank())
        {
            txtDescription.setText(valueDescription)
        }
        else
        {
            txtDescription.setText("Sin Descripción")
        }

        if(validateTeacher == "3")
        {
            teacherRegisterResources.visibility = View.GONE
            listViewCourses.visibility = View.VISIBLE
            lbUserCourses.visibility = View.GONE
            fillList(true)
            btnAddTeacher.visibility = View.VISIBLE
        }
        else
        {
            fillList(false)
        }

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
    private fun fillList(isTeacher: Boolean)
    {
        var listFill = mutableListOf<Courses>()
        val idUser: String? = sharedPreferences.getString("idUser", "")
        var query: String = ""
        var values: Array<String> = emptyArray()

        if(isTeacher == true)
        {
            query = "SELECT NCOURSE, DESCRIPTION, PRICE, TEACHER_COURSE FROM COURSE WHERE TEACHER_COURSE = ?"
            values = arrayOf(idUser.toString())
        }
        else
        {
            query = "SELECT NCOURSE, DESCRIPTION, PRICE, TEACHER_COURSE FROM SHOPPING_CAR " +
                    "INNER JOIN COURSE ON SHOPPING_CAR.COURSE_CAR = COURSE.ID_COURSE " +
                    "WHERE USER_CAR = ? AND STATUS = ?"
            values = arrayOf(sharedPreferences.getString("idUser", "").toString(), "PURCHASED")
        }

        val cursor: Cursor = db.rawQuery(query, values)

        if(cursor.moveToFirst())
        {
            do
            {
                val titleCourse = cursor.getString(cursor.getColumnIndex("NCOURSE"))
                val descriptionCourse = cursor.getString(cursor.getColumnIndex("DESCRIPTION"))
                var priceCourse: Double? = 0.0
                if(isTeacher == true) {priceCourse = cursor.getDouble(cursor.getColumnIndex("PRICE"))}
                else {priceCourse = null}
                val idTeacher = cursor.getString(cursor.getColumnIndex("TEACHER_COURSE"))

                val courses = Courses(titleCourse, descriptionCourse, priceCourse, idTeacher)
                listFill.add(courses)
            }while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = CoursesAdapter(this, R.layout.courses_table, false, listFill)
        listViewCourses.adapter = adapter

        if(isTeacher == true)
        {
            listViewCourses.setOnItemClickListener{_, _, position, _ ->
                val clickedCourse = listFill[position]
                val titleCourse = clickedCourse.titleCourse.toString()
                val vtnCourse = Intent(this, CourseView::class.java)
                vtnCourse.putExtra("title", titleCourse)
                vtnCourse.putExtra("teacherID", idUser)
                startActivity(vtnCourse)
            }
        }
    }


    public fun addDescription(v: View)
    {
        if(txtDescription.visibility == View.VISIBLE)
        {
            txtDescription.visibility = View.GONE
            txtBxDescription.visibility = View.VISIBLE
        }
        else
        {
            var descriptionRegister: String? = txtBxDescription.text.toString()

            when(descriptionRegister.isNullOrBlank())
            {
                false -> {
                    var email = sharedPreferences.getString("emailUser", "")
                    var password = sharedPreferences.getString("passwordUser", "")

                    try
                    {
                        var queryDescriptionRegister = "UPDATE USER SET DESCRIPTION = ? WHERE EMAIL = ? AND PASSWORD = ?"
                        var clausesDescriptionRegister = arrayOf(descriptionRegister, email, password)

                        db.execSQL(queryDescriptionRegister, clausesDescriptionRegister)

                        editor.putString("descriptionUser", descriptionRegister)
                        editor.apply()
                        Toast.makeText(this, "Hecho", Toast.LENGTH_SHORT).show()

                        txtDescription.setText(descriptionRegister)
                        txtDescription.visibility = View.VISIBLE
                        txtBxDescription.visibility = View.GONE
                    }
                    catch (e: SQLException)
                    {
                        Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    txtDescription.visibility = View.VISIBLE
                    txtBxDescription.visibility = View.GONE
                }
            }
        }
    }

    public fun addPaymentMethod(v: View)
    {
        var intent = Intent(this, Browse::class.java)
        startActivity(intent)
    }

    public fun logout(v: View)
    {
        editor.remove("idUser")
        editor.remove("emailUser")
        editor.remove("passwordUser")
        editor.remove("nameUser")
        editor.remove("pSurNameUser")
        editor.remove("mSurNameUser")
        editor.remove("typeUser")
        editor.apply()
        Toast.makeText(this, "¡Adiós!", Toast.LENGTH_SHORT).show()
        val vtnMain = Intent(this, MainActivity::class.java)
        startActivity(vtnMain)
    }

    public fun vtnTeacherRegister(v: View)
    {
        val vtnTeacherRegister = Intent(this, RegisterTeacher::class.java)
        startActivity(vtnTeacherRegister)
    }

    fun addCourse(v: View)
    {
        val vtnAddCourse = Intent(this, AddCourse::class.java)
        startActivity(vtnAddCourse)
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