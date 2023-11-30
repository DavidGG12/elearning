package com.example.elearning

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.sql.SQLException
import kotlin.properties.Delegates

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
    private lateinit var lbNoFoundUser: TextView
    private lateinit var listViewUsers: ListView
    //An other shared preference to save the email of the teacher that we want to see their documents
    private lateinit var sharedPreferences_Pending: SharedPreferences
    private lateinit var editor_Pending: SharedPreferences.Editor

    //Category View
    private lateinit var categoryView: ConstraintLayout
    //Resources of the Category View
    private lateinit var txtCategory: EditText
    private lateinit var listViewCategory: ListView
    private lateinit var lbNoFoundCategory: TextView
    private lateinit var btnRegisterCategory: Button
    private var ID_UPDATE_CATEGORY by Delegates.notNull<String>()
    private var VALUE_REGISTER_UPDATE_CATEGORY: Int = 1

    //Subcategory View
    private lateinit var subcategoryView: ConstraintLayout
    //Resources of the Subcategory View
    private lateinit var txtSubcategory: EditText
    private lateinit var spnrCategory: Spinner
    private lateinit var lbNoFoundsSubcategory: TextView
    private lateinit var tableSubcategories: ListView
    private lateinit var btnRegisterSubcategory: Button
    private lateinit var ID_CATEGORY_SELECT: String
    private lateinit var ID_UPDATE_SUBCATEGORY: String
    private var VALUE_REGISTER_UPDATE_SUBCATEGORY: Int = 1

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
        lbNoFoundUser = findViewById(R.id.lbNoFounds)
        listViewUsers = findViewById(R.id.tableUsers)
        //Init of the shared preference
        sharedPreferences_Pending = getSharedPreferences("TeacherPending", Context.MODE_PRIVATE)
        editor_Pending = sharedPreferences_Pending.edit()

        //Category View
        categoryView = findViewById(R.id.categoryRegisterView)
        //Resources of the Category View
        lbNoFoundCategory = findViewById(R.id.lbNoFoundsCategory)
        txtCategory = findViewById(R.id.txtCategory)
        listViewCategory = findViewById(R.id.tableCategories)
        btnRegisterCategory = findViewById(R.id.btnRegisterCategory)

        //Subcategory View
        subcategoryView = findViewById(R.id.subcategoryRegisterView)
        //Resources of the Subcategory View
        txtSubcategory = findViewById(R.id.txtSubCategory)
        spnrCategory = findViewById(R.id.spnrCategory)
        lbNoFoundsSubcategory = findViewById(R.id.lbNoFoundsSubCategory)
        tableSubcategories = findViewById(R.id.tableSubCategories)
        btnRegisterSubcategory = findViewById(R.id.btnRegisterSubCategory)
        //Fill the spinner with the values in the table Category
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
        {
            R.id.adminRegister ->{
                welcomeView.visibility = View.GONE
                usersTableView.visibility = View.GONE
                categoryView.visibility = View.GONE
                subcategoryView.visibility = View.GONE
                registerView.visibility = View.VISIBLE
            }

            R.id.adminTable ->{
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                categoryView.visibility = View.GONE
                subcategoryView.visibility = View.GONE
                tableUsers(typeUser = "1", false)
                usersTableView.visibility = View.VISIBLE
            }

            R.id.usersTable ->{
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                categoryView.visibility = View.GONE
                subcategoryView.visibility = View.GONE
                tableUsers(typeUser = "2", false)
                usersTableView.visibility = View.VISIBLE
            }

            R.id.teachersTable -> {
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                categoryView.visibility = View.GONE
                subcategoryView.visibility = View.GONE
                tableUsers(typeUser = "3", false)
                usersTableView.visibility = View.VISIBLE
            }

            R.id.teachersDocuments -> {
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                categoryView.visibility = View.GONE
                subcategoryView.visibility = View.GONE
                tableUsers(typeUser = "2", true)
                usersTableView.visibility = View.VISIBLE
            }

            R.id.categoryRegister -> {
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                usersTableView.visibility = View.GONE
                subcategoryView.visibility = View.GONE
                categoryView.visibility = View.VISIBLE
                tableCategory()
            }

            R.id.subcategoryRegisterView -> {
                welcomeView.visibility = View.GONE
                registerView.visibility = View.GONE
                usersTableView.visibility = View.GONE
                categoryView.visibility = View.GONE
                subcategoryView.visibility = View.VISIBLE
                fillSpnrCategory()
            }

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


    //Function to register an admin
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

    //Function to show the tables of the administrators, users and teachers
    @SuppressLint("Range")
    private fun tableUsers(typeUser: String, flag: Boolean?)
    {
        if(flag == false)
        {
            //List where we save all the users
            var users = mutableListOf<User>()

            //Query
            val query = "SELECT ID_USER, EMAIL, PASSWORD FROM USER WHERE TYPE_USER_USER = ?"
            val clauses = arrayOf(typeUser)

            val cursor: Cursor = db.rawQuery(query, clauses)

            if(cursor.moveToFirst())
            {
                listViewUsers.visibility = View.VISIBLE
                lbNoFoundUser.visibility = View.GONE
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
                val adapter = UserAdapter(this, R.layout.users_table, users, false)
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
                lbNoFoundUser.visibility = View.VISIBLE
            }
        }
        else if(flag == true)
        {
            var teachersPending = mutableListOf<User>()

            val queryPending = "SELECT ID_USER, EMAIL FROM USER WHERE TYPE_USER_USER = ? AND INFORMATION_USER != ?"
            val values = arrayOf(typeUser, "NULL")
            val cursorPending: Cursor = db.rawQuery(queryPending, values)

            if(cursorPending.moveToFirst())
            {
                listViewUsers.visibility = View.VISIBLE
                lbNoFoundUser.visibility = View.GONE

                do
                {
                    val id = cursorPending.getInt(cursorPending.getColumnIndex("ID_USER"))
                    val email = cursorPending.getString(cursorPending.getColumnIndex("EMAIL"))

                    val userPending = User(id, email, null)
                    teachersPending.add(userPending)
                }while(cursorPending.moveToNext())

                cursorPending.close()

                val adapterPending = UserAdapter(this, R.layout.users_table, teachersPending, true)
                listViewUsers.adapter = adapterPending

                listViewUsers.setOnItemClickListener{_, _, position, _ ->
                    val clickedCategoryPending = teachersPending[position]
                    var emailPutOnSharedPreference = clickedCategoryPending.email.toString()
                    editor_Pending.putString("emailPending", emailPutOnSharedPreference)
                    editor_Pending.apply()
                    val vtnTeachersDocument = Intent(this, TeachersDocuments::class.java)
                    startActivity(vtnTeachersDocument)
                }
            }
            else
            {
                listViewUsers.visibility = View.GONE
                lbNoFoundUser.visibility = View.VISIBLE
            }
        }
    }

    //Function that the function "tableUsers" call to delete an user when the administrator click a register of the table
    private fun deleteUser(id: String, typeUser: String)
    {
        val query_delete = "DELETE FROM USER WHERE ID_USER = ?"
        val clause_delete = arrayOf(id)

        try
        {
            db.execSQL(query_delete, clause_delete)
            tableUsers(typeUser, false)
            Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
        }
        catch (e: SQLException)
        {
            Toast.makeText(this, "Algo ocurrió", Toast.LENGTH_SHORT).show()
        }
    }

    //Function to show the table Category
    @SuppressLint("Range")
    private fun tableCategory()
    {
        var categories = mutableListOf<Categories_Subcategories>()
        //query
        val query = "SELECT ID_CATEGORY, NCATEGORY FROM CATEGORY"

        val cursor: Cursor = db.rawQuery(query, null)

        if(cursor.moveToFirst())
        {
            do
            {
                val idCategory = cursor.getInt(cursor.getColumnIndex("ID_CATEGORY"))
                val nCategory = cursor.getString(cursor.getColumnIndex("NCATEGORY"))
                val category = Categories_Subcategories(idCategory, nCategory)
                categories.add(category)
            }while(cursor.moveToNext())

            cursor.close()

            val adapter = CategoryAdapter(this, R.layout.users_table, categories)
            listViewCategory.adapter = adapter
            listViewCategory.visibility = View.VISIBLE
            lbNoFoundCategory.visibility = View.GONE

            listViewCategory.setOnItemClickListener{_, _, position, _ ->
                val clickedCategory = categories[position]
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Categorías")
                builder.setMessage("¿Deseeas eliminar o actualizar esta categoría?")
                builder.setNeutralButton("Eliminar") { dialog, _ ->
                    deleteCategory(clickedCategory.idCategory.toString())
                }
                builder.setPositiveButton("Actualizar") { dialog, _ ->
                    btnRegisterCategory.text = "Actualizar"
                    VALUE_REGISTER_UPDATE_CATEGORY = 2
                    ID_UPDATE_CATEGORY = clickedCategory.idCategory.toString()
                }
                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
        else
        {
            listViewCategory.visibility = View.GONE
            lbNoFoundCategory.visibility = View.VISIBLE
        }
    }

    fun addCategory(v: View)
    {
        try
        {
            if(VALUE_REGISTER_UPDATE_CATEGORY == 1 && !txtCategory.text.toString().isNullOrBlank())
            {
                val txtAddCategory = txtCategory.text.toString()
                val query = "INSERT INTO CATEGORY(NCATEGORY) VALUES (?)"
                val values = arrayOf(txtAddCategory)
                db.execSQL(query, values)
                Toast.makeText(this, "Registrado", Toast.LENGTH_SHORT).show()
                txtCategory.text = null

                tableCategory()
            }
            else if(VALUE_REGISTER_UPDATE_CATEGORY == 2 && !txtCategory.text.toString().isNullOrBlank())
            {
                val txtAddCategory = txtCategory.text.toString()
                val query = "UPDATE CATEGORY SET NCATEGORY = ? WHERE ID_CATEGORY = ?"
                val values = arrayOf(txtAddCategory, ID_UPDATE_CATEGORY)
                db.execSQL(query, values)
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show()
                btnRegisterCategory.text = "Registrar"
                VALUE_REGISTER_UPDATE_CATEGORY = 1
                txtCategory.text = null

                tableCategory()
            }
            else
            {
                Toast.makeText(this, "Tienes que escribir una categoría", Toast.LENGTH_SHORT).show()
            }
        }
        catch (e: SQLException)
        {
            Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCategory(idCategory: String)
    {
        val query_deleteCategory = "DELETE FROM CATEGORY WHERE ID_CATEGORY = ?"
        val clauses_deleteCategory = arrayOf(idCategory)

        try
        {
            db.execSQL(query_deleteCategory, clauses_deleteCategory)
            tableCategory()
            Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
        }
        catch (e: SQLException)
        {
            Toast.makeText(this, "Algo salió mal al eliminarlo", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("Range")
    private fun fillSpnrCategory()
    {
        var categoriesList = mutableListOf<String>()
        val queryFill = "SELECT NCATEGORY FROM CATEGORY"
        val cursor: Cursor = db.rawQuery(queryFill, null)

        if(cursor.moveToFirst())
        {
            do
            {
                val nCategory = cursor.getString(cursor.getColumnIndex("NCATEGORY"))
                categoriesList.add(nCategory)
            }while (cursor.moveToNext())

            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categoriesList)

            spnrCategory.adapter = adapter
            spnrCategory.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
            {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long)
                {
                    var selectedItemActions = parentView?.getItemAtPosition(position).toString()

                    val query_RegisterFill = "SELECT ID_CATEGORY FROM CATEGORY WHERE NCATEGORY = ?"
                    val values_RegisterFill = arrayOf(selectedItemActions)
                    val cursor_ItemSelected: Cursor = db.rawQuery(query_RegisterFill, values_RegisterFill)

                    if(cursor_ItemSelected.moveToFirst())
                    {
                        ID_CATEGORY_SELECT = cursor_ItemSelected.getString(cursor_ItemSelected.getColumnIndex("ID_CATEGORY"))
                    }
                    cursor_ItemSelected.close()
                    tableSubcategory(ID_CATEGORY_SELECT)
                }

                override fun onNothingSelected(p0: AdapterView<*>?)
                {
                    TODO("Not yet implemented")
                }
            }
        }

        cursor.close()
    }

    @SuppressLint("Range")
    private fun tableSubcategory(idCategorySelected: String)
    {
        var subcategories = mutableListOf<Categories_Subcategories>()
        //query
        val query_tableSubcategories = "SELECT ID_SUBCATEGORY, NSUBCATEGORY FROM SUBCATEGORY WHERE CATEGORY_SUBCATEGORY = ?"
        val values_tableSubcategories = arrayOf(idCategorySelected)

        val cursor_tableSubcategories: Cursor = db.rawQuery(query_tableSubcategories, values_tableSubcategories)

        if(cursor_tableSubcategories.moveToFirst())
        {
            do
            {
                val idSubcategory = cursor_tableSubcategories.getInt(cursor_tableSubcategories.getColumnIndex("ID_SUBCATEGORY"))
                val nSubcategory = cursor_tableSubcategories.getString(cursor_tableSubcategories.getColumnIndex("NSUBCATEGORY"))
                val subcategory = Categories_Subcategories(idSubcategory, nSubcategory)
                subcategories.add(subcategory)
            }while(cursor_tableSubcategories.moveToNext())

            cursor_tableSubcategories.close()

            val adapterSubcategory = CategoryAdapter(this, R.layout.subcategory_register, subcategories)
            tableSubcategories.adapter = adapterSubcategory
            tableSubcategories.visibility = View.VISIBLE
            lbNoFoundsSubcategory.visibility = View.GONE

            tableSubcategories.setOnItemClickListener{_, _, position, _ ->
                val clickedSubcategory = subcategories[position]
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Subcategorías")
                builder.setMessage("¿Deseas eliminar o actualizar esta subcategoría?")
                builder.setNeutralButton("Eliminar"){dialog, _ ->
                    deleteSubcategory(clickedSubcategory.idCategory.toString())
                }
                builder.setPositiveButton("Actualizar"){dialog, _ ->
                    btnRegisterSubcategory.text = "Actualizar"
                    VALUE_REGISTER_UPDATE_SUBCATEGORY = 2
                    ID_UPDATE_SUBCATEGORY = clickedSubcategory.idCategory.toString()
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
            tableSubcategories.visibility = View.GONE
            lbNoFoundsSubcategory.visibility = View.VISIBLE
        }
    }

    fun addSubCategory(v: View)
    {
        try
        {
            if(VALUE_REGISTER_UPDATE_SUBCATEGORY == 1 && !txtSubcategory.text.toString().isNullOrBlank())
            {
                val txtAddSubcategory = txtSubcategory.text.toString()
                val queryAddSubcategory = "INSERT INTO SUBCATEGORY(NSUBCATEGORY, CATEGORY_SUBCATEGORY) VALUES (?, ?)"
                val valuesAddSubcategory = arrayOf(txtAddSubcategory, ID_CATEGORY_SELECT)
                db.execSQL(queryAddSubcategory, valuesAddSubcategory)
                Toast.makeText(this, "Registrado", Toast.LENGTH_SHORT).show()
                txtSubcategory.text = null

                tableSubcategory(ID_CATEGORY_SELECT)
            }
            else if(VALUE_REGISTER_UPDATE_SUBCATEGORY == 2 && !txtSubcategory.text.toString().isNullOrBlank())
            {
                val txtAddSubcategory = txtSubcategory.text.toString()
                val queryUpdate = "UPDATE SUBCATEGORY SET NSUBCATEGORY = ? WHERE ID_SUBCATEGORY = ? AND CATEGORY_SUBCATEGORY = ?"
                val valuesUpdate = arrayOf(txtAddSubcategory, ID_UPDATE_SUBCATEGORY, ID_CATEGORY_SELECT)
                db.execSQL(queryUpdate, valuesUpdate)
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show()
                btnRegisterSubcategory.text = "Registrar"
                VALUE_REGISTER_UPDATE_SUBCATEGORY = 1
                txtSubcategory.text = null

                tableSubcategory(ID_CATEGORY_SELECT)
            }
        }
        catch (e: SQLException)
        {
            Toast.makeText(this, "Algo salió mal en el registro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSubcategory(id: String)
    {
        val query_deleteSubcategory = "DELETE FROM SUBCATEGORY WHERE ID_SUBCATEGORY = ?"
        val clause_deleteSubcategory = arrayOf(id)
        try
        {
            db.execSQL(query_deleteSubcategory, clause_deleteSubcategory)
            tableSubcategory(ID_CATEGORY_SELECT)
            Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
        }
        catch (e: SQLException)
        {
            Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
        }
    }
}


