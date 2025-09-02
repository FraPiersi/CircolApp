package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.circolapp.R
import com.example.circolapp.databinding.ItemUserSelectionBinding
import com.example.circolapp.model.User

class UserSelectionAdapter(private val onUserSelected: (User) -> Unit) :
    ListAdapter<User, UserSelectionAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
        holder.itemView.setOnClickListener {
            onUserSelected(user)
        }
    }

    class UserViewHolder(private val binding: ItemUserSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.textViewUserName.text = user.username  ?: "Utente sconosciuto"
            Glide.with(binding.imageViewUserProfile.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.account)
                .error(R.drawable.account)
                .circleCrop()
                .into(binding.imageViewUserProfile)
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}