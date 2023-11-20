package com.example.elearning

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


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


data class User(val id: Int, val email: String, val password: String)

class UserAdapter(context: Context, resource: Int, objects: List<User>): ArrayAdapter<User>(context, resource, objects)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val user = getItem(position)

        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.users_table, parent, false)

        val textId: TextView = view.findViewById(R.id.txtViewId)
        val textEmail: TextView = view.findViewById(R.id.txtViewEmail)
        val textPassword: TextView = view.findViewById(R.id.txtViewPassword)

        textId.text = user?.id.toString()
        textEmail.text = user?.email
        textPassword.text = user?.password

        return view
    }
}