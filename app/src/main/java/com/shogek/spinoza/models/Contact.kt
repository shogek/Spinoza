package com.shogek.spinoza.models

class Contact(
    val id: String,
    val displayName: String,
    val number: String,
    val numberE164: String?,
    var photoUri: String?
) {
}