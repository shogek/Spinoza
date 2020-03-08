package com.shogek.spinoza.db.contact

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity


@Entity(tableName = "contact_table")
data class Contact(

    @PrimaryKey
    @ColumnInfo(name = "id")
    /**
     * The ID of the contact as it is stored in the phone (ContactsContract.CommonDataKinds.Phone._ID)
     * (This app does not create new contacts - it only mirrors the ones already on the phone)
     */
    val id: Long,

    @ColumnInfo(name = "name")
    /** The person's name. */
    var name: String?,

    @ColumnInfo(name = "phone")
    /** The person's phone number. */
    var phone: String,

    @ColumnInfo(name = "photo_uri")
    /** The person's profile picture. */
    var photoUri: String?
) {
    fun getDisplayTitle(): String = this.name ?: this.phone
}