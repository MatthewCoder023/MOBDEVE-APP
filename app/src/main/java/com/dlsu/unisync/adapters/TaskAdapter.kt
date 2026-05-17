package com.dlsu.unisync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.models.TaskItem

// Simple task adapter with local checkbox state for prototype demos.
class TaskAdapter(
    private val tasks: MutableList<TaskItem>
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    fun addTask(task: TaskItem) {
        tasks.add(0, task)
        notifyItemInserted(0)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.taskCheckBox)
        private val titleText: TextView = itemView.findViewById(R.id.taskTitle)
        private val dueText: TextView = itemView.findViewById(R.id.taskDue)

        fun bind(task: TaskItem) {
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = task.isDone
            titleText.text = task.title
            dueText.text = task.due

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked
            }
        }
    }
}
