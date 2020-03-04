package com.shogek.spinoza.db.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_table")
data class Contact(

    @ColumnInfo(name = "contact_name")
    /** The person's name. */
    var name: String?,

    @ColumnInfo(name = "contact_phone")
    /** The person's phone number. */
    val phone: String,

    @ColumnInfo(name = "contact_photo_uri")
    /** The person's profile picture. */
    var photoUri: String?
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "contact_contact_id")
    var contactId: Long = 0

    fun getDisplayName(): String = name ?: phone
}