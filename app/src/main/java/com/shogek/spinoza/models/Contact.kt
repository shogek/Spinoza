package com.shogek.spinoza.models

class Contact(
    val id: String,
    val displayName: String,
    /*
        TODO: [Refactor] Represents the original phone number extracted from a received message
        Example: "+3775478741". Whereas the field "number" has the clean version: "+377 547 8741"
    */
    val strippedPhone: String,
    val number: String,
    val numberE164: String?,
    var photoUri: String?
) {
}