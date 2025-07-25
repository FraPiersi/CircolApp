package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.adapter.FeedbackListAdapter
import com.example.circolapp.model.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedbackListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feedbackAdapter: FeedbackListAdapter
    private val feedbackList = mutableListOf<Feedback>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feedback_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFeedback)
        setupRecyclerView()
        loadFeedback()

        return view
    }

    private fun setupRecyclerView() {
        feedbackAdapter = FeedbackListAdapter(feedbackList) { feedback ->
            // Callback per quando si clicca su un feedback
            markFeedbackAsRead(feedback)
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
                    val feedback = document.toObject(Feedback::class.java)
                    feedbackList.add(feedback)
                }
                feedbackAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore nel caricamento feedback: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun markFeedbackAsRead(feedback: Feedback) {
        if (!feedback.letto) {
            FirebaseFirestore.getInstance()
                .collection("feedback")
                .document(feedback.id)
                .update("letto", true)
                .addOnSuccessListener {
                    // Aggiorna la lista locale
                    val index = feedbackList.indexOfFirst { it.id == feedback.id }
                    if (index != -1) {
                        feedbackList[index] = feedback.copy(letto = true)
                        feedbackAdapter.notifyItemChanged(index)
                    }
                }
        }
    }
}
