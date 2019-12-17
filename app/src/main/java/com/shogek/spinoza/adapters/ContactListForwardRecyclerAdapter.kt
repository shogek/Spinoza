package com.shogek.spinoza.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.R
import com.shogek.spinoza.cores.ContactListForwardCore
import com.shogek.spinoza.models.Contact

class ContactListForwardRecyclerAdapter(
    private val context: AppCompatActivity,
    private val core: ContactListForwardCore,
    private val contacts: Array<Contact>
) : RecyclerView.Adapter<ContactListForwardRecyclerAdapter.ViewHolder>() {

    private val filteredContacts: MutableList<Contact> = mutableListOf<Contact>().apply { addAll(contacts) } // copy the list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.contact_list_forward_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = this.filteredContacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = this.filteredContacts[position]
        holder.contactId = contact.id
        holder.contactName.text = contact.displayName

        if (contact.photoUri != null)
            holder.contactPhoto.setImageURI(Uri.parse(contact.photoUri))
        else
            holder.contactPhoto.setImageResource(R.drawable.ic_placeholder_face_24dp)
    }

    fun filter(phrase: String) {
        this.filteredContacts.clear()

        if (phrase.isEmpty()) {
            // No filter - show all contacts
            this.filteredContacts.addAll(this.contacts)
        } else {
            // Apply filter
            val lowerCasePhrase = phrase.toLowerCase()
            val filtered = this.contacts.filter { c -> c.displayName.toLowerCase().contains(lowerCasePhrase) }
            this.filteredContacts.addAll(filtered)
        }

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var contactId: String
        private val forwardButton: TextView = itemView.findViewById(R.id.btn_forwardMessage)
        val contactName: TextView = itemView.findViewById(R.id.tv_contactName)
        val contactPhoto: ImageView = itemView.findViewById(R.id.iv_contactImage)

        init {
            forwardButton.setOnClickListener {
                // TODO: [Bug] Change button only after confirming message was sent
                core.onClickForwardMessage(this.contactId)
                it.setBackgroundResource(R.color.colorWhite)
                it.findViewById<TextView>(R.id.btn_forwardMessage).text = "SENT"
            }
        }
    }
}