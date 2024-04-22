package com.example.guvenlipati

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.guvenlipati.advert.ActiveAdvertFragment
import com.example.guvenlipati.chat.ChatActivity
import com.example.guvenlipati.chat.ProfileActivity
import com.example.guvenlipati.models.Backer
import com.example.guvenlipati.models.Job
import com.example.guvenlipati.models.Offer
import com.example.guvenlipati.models.Pet
import com.example.guvenlipati.models.User
import com.example.guvenlipati.payment.PaymentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime

class ActiveJobAdapter(
    private val context: Context,
    private val jobList: List<Job>,
    private val petList: List<Pet>,
    private val userList: List<User>,
    private val offerList: ArrayList<Offer>
) : RecyclerView.Adapter<ActiveJobAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_active_job, parent, false)


        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActiveJobAdapter.ViewHolder, position: Int) {
        if (position < jobList.size && position < petList.size && position < userList.size && position < offerList.size) {
            holder.bind(
                jobList[position],
                petList[position],
                userList[position],
                offerList[position]
            )
        }
    }

    override fun getItemCount(): Int = offerList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val petPhotoImageView = view.findViewById<ImageView>(R.id.petPhotoImageView)
        private val petNameTextView = view.findViewById<TextView>(R.id.petNameTextView)
        private val jobTypeTextView = view.findViewById<TextView>(R.id.jobTypeTextView)
        private val petTypeTextView = view.findViewById<TextView>(R.id.petTypeTextView)
        private val startDateTextView = view.findViewById<TextView>(R.id.startDateTextView)
        private val endDateTextView = view.findViewById<TextView>(R.id.endDateTextView)
        private val locationTextView = view.findViewById<TextView>(R.id.locationTextView)
        private val backerPhotoImageView =
            view.findViewById<ImageView>(R.id.backerPhotoImageView)
        private val backerNameTextView = view.findViewById<TextView>(R.id.backerNameTextView)
        private val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
        private val buttonGoChat = view.findViewById<ImageButton>(R.id.buttonGoChat)
        private val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        private val confirmStatusTextView = view.findViewById<TextView>(R.id.confirmStatusTextView)
        private val buttonCopyId = view.findViewById<ImageButton>(R.id.buttonCopyId)
        private val supportButton = view.findViewById<Button>(R.id.supportButton)

        fun bind(job: Job, pet: Pet, user: User, offer: Offer) {
            when (job.jobType) {
                "feedingJob" -> jobTypeTextView.text = "Besleme"
                "walkingJob" -> jobTypeTextView.text = "Gezdirme"
                "homeJob" -> jobTypeTextView.text = "Evde Bakım"
            }
            petNameTextView.text = pet.petName
            petTypeTextView.text = pet.petBreed
            startDateTextView.text = job.jobStartDate
            endDateTextView.text = job.jobEndDate
            locationTextView.text = job.jobProvince + ", " + job.jobTown
            backerNameTextView.text = user.userName + " " + user.userSurname
            priceTextView.text = offer.offerPrice.toString() + " TL"

            if (offer.confirmBacker==false && offer.confirmUser==true){
                confirmStatusTextView.text="İş Sahibi\nOnayladı..."
            }
            else if (offer.confirmBacker==true && offer.confirmUser==false){
                confirmStatusTextView.text="Sen\nOnayladın..."
            }
            else{
                confirmStatusTextView.text="Henüz\nOnaylanmadı..."
            }

            Glide.with(context)
                .load(pet.petPhoto)
                .placeholder(R.drawable.default_pet_image_2)
                .into(petPhotoImageView)

            Glide.with(context)
                .load(user.userPhoto)
                .placeholder(R.drawable.default_pet_image_2)
                .into(backerPhotoImageView)

            buttonGoChat.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("userId", offer.offerUser)
                context.startActivity(intent)
            }

            confirmButton.setOnClickListener {
                val databaseReference =
                    FirebaseDatabase.getInstance().getReference("offers").child(offer.offerId)
                if (offer.confirmUser == true) {
                    databaseReference.updateChildren(
                        mapOf(
                            "confirmBacker" to true,
                            "offerStatus" to true
                        )
                    )
                } else {
                    databaseReference.updateChildren(
                        mapOf(
                            "confirmBacker" to true
                        )
                    )
                }
            }

            supportButton.setOnClickListener{
                val intent=Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto", "yunusemre-kurt@outlook.com",  null))
                context.startActivity(Intent.createChooser(intent,"Send email..."))
            }

            buttonCopyId.setOnClickListener{
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("simple text", offer.offerId)
                clipboard.setPrimaryClip(clip)
                showToast("Teklif ID kopyalandı...")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}