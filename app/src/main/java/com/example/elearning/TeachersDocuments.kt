package com.example.elearning

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Text
import java.io.File
import java.sql.SQLException

class TeachersDocuments : AppCompatActivity()
{
    //Toolbar
    private lateinit var toolbarTeachers: Toolbar

    //Shared Preference
    private lateinit var emailTeacher_sharedPreference: SharedPreferences
    private lateinit var emailTeacher_editor: SharedPreferences.Editor

    //Firebase
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference

    //Database
    private lateinit var dbHelper: connection
    private lateinit var db: SQLiteDatabase

    //Path
    private lateinit var path_DocumentsTeacher: String

    //Resources
    private lateinit var lbDocument: TextView
    private lateinit var load: ProgressBar
    private lateinit var listDocumentView: ListView
    private lateinit var scrDocumentView: ScrollView
    //Resources of the scroll view
    private lateinit var pdfViewer: com.pdfview.PDFView
    private lateinit var btnAccept: Button
    private lateinit var btnDeny: Button

    //Document counter: this variable is only to save if the admin accept all the documents or deny the documents
    private var counterDocumentAccepted = 0
    private var counterDocumentDenied = 0
    //A list to save if in case the admin has already seen a file and accepted it or denied it
    private var listDocumentsAccepted: MutableList<String> = mutableListOf("Archivos: ")
    private var listDocumentDenied: MutableList<String> = mutableListOf("Archivos: ")
    private lateinit var currentDocument: String


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teachers_documents)

        //Init the firebase
        FirebaseApp.initializeApp(this)

        //Init Database
        dbHelper = connection(this)
        db = dbHelper.writableDatabase

        //Init of the toolbar
        toolbarTeachers = findViewById(R.id.toolbarTeachers)
        setSupportActionBar(toolbarTeachers)

        //Set the button back enable
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        //Init Shared Preference
        emailTeacher_sharedPreference = getSharedPreferences("TeacherPending", Context.MODE_PRIVATE)
        emailTeacher_editor = emailTeacher_sharedPreference.edit()

        //Resources
        lbDocument = findViewById(R.id.lbDocument)
        load = findViewById(R.id.loadPDF)
        listDocumentView = findViewById(R.id.listDocuments)
        scrDocumentView = findViewById(R.id.scrViewDocument)
        //Resources of the scroll view
        pdfViewer = findViewById(R.id.pdfView)
        btnAccept = findViewById(R.id.btnSuccess)
        btnDeny = findViewById(R.id.btnDeny)

        //Init Path
        path_DocumentsTeacher = "/teachers/${emailTeacher_sharedPreference.getString("emailPending", "").toString()}/documents/"

        load.visibility = View.VISIBLE
        getNameDocuments()
    }

    private fun getNameDocuments()
    {
        val pathReference: StorageReference = storageReference.child(path_DocumentsTeacher)
        var documentsList = mutableListOf<Documents_Teachers>()

        pathReference.listAll()
            .addOnSuccessListener { result ->
                for (item in result.items)
                {
                    val nDocument = item.name
                    val documentData = Documents_Teachers(nDocument)
                    documentsList.add(documentData)
                }

                val adapterDocument = DocumentsAdapter(this, R.layout.documents_table, documentsList)
                listDocumentView.adapter = adapterDocument

                load.visibility = View.GONE
                listDocumentView.visibility = View.VISIBLE

                listDocumentView.setOnItemClickListener{_, _, position, _ ->
                    val clickedListDocument = documentsList[position]
                    val nDocumentToView = clickedListDocument.nDocument

                    lbDocument.visibility = View.GONE
                    listDocumentView.visibility = View.GONE
                    load.visibility = View.VISIBLE

                    generateViewPDF(this, nDocumentToView)
                }
            }
            .addOnFailureListener{e ->
                Toast.makeText(this, "Error al obtener archivos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateViewPDF(context: Context, nDocument: String)
    {
        val referenceDocument = storageReference.child(path_DocumentsTeacher).child(nDocument)
        val pathSave = File(context.getExternalFilesDir(null), "IADMDownload")

        if(!pathSave.exists())
        {
            pathSave.mkdir()
        }

        val downloadDocument = File(pathSave, nDocument)

        referenceDocument.getFile(downloadDocument)
            .addOnSuccessListener {
                currentDocument = nDocument

                load.visibility = View.GONE
                scrDocumentView.visibility = View.VISIBLE

                pdfViewer.fromFile(downloadDocument)
                pdfViewer.isZoomEnabled = true
                pdfViewer.show()

                if (listDocumentsAccepted.find { it == currentDocument } != null) { btnAccept.isEnabled = false }
                if (listDocumentDenied.find { it == currentDocument } != null) { btnDeny.isEnabled = false }

            }
            .addOnFailureListener{e ->
                Toast.makeText(this, "No se pudo descargar el archivo", Toast.LENGTH_LONG).show()
            }
    }

    fun documentVerify(v: View)
    {
        counterDocumentAccepted++
        if(counterDocumentAccepted == 2)
        {
            documentsAccepted()
        }
        else
        {
            scrDocumentView.visibility = View.GONE
            lbDocument.visibility = View.VISIBLE
            listDocumentView.visibility = View.VISIBLE

            Toast.makeText(this, "Aceptado", Toast.LENGTH_SHORT).show()

            currentDocument = ""
            if (listDocumentDenied.find { it == currentDocument } != null)
            {
                listDocumentsAccepted.add(currentDocument)
                listDocumentDenied.remove(currentDocument)
            }
        }
    }

    fun denyDocument(v: View)
    {
        counterDocumentDenied++
        if(counterDocumentDenied == 2)
        {
            documentsDenied()
        }
        else
        {
            scrDocumentView.visibility = View.GONE
            lbDocument.visibility = View.VISIBLE
            listDocumentView.visibility = View.VISIBLE

            Toast.makeText(this, "Denegado", Toast.LENGTH_SHORT).show()

            currentDocument = ""

            if (listDocumentsAccepted.find { it == currentDocument } != null)
            {
                listDocumentsAccepted.remove(currentDocument)
                listDocumentDenied.add(currentDocument)
            }
        }
    }

    fun back(v: View)
    {
        scrDocumentView.visibility = View.GONE
        lbDocument.visibility = View.VISIBLE
        listDocumentView.visibility = View.VISIBLE
    }

    private fun documentsAccepted()
    {
        try
        {
            val emailNewTeacher = emailTeacher_sharedPreference.getString("emailPending", "")
            val queryUpdate_NewTeacher = "UPDATE USER SET TYPE_USER_USER = ? WHERE EMAIL = ? AND INFORMATION_USER != ?"
            val values_update = arrayOf("3", emailNewTeacher, "NULL")

            db.execSQL(queryUpdate_NewTeacher, values_update)

            Toast.makeText(this, "Profesor Aceptado", Toast.LENGTH_SHORT).show()

            val vtnBackAdmin = Intent(this, Admin::class.java)
            startActivity(vtnBackAdmin)
        }
        catch (e: SQLException)
        {
            Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("Range")
    private fun documentsDenied()
    {
        try
        {
            val emailDeleteTeacher = emailTeacher_sharedPreference.getString("emailPending", "")
            var idInfoTeacher = ""
            val querySelect_TeacherIdInfo = "SELECT INFORMATION_USER FROM USER WHERE EMAIL = ?"
            val valuesSelect = arrayOf(emailDeleteTeacher)
            val cursor: Cursor = db.rawQuery(querySelect_TeacherIdInfo, valuesSelect)

            if(cursor.moveToFirst()) { idInfoTeacher = cursor.getString(cursor.getColumnIndex("INFORMATION_USER")) }
            else
            {
                Toast.makeText(this, "Error al eliminarlo", Toast.LENGTH_SHORT).show()
                val vtnReload = Intent(this, TeachersDocuments::class.java)
                startActivity(vtnReload)
            }

            val queryUpdateUser = "UPDATE USER SET INFORMATION_USER = ? WHERE EMAIL = ?"
            val valuesUpdate = arrayOf(null, emailDeleteTeacher)
            db.execSQL(queryUpdateUser, valuesUpdate)

            val queryDeleteInformation = "DELETE FROM INFORMATION WHERE ID_INFORMATION = ?"
            val valuesDelete = arrayOf(idInfoTeacher)
            db.execSQL(queryDeleteInformation, valuesDelete)

            val pathDelete = "/teachers/${emailDeleteTeacher}/"
            deleteDocuments(pathDelete)
            val pathReferenceToDelete = storageReference.child(pathDelete)

            pathReferenceToDelete.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Profesor Denegado", Toast.LENGTH_SHORT).show()

                    val vtnBackAdmin = Intent(this, Admin::class.java)
                    startActivity(vtnBackAdmin)
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Profesor Denegado", Toast.LENGTH_SHORT).show()

                    val vtnBackAdmin = Intent(this, Admin::class.java)
                    startActivity(vtnBackAdmin)
                }
        }
        catch (e: SQLException)
        {
            Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteDocuments(path: String)
    {
        val deleteDocuments = storageReference.child(path).child("documents/")

        deleteDocuments.listAll()
            .addOnSuccessListener {result ->
                for(file in result.items)
                {
                    file.delete()
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener{
                            Toast.makeText(this, "No se pudieron eliminar los archivos", Toast.LENGTH_SHORT).show()
                        }
                }
                deleteDocuments.delete()
            }
            .addOnFailureListener{
                Toast.makeText(this, "No se pudo acceder a la carpeta", Toast.LENGTH_SHORT).show()
            }
    }
}