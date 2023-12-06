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
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar

class ShoppingCar : AppCompatActivity()
{
    //Shared Preference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    //Connection
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase

    //Toolbar
    private lateinit var toolbar: Toolbar

    //Resources
    private lateinit var lbWelcome: TextView
    private lateinit var lbWithoutCar: TextView
    private lateinit var scrCar: ScrollView
    private lateinit var tableCar: ListView
    private lateinit var txtTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_car)

        //Shared Preference
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        //Connection
        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        //Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Resources
        lbWelcome = findViewById(R.id.lbShopUser)
        lbWithoutCar = findViewById(R.id.lbWithoutCar)
        scrCar = findViewById(R.id.scrCar)
        tableCar = findViewById(R.id.tableCar)
        txtTotal = findViewById(R.id.txtTotal)

        val validateSession = sharedPreferences.getString("idUser", "")

        if(validateSession.isNullOrBlank())
        {
            val vtnLogin = Intent(this, Login::class.java)
            startActivity(vtnLogin)
        }

        fillData()
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
    private fun fillData()
    {
        var listCar = mutableListOf<Courses>()
        var total: Double = 0.0
        val userName = sharedPreferences.getString("nameUser", "")
        lbWelcome.text = "¡Carrito de ${userName}!"

        val queryCar = "SELECT NCOURSE, DESCRIPTION, PRICE, TEACHER_COURSE FROM SHOPPING_CAR" +
                " INNER JOIN COURSE ON SHOPPING_CAR.COURSE_CAR = COURSE.ID_COURSE" +
                " WHERE USER_CAR = ? AND STATUS = ?"
        val valuesCar = arrayOf(sharedPreferences.getString("idUser", ""), "PENDING")
        val cursor = db.rawQuery(queryCar, valuesCar)

        if(cursor.moveToFirst())
        {
            do
            {
                val title = cursor.getString(cursor.getColumnIndex("NCOURSE"))
                val description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"))
                val price = cursor.getDouble(cursor.getColumnIndex("PRICE"))
                total = total + price
                val idTeacher = cursor.getString(cursor.getColumnIndex("TEACHER_COURSE"))
                val courses = Courses(title, description, price, idTeacher)
                listCar.add(courses)
            }while (cursor.moveToNext())

            cursor.close()

            val adapter = CoursesAdapter(this, R.layout.courses_table, false, listCar)
            tableCar.adapter = adapter

            txtTotal.text = "$ ${total}"

            tableCar.setOnItemClickListener{_, _, position, _ ->
                val clickedCourse = listCar[position]
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Eliminar")
                builder.setMessage("¿Deseas eliminar este curso de tu carrito?")
                builder.setPositiveButton("Aceptar"){dialog, _ ->
                    deleteCar(clickedCourse.titleCourse, clickedCourse.teacherID)
                }
                builder.setNegativeButton("Cancelar"){dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
        else
        {
            lbWithoutCar.visibility = View.VISIBLE
            scrCar.visibility = View.GONE
        }
    }

    @SuppressLint("Range")
    private fun deleteCar(titleCourse: String, idTeacher: String)
    {
        val idUser = sharedPreferences.getString("idUser", "")
        var query = "SELECT ID_COURSE FROM COURSE WHERE NCOURSE = ? AND TEACHER_COURSE = ?"
        var values = arrayOf(titleCourse, idTeacher)
        var cursor = db.rawQuery(query, values)

        if(cursor.moveToFirst())
        {
            var idCourse = cursor.getString(cursor.getColumnIndex("ID_COURSE"))
            db.execSQL("DELETE FROM SHOPPING_CAR WHERE USER_CAR = $idUser AND COURSE_CAR = $idCourse")
        }
        fillData()
        Toast.makeText(this, "Removido", Toast.LENGTH_SHORT).show()
    }

    fun buy(v: View)
    {
        db.execSQL("UPDATE SHOPPING_CAR SET STATUS = 'PURCHASED' WHERE USER_CAR = ${sharedPreferences.getString("idUser", "")}")
        fillData()
        Toast.makeText(this, "Comprado", Toast.LENGTH_SHORT).show()
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