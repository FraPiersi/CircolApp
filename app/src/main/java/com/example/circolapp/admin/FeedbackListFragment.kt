package com.example.circolapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.R
import com.example.circolapp.adapter.FeedbackListAdapter
import com.example.circolapp.model.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedbackListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feedbackAdapter: FeedbackListAdapter
    private            markFeedbackAsRead(feedback)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedbackAdapter
        }
    }

    private fun loadFeedback() {
        FirebaseFirestore.getInstance()
            .collection("feedback")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                feedbackList.clear()
                for (document in documents) {
                    val index = feedbackList.indexOfFirst { it.id == feedback.id }
                    if (index != -1) {
                        feedbackList[index] = feedback.copy(letto = true)
                        feedbackAdapter.notifyItemChanged(index)
                    }
                }
        }
    }
}