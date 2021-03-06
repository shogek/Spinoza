package com.shogek.spinoza.ui.contacts.forward

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shogek.spinoza.R
import com.shogek.spinoza.db.contact.Contact

class ContactListForwardAdapter(
    private val context: AppCompatActivity,
    private val onClickSend: (Contact) -> Unit
) : RecyclerView.Adapter<ContactListForwardAdapter.ViewHolder>() {

    private var originalContacts: List<Contact> = emptyList()
    private var filteredContacts: MutableList<Contact> = this.originalContacts.toMutableList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.contact_list_forward_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = this.filteredContacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = this.filteredContacts[position]

        holder.contact = contact
        holder.contactName.text = contact.getDisplayTitle()

        Glide.with(holder.itemView)
            .load(contact.photoUri)
            .placeholder(R.drawable.unknown_contact)
            .into(holder.contactPhoto)
    }

    fun setContacts(
        contacts: List<Contact>
    ) {
        this.originalContacts = contacts.toMutableList()
        this.filteredContacts = contacts.toMutableList()
        notifyDataSetChanged()
    }

    fun filter(phrase: String) {
        this.filteredContacts.clear()

        if (phrase.isEmpty()) {
            // No filter - show all contacts
            this.filteredContacts.addAll(this.originalContacts)
        } else {
            // Apply filter
            val lowerCasePhrase = phrase.toLowerCase()
            val filtered = this.originalContacts.filter { c -> c.getDisplayTitle().toLowerCase().contains(lowerCasePhrase) }
            this.filteredContacts.addAll(filtered)
        }

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var contact: Contact
        private val forwardButton: TextView = itemView.findViewById(R.id.btn_forwardMessage)
        val contactName: TextView = itemView.findViewById(R.id.tv_contactName)
        val contactPhoto: ImageView = itemView.findViewById(R.id.iv_contactImage)

        init {
            forwardButton.setOnClickListener {
                // TODO: [Bug] Change button only after confirming message was sent
                onClickSend(this.contact)
                it.setBackgroundResource(R.color.colorWhite)
                it.findViewById<TextView>(R.id.btn_forwardMessage).text = "SENT"
            }
        }
    }
}