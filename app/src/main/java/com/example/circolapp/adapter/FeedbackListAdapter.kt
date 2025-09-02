package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.R
import com.example.circolapp.model.Feedback
import java.text.SimpleDateFormat
import java.util.*

class FeedbackListAdapter(
    private            feedback.timestamp?.let { timestamp ->
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                textData.text = formatter.format(timestamp.toDate())
            } ?: run {
                textData.text = "Data non disponibile"
            }

           
            indicatoreLetto.visibility = if (feedback.letto) View.GONE else View.VISIBLE

            // Cambia lo sfondo se non letto
            itemView.alpha = if (feedback.letto) 0.7f else 1.0f

            itemView.setOnClickListener {
                onFeedbackClick(feedback)
            }
        }
    }
}
