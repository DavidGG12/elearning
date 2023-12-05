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


    private lateinit var spnrTeacherCourses: Spinner
    private lateinit var btnAddTeacher: Button
    private lateinit var teacherRegisterResources: LinearLayout

    private lateinit var listViewCourses: ListView

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
            fillList(true)
            btnAddTeacher.visibility = View.VISIBLE
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


    @SuppressLint("Range")
    private fun fillList(isTeacher: Boolean)
    {
        var listFill = mutableListOf<Courses>()
        val idUser: String? = sharedPreferences.getString("idUser", "")
        var query: String = ""
        var values: Array<String> = emptyArray()

        if(isTeacher == true)
        {
            query = "SELECT NCOURSE, PRICE FROM COURSE WHERE TEACHER_COURSE = ?"
            values = arrayOf(idUser.toString())
        }
        else if(isTeacher == false)
        {
            //Check how to make the query
        }

        val cursor: Cursor = db.rawQuery(query, values)

        if(cursor.moveToFirst())
        {
            do
            {
                val titleCourse = cursor.getString(cursor.getColumnIndex("NCOURSE"))
                val priceCourse = cursor.getDouble(cursor.getColumnIndex("PRICE"))
                val courses = Courses(titleCourse, priceCourse)
                listFill.add(courses)
            }while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = CoursesAdapter(this, R.layout.courses_table, listFill)
        listViewCourses.adapter = adapter

        if(isTeacher == true)
        {
            listViewCourses.setOnItemClickListener{_, _, position, _ ->
                val clickedCourse = listFill[position]
                val titleCourse = clickedCourse.titleCourse.toString()
                val vtnCourse = Intent(this, CourseView::class.java)
                vtnCourse.putExtra("title", titleCourse)
                vtnCourse.putExtra("teacherID", sharedPreferences.getString("idUser", ""))
                startActivity(vtnCourse)
            }
        }
        else if(isTeacher == false)
        {

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
}