package com.shogek.spinoza.db.contact

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity


@Entity(tableName = "contact_table")
data class Contact(

    @ColumnInfo(name = "android_id")
    /**
     * The ID of the contact as it is stored in the phone (ContactsContract.CommonDataKinds.Phone._ID)
     * (indicates that the record was not created by our application, but imported from the phone)
     */
    val androidId: Long,

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

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    fun getDisplayTitle(): String = this.name ?: this.phone
}