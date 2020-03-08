package com.shogek.spinoza.ui.contacts.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.shogek.spinoza.R
import com.shogek.spinoza.db.contact.Contact

class ContactListAdapter(
    private val context: AppCompatActivity,
    private val viewModel: ContactListViewModel
) : RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {

    private var originalContacts = listOf<Contact>()
    private var filteredContacts = mutableListOf<Contact>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = this.filteredContacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = this.filteredContacts[position]
        holder.contactId = contact.id
        holder.contactName.text = contact.getDisplayTitle()

        Glide.with(holder.itemView)
             .load(contact.photoUri)
             .apply(RequestOptions().placeholder(R.drawable.unknown_contact))
             .into(holder.contactPhoto)
    }

    fun setContacts(contacts: List<Contact>) {
        this.originalContacts = contacts
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
            val filtered = this.originalContacts.filter { c -> (c.name ?: c.phone).toLowerCase().contains(lowerCasePhrase) }
            this.filteredContacts.addAll(filtered)
        }

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName: TextView = itemView.findViewById(R.id.tv_contactName)
        val contactPhoto: ImageView = itemView.findViewById(R.id.iv_contactImage)
        var contactId: Long = -1

        init {
            itemView.setOnClickListener { viewModel.returnPickedContact(context, contactId) }
        }
    }
}
