package com.shogek.spinoza.db.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_table")
data class Contact(

    // TODO: [Refactor] Use '@ForeignKey'
    // TODO: [Refactor] Primary key should be internal contact id
    @ColumnInfo(name = "internal_contact_id")
    /**
     * The ID of the contact as it is stored in the phone (ContactsContract.CommonDataKinds.Phone._ID)
     * (This app does not create new contacts - it only mirrors the ones already on the phone)
     */
    val internalContactId: Long,

    @ColumnInfo(name = "contact_name")
    /** The person's name. */
    var name: String?,

    @ColumnInfo(name = "contact_phone")
    /** The person's phone number. */
    var phone: String,

    @ColumnInfo(name = "contact_photo_uri")
    /** The person's profile picture. */
    var photoUri: String?
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "contact_contact_id")
    var contactId: Long = 0

    fun getDisplayName(): String = name ?: phone
}