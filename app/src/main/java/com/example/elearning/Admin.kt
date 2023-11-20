package com.example.elearning

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.DrawerLayoutUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.w3c.dom.Text
import java.sql.SQLException

class Admin : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
{
    //Connection
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase

    //Menu
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    //Views
    //Welcome View
    private lateinit var welcomeView: ConstraintLayout

    //Register View
    private lateinit var registerView: ConstraintLayout
    //Resources of the Register View
    private lateinit var name: EditText
    private lateinit var pSurName: EditText
    private lateinit var mSurName: EditText
    private lateinit var email: EditText
    private lateinit var mainPassword: EditText
    private lateinit var repeatPassword: EditText

    //Users View
    private lateinit var usersTableView: ConstraintLayout
    //Resources of the Users View
    private lateinit var lbNoFound: TextView
    private lateinit var listViewUsers: ListView


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        //Connection
        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        //Menu
        val toolbar: Toolbar = findViewById(R.id.toolbar_bar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layoutAdmin)

        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        //Views
        //Welcome View
        welcomeView = findViewById(R.id.welcomeView)

        //Register View
        registerView = findViewById(R.id.registerView)
        //Identification of the TextBox
        name = findViewById(R.id.bxNameRegister)
        pSurName = findViewById(R.id.bxPSNameRegister)
        mSurName = findViewById(R.id.bxMSNameRegister)
        email = findViewById(R.id.bxEmailRegister)
        mainPassword = findViewById(R.id.bxMainPasswordRegister)
        repeatPassword = findViewById(R.id.bxRepeatPasswordRegister)

        //Users View
        usersTableView = findViewById(R.id.usersView)
        //Resources of the Users View
        lbNoFound = findViewById(R.id.lbNoFounds)
        listViewUsers = findViewById(R.id.tableUsers)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
        {
            R.id.adminRegister ->{
                welcomeView.visibility = View.GONE
                usersTableView.visibility = View.GONE
                registerView.visibility = View.VISIBLE
            }

            R.id.adminTable ->{
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                tableUsers(typeUser = "1")
                usersTableView.visibility = View.VISIBLE
            }

            R.id.usersTable ->{
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                tableUsers(typeUser = "2")
                usersTableView.visibility = View.VISIBLE
            }

            R.id.teachersTable -> {
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                tableUsers(typeUser = "3")
                usersTableView.visibility = View.VISIBLE
            }

            R.id.teachersDocuments -> Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()

            R.id.categoryRegisterView -> Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()

            R.id.subcategoryRegisterView -> Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()

            R.id.sealsTickets -> Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()

            R.id.profile -> {
                val vtnProfile = Intent(this, Profile::class.java)
                startActivity(vtnProfile)
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?)
    {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration)
    {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        if(toggle.onOptionsItemSelected(item))
        {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    public fun register(v: View)
    {
        var res: functions = functions()

        val typeUser: Int = 1
        var nameRegister = name.text.toString()
        var pSurNameRegister = pSurName.text.toString()
        var mSurNameRegister = mSurName.text.toString()
        var emailRegister = email.text.toString()
        var mainPasswordRegister = mainPassword.text.toString()
        var repeatPasswordRegister = repeatPassword.text.toString()
        var validatePasswords = res.validatePassword(mainPasswordRegister, repeatPasswordRegister)

        if(res.emptyOrNot(nameRegister) && res.emptyOrNot(pSurNameRegister)
            && res.emptyOrNot(mSurNameRegister) && res.emptyOrNot(emailRegister)
            && res.emptyOrNot(mainPasswordRegister) && res.emptyOrNot(repeatPasswordRegister))
        {
            Toast.makeText(this, "Alguno de los campos está vacío", Toast.LENGTH_LONG).show()
        }
        else
        {
            if(!res.validateEmail(emailRegister)){Toast.makeText(this, "Correo Inválido", Toast.LENGTH_LONG).show()}
            else
            {
                if(validatePasswords == "Contraseñas no coinciden" || validatePasswords == "La contraseña debe tener al menos 8 caracteres"){Toast.makeText(this, validatePasswords, Toast.LENGTH_LONG).show()}
                else
                {
                    try
                    {
                        var query_register = "INSERT INTO USER (NUSER, PSURNAME, MSURNAME, EMAIL, PASSWORD, TYPE_USER_USER) VALUES (?, ?, ?, ?, ?, ?)"
                        var values = arrayOf(nameRegister, pSurNameRegister, mSurNameRegister, emailRegister, mainPasswordRegister, typeUser)

                        db.execSQL(query_register, values)

                        //Place a vtn for the users table
                        Toast.makeText(this, "Bien", Toast.LENGTH_SHORT).show()
                    }
                    catch (e: SQLException)
                    {
                        Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    @SuppressLint("Range")
    fun tableUsers(typeUser: String)
    {
        //List where we save all the users
        var users = mutableListOf<User>()

        //Query
        val query = "SELECT ID_USER, EMAIL, PASSWORD FROM USER WHERE TYPE_USER_USER = ?"
        val clauses = arrayOf(typeUser)

        val cursor: Cursor = db.rawQuery(query, clauses)

        if(cursor.moveToFirst())
        {
            do
            {
                val id = cursor.getInt(cursor.getColumnIndex("ID_USER"))
                val email = cursor.getString(cursor.getColumnIndex("EMAIL"))
                val password = cursor.getString(cursor.getColumnIndex("PASSWORD"))

                val user = User(id, email, password)
                users.add(user)
            } while (cursor.moveToNext())

            cursor.close()

            //val listViewUsers: ListView = findViewById(R.id.tableUsers)
            val adapter = UserAdapter(this, R.layout.users_table, users)
            listViewUsers.adapter = adapter

            listViewUsers.setOnItemClickListener { _, _, position, _ ->
                val clickedUser = users[position]
                //Toast.makeText(this, "Email: ${clickedUser.email}", Toast.LENGTH_SHORT).show()
                if(clickedUser.id.toString() != "1")
                {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Eliminar")
                    builder.setMessage("¿Deseeas eliminar a este usuario?")
                    builder.setPositiveButton("Aceptar") { dialog, _ ->
                        deleteUser(clickedUser.id.toString(), typeUser)
                    }
                    builder.setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }
        else
        {
            listViewUsers.visibility = View.GONE
            lbNoFound.visibility = View.VISIBLE
        }
    }

    fun deleteUser(id: String, typeUser: String)
    {
        val query_delete = "DELETE FROM USER WHERE ID_USER = ?"
        val clause_delete = arrayOf(id)

        try
        {
            db.execSQL(query_delete, clause_delete)
            tableUsers(typeUser)
            Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
        }
        catch (e: SQLException)
        {
            Toast.makeText(this, "Algo ocurrió", Toast.LENGTH_SHORT).show()
        }
    }
}