package com.dlsu.unisync.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.databinding.ItemTaskBinding
import com.dlsu.unisync.models.TaskItem

// Renders the task list with DiffUtil-driven updates. Checkbox changes and row
// taps flow back through the callbacks into TasksViewModel/TasksFragment.
class TaskAdapter(
    private val onTaskToggled: (TaskItem, Boolean) -> Unit,
    private val onTaskClicked: (TaskItem) -> Unit
) : ListAdapter<TaskItem, TaskAdapter.TaskViewHolder>(TaskDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding, onTaskToggled, onTaskClicked)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        private val binding: ItemTaskBinding,
        private val onTaskToggled: (TaskItem, Boolean) -> Unit,
        private val onTaskClicked: (TaskItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskItem) {
            binding.taskCheckBox.setOnCheckedChangeListener(null)
            binding.taskCheckBox.isChecked = task.isDone
            binding.taskCheckBox.contentDescription = task.title
            binding.taskTitle.text = task.title
            binding.taskDue.text = task.due

            binding.taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onTaskToggled(task, isChecked)
            }
            binding.root.setOnClickListener { onTaskClicked(task) }
        }
    }

    private object TaskDiffCallback : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(oldItem: TaskItem, newItem: TaskItem) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TaskItem, newItem: TaskItem) = oldItem == newItem
    }
}
