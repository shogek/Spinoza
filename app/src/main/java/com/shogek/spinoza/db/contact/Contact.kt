package com.shogek.spinoza.db.contact

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_table")
data class Contact(

    @ColumnInfo(name = "name")
    /** The person's name. */
    val name: String?,

    @ColumnInfo(name = "phone")
    /** The person's phone number. */
    val phone: String,

    @ColumnInfo(name = "photo")
    /** The person's profile picture. */
    val photoUri: String?
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    fun getDisplayName(): String = name ?: phone
}