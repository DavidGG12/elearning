package com.example.elearning

import android.content.Context
import android.widget.ArrayAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.w3c.dom.Text


class functions()
{
    public fun emptyOrNot(string: String): Boolean
    {
        return if(!string.isEmpty() && !string.isBlank()) false else true
    }

    public fun validateEmail(email: String): Boolean
    {
        var flag = true
        val patronEmail = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")

        if(!patronEmail.matches(email))
        {
            flag = false
        }

        return flag
    }

    public fun validatePassword(mainPassword: String, repeatPassword: String): String
    {
        var message: String = ""

        if(mainPassword.length >= 8)
        {
            if(mainPassword != repeatPassword)
            {
                message = "Contraseñas no coinciden"
            }
        }
        else
        {
            message = "La contraseña debe tener al menos 8 caracteres"
        }

        return message
    }
}


data class User(val id: Int, val email: String, val password: String?)

class UserAdapter(context: Context, resource: Int, objects: List<User>, flag: Boolean?): ArrayAdapter<User>(context, resource, objects)
{
    var flag = flag
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val user = getItem(position)

        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.users_table, parent, false)

        val textId: TextView = view.findViewById(R.id.txtViewId)
        val textEmail: TextView = view.findViewById(R.id.txtViewEmail)
        val textPassword: TextView = view.findViewById(R.id.txtViewPassword)

        if(flag == false)
        {
            textId.text = user?.id.toString()
            textEmail.text = user?.email
            textPassword.text = user?.password
        }
        else if(flag == true)
        {
            textId.text = user?.id.toString()
            textEmail.text = user?.email
            textPassword.text = "Pendiente"
        }

        return view
    }
}

data class Categories_Subcategories(val idCategory: Int, val nCategory: String)
class CategoryAdapter(context: Context, resource: Int, objects: List<Categories_Subcategories>): ArrayAdapter<Categories_Subcategories>(context, resource, objects)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val user = getItem(position)

        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.users_table, parent, false)

        val textId: TextView = view.findViewById(R.id.txtViewId)
        val textEmail: TextView = view.findViewById(R.id.txtViewEmail)
        val textPassword: TextView = view.findViewById(R.id.txtViewPassword)

        textId.text = user?.idCategory.toString()
        textEmail.text = user?.nCategory
        textPassword.visibility = View.GONE

        return view
    }
}

data class Documents_Teachers(val nDocument: String)

class DocumentsAdapter(context: Context, resource: Int, objects: List<Documents_Teachers>): ArrayAdapter<Documents_Teachers>(context, resource, objects)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val document = getItem(position)

        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.documents_table, parent, false)

        val textName: TextView = view.findViewById(R.id.txtViewName)

        textName.text = document?.nDocument

        return view
    }
}

data class Courses(val titleCourse: String, val priceCourse: Double)

class CoursesAdapter(context: Context, resource: Int, objects: List<Courses>): ArrayAdapter<Courses>(context, resource, objects)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val course = getItem(position)

        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.courses_table, parent, false)

        val titleCourse: TextView = view.findViewById(R.id.txtViewTitleCourse)
        val priceCourses: TextView = view.findViewById(R.id.txtViewPrice)

        titleCourse.text = course?.titleCourse
        if(priceCourses != null)
        {
            priceCourses.text = "$ " + course?.priceCourse.toString()
        }
        else
        {
            priceCourses.text = "Propio"
        }

        return view
    }
}