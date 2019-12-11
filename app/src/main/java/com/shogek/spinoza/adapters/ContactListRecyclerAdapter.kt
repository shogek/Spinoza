package com.shogek.spinoza.adapters

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.PARAM_PICK_CONTACT
import com.shogek.spinoza.R
import com.shogek.spinoza.models.Contact

class ContactListRecyclerAdapter(
    private val context: AppCompatActivity,
    private val contacts: MutableList<Contact>
) : RecyclerView.Adapter<ContactListRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = this.contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = this.contacts[position]
        holder.contactId = contact.id
        holder.contactName.text = contact.displayName

        if (contact.photoUri != null)
            holder.contactPhoto.setImageURI(Uri.parse(contact.photoUri))
        else
            holder.contactPhoto.setImageResource(R.drawable.ic_placeholder_face_24dp)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var contactId: String
        val contactName: TextView = itemView.findViewById(R.id.tv_contactName)
        val contactPhoto: ImageView = itemView.findViewById(R.id.iv_contactImage)

        init {
            // Return ID of selected 'Contact' record
            itemView.setOnClickListener {
                val returnIntent = Intent()
                returnIntent.putExtra(PARAM_PICK_CONTACT, contactId)
                context.setResult(Activity.RESULT_OK, returnIntent)
                context.finish()
            }
        }
    }
}
