package com.example.elearning

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class connection(context: Context): SQLiteOpenHelper(context, "elearning.db", null, 2)
{
    /*
    *
    *
    * CREATION OF THE TABLES WITHOUT FOREIGN KEY
    *
    *
    */
    private final val TB_PRUEBA = "CREATE TABLE LOL (ID INTEGER)"

    private final val TB_CATEGORY = "CREATE TABLE CATEGORY " +
            "(ID_CATEGORY INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NCATEGORY TEXT NOT NULL UNIQUE)"

    private final val TB_CONTENT = "CREATE TABLE CONTENT (\n" +
            "    ID_CONTENT INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    NCONTENT TEXT NOT NULL,\n" +
            "    RUTH_CONTENT TEXT NOT NULL,\n" +
            "    SECTION_CONTENT INTEGER NOT NULL,\n" +
            "    FOREIGN KEY (SECTION_CONTENT) REFERENCES SECTION(ID_SECTION)" +
            ")"

    private final val TB_PAY_METHOD = "CREATE TABLE PAY_METHOD (\n" +
            "    ID_METHOD INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    NMETHOD TEXT NOT NULL\n" +
            ")"

    private final val TB_TYPE_USER = "CREATE TABLE TYPE_USER (\n" +
            "    ID_TYPE INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    NTYPE TEXT NOT NULL UNIQUE\n" +
            ")"

    private final val TB_INFORMATION = "CREATE TABLE INFORMATION (\n" +
            "    ID_INFORMATION INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    RUTH_CURP TEXT NOT NULL,\n" +
            "    RUTH_CURRENCY TEXT NOT NULL,\n" +
            "    LADA TEXT NOT NULL,\n" +
            "    PHONE_NUMBER TEXT NOT NULL\n" +
            ")"
    /*
    *
    *
    * CREATION OF THE OTHER TABLES THAT HAVE FOREIGN KEY
    *
    *
    */
    private final val TB_SUBCATEGORY = "CREATE TABLE SUBCATEGORY (\n" +
            "    ID_SUBCATEGORY INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    NSUBCATEGORY TEXT NOT NULL,\n" +
            "    CATEGORY_SUBCATEGORY INTEGER NOT NULL,\n" +
            "    FOREIGN KEY (CATEGORY_SUBCATEGORY) REFERENCES CATEGORY(ID_CATEGORY)\n" +
            ")"

    private final val TB_USER = "CREATE TABLE USER (\n" +
            "    ID_USER INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    NUSER TEXT NOT NULL,\n" +
            "    PSURNAME TEXT NOT NULL,\n" +
            "    MSURNAME TEXT NOT NULL,\n" +
            "    EMAIL TEXT NOT NULL UNIQUE,\n" +
            "    PASSWORD TEXT NOT NULL,\n" +
            "    DESCRIPTION TEXT,\n" +
            "    METHOD_USER INTEGER,\n" +
            "    TYPE_USER_USER INTEGER NOT NULL,\n" +
            "    INFORMATION_USER INTEGER UNIQUE,\n" +
            "    FOREIGN KEY (METHOD_USER) REFERENCES PAY_METHOD(ID_METHOD),\n" +
            "    FOREIGN KEY (TYPE_USER_USER) REFERENCES TYPE_USER(ID_TYPE),\n" +
            "    FOREIGN KEY (INFORMATION_USER) REFERENCES INFORMATION(ID_INFORMATION)\n" +
            ")"

    private final val TB_COURSE = "CREATE TABLE COURSE (\n" +
            "    ID_COURSE INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    NCOURSE TEXT NOT NULL,\n" +
            "    DESCRIPTION TEXT,\n" +
            "    PRICE INTEGER NOT NULL,\n" +
            "    TEACHER_COURSE INTEGER NOT NULL,\n" +
            "    SUBCATEGORY_COURSE INTEGER NOT NULL,\n" +
            "    FOREIGN KEY (TEACHER_COURSE) REFERENCES USER(ID_USER),\n" +
            "    FOREIGN KEY (SUBCATEGORY_COURSE) REFERENCES SUBCATEGORY(ID_SUBCATEGORY)\n" +
            ")"

    private final val TB_SECTION = "CREATE TABLE SECTION (\n" +
            "    ID_SECTION INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    NSECTION TEXT NOT NULL,\n" +
            "    COURSE_SECTION INTEGER NOT NULL,\n" +
            "    FOREIGN KEY (COURSE_SECTION) REFERENCES COURSE(ID_COURSE)\n" +
            ")"

    private final val TB_SHOPPING_CAR = "CREATE TABLE SHOPPING_CAR (\n" +
            "    USER_CAR INTEGER NOT NULL,\n" +
            "    COURSE_CAR INTEGER NOT NULL,\n" +
            "    STATUS TEXT NOT NULL,\n" +
            "    FOREIGN KEY (USER_CAR) REFERENCES USER(ID_USER),\n" +
            "    FOREIGN KEY (COURSE_CAR) REFERENCES COURSE(ID_COURSE)\n" +
            ")"

    //Array with the names of each one of the values
    private val tables: Array<String> = arrayOf("TB_CATEGORY",
        "TB_CONTENT", "TB_PAY_METHOD", "TB_TYPE_USER", "TB_INFORMATION",
        "TB_SUBCATEGORY", "TB_USER", "TB_COURSE", "TB_SECTION", "TB_SHOPPING_CAR")

    override fun onCreate(db: SQLiteDatabase?)
    {
        //This "for" is using to go to one to one of the values that have the query to create the tables
        if(db != null)
        {
            db.execSQL(TB_CATEGORY)
            db.execSQL(TB_CONTENT)
            db.execSQL(TB_PAY_METHOD)
            db.execSQL(TB_TYPE_USER)
            db.execSQL(TB_INFORMATION)
            db.execSQL(TB_SUBCATEGORY)
            db.execSQL(TB_USER)
            db.execSQL(TB_COURSE)
            db.execSQL(TB_SECTION)
            db.execSQL(TB_SHOPPING_CAR)
            //when the "for" is finished, we need to insert the first registers in the tables "type_user" and "user"
            db.execSQL("INSERT INTO TYPE_USER (NTYPE) VALUES('ADMIN')")
            db.execSQL("INSERT INTO TYPE_USER (NTYPE) VALUES('USER')")
            db.execSQL("INSERT INTO TYPE_USER (NTYPE) VALUES('TEACHER')")

            //To create the first user, its the admin
            db.execSQL("INSERT INTO USER (NUSER, PSURNAME, MSURNAME, EMAIL, PASSWORD, DESCRIPTION, TYPE_USER_USER) VALUES(" +
                    "'IAN DAVID'," +
                    "'GARCIA'," +
                    "'GARCIA'," +
                    "'garcia.iandavid117@hotmail.com'," +
                    "'12345678!'," +
                    "'ADMIN'," +
                    "1)")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        if(oldVersion == 1)
        {
            if (db != null)
            {
                db.execSQL("DROP TABLE SECTION")
                db.execSQL("DROP TABLE CONTENT")

                db.execSQL(TB_CONTENT)
                db.execSQL(TB_SECTION)
            }
        }
    }

}