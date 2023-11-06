package com.example.elearning

import android.content.Context
import android.content.SharedPreferences

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