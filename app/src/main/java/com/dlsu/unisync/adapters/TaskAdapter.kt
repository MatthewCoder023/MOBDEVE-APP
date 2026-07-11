package com.dlsu.unisync.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.databinding.ItemTaskBinding
import com.dlsu.unisync.models.TaskItem

// Renders the shared task list; list mutations go through TasksViewModel.
class TaskAdapter(
    private val tasks: List<TaskItem>
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskItem) {
            binding.taskCheckBox.setOnCheckedChangeListener(null)
            binding.taskCheckBox.isChecked = task.isDone
            binding.taskTitle.text = task.title
            binding.taskDue.text = task.due

            binding.taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked
            }
        }
    }
}
