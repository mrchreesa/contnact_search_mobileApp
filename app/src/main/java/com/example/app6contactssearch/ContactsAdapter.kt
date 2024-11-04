import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app6contactssearch.R

class ContactsAdapter(
    private var contacts: List<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.contactImage)
        val name: TextView = view.findViewById(R.id.contactName)
        val phone: TextView = view.findViewById(R.id.contactPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.phone.text = contact.phoneNumber
        
        try {
            if (contact.photoUri != null) {
                val resourceId = contact.photoUri.replace("R.drawable.", "")
                val drawableId = holder.itemView.context.resources.getIdentifier(
                    resourceId,
                    "drawable",
                    holder.itemView.context.packageName
                )
                if (drawableId != 0) {
                    Glide.with(holder.image)
                        .load(drawableId)
                        .circleCrop()
                        .into(holder.image)
                } else {
                    holder.image.setImageResource(R.drawable.ic_person_placeholder)
                }
            } else {
                holder.image.setImageResource(R.drawable.ic_person_placeholder)
            }
        } catch (e: Exception) {
            holder.image.setImageResource(R.drawable.ic_person_placeholder)
        }

        holder.itemView.setOnClickListener { onContactClick(contact) }
    }

    override fun getItemCount() = contacts.size

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
} 