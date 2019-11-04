package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.shogek.spinoza.utils.DateUtils
import java.io.FileNotFoundException
import java.io.IOException

object ContactRepository {
    // TODO: Find out what the fuck this does
    /**
     * Load a contact photo thumbnail and return it as a Bitmap,
     * resizing the image to the provided image dimensions as needed.
     * @return A thumbnail Bitmap, sized to the provided width and height.
     * Returns null if the thumbnail is not found.
     */
    fun loadContactPhotoThumbnail(resolver: ContentResolver, thumbUri: Uri): Bitmap? {
        // Creates an asset file descriptor for the thumbnail file.
        var afd: AssetFileDescriptor? = null
        // try-catch block for file not found
        try {
            /*
             * Retrieves an AssetFileDescriptor object for the thumbnail
             * URI
             * using ContentResolver.openAssetFileDescriptor
             */
            afd = resolver.openAssetFileDescriptor(thumbUri, "r")
            /*
             * Gets a file descriptor from the asset file descriptor.
             * This object can be used across processes.
             */
            return afd?.fileDescriptor?.let { fileDescriptor ->
                // Decode the photo file and return the result as a Bitmap
                // If the file descriptor is valid
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null)
            }
        } catch (e: FileNotFoundException) {
            /*
             * Handle file not found errors
             */
            return null
        } finally {
            // In all cases, close the asset file descriptor
            try {
                afd?.close()
            } catch (e: IOException) {
            }
        }
    }

    fun getAllContacts(resolver: ContentResolver) {
        val uri = ContactsContract.Contacts.CONTENT_URI

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,         /*      TEXT       ex.: Tomas Ziobakas                                          The display name for the contact.                                                                                                                                                                                                                                                                                                                                                                                                                                                                   */
            ContactsContract.Contacts.LOOKUP_KEY,           /*      TEXT?      ex.: 0r1-4F4541294D5B39452B293D294D                          An opaque value that contains hints on how to find the contact if its row id changed as a result of a sync or aggregation. */
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI   /*      TEXT       ex.: content://com.android.contacts/contacts/1/photo         [see below] */
            /*
                A URI that can be used to retrieve a thumbnail of the contact's photo.
                A photo can be referred to either by a URI (this field or PHOTO_URI) or by ID (see PHOTO_ID).
                If PHOTO_ID is not null, PHOTO_URI and PHOTO_THUMBNAIL_URI shall not be null (but not necessarily vice versa).
                If the content provider does not differentiate between full-size photos and thumbnail photos, PHOTO_THUMBNAIL_URI and PHOTO_URI can contain the same value, but either both shall be null or both not null.
            */
        )

        val selection = null
        val selectionArgs = null
        val sortOrder = null

        val cursor = resolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        if (cursor == null)
            return

        var text = "\n "
        while (cursor.moveToNext()) {
            for (column in cursor.columnNames) {
                var columnIndex = cursor.getColumnIndex(column)
                var columnName = column
                var columnValue = cursor.getString(columnIndex)

                text += "\n"
                text += "INDEX: $columnIndex \t"
                text += "NAME:  $columnName  \t"
                if (columnName == "date_sent") {
                    var timestamp = DateUtils.getDateTime(columnValue)
                    text += "VALUE: $timestamp"
                } else {
                    text += "VALUE: $columnValue"
                }

                if (columnName == ContactsContract.Contacts.PHOTO_THUMBNAIL_URI) {
                    this.loadContactPhotoThumbnail(resolver, Uri.parse(columnValue))
                }
            }
        }
        Log.w("1", text)

        cursor.close()
    }
}