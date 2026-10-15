package com.taskmanager.ui.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskmanager.R
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import com.taskmanager.data.model.Task
import com.taskmanager.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter RecyclerView untuk menampilkan daftar tugas.
 * Menggunakan ListAdapter dengan DiffUtil untuk performa optimal.
 */
class TaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onStatusChange: (Task, StatusTugas) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                // Judul tugas
                tvJudul.text = task.judul
                if (task.status == StatusTugas.SELESAI) {
                    tvJudul.paintFlags = tvJudul.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    tvJudul.paintFlags = tvJudul.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                // Mata kuliah
                tvMataKuliah.text = task.mataKuliah.ifEmpty { "-" }

                // Deadline
                tvDeadline.text = task.deadline?.let { dateFormat.format(it) } ?: "Tidak ada deadline"

                // Cek apakah sudah melewati deadline
                val isOverdue = task.deadline != null &&
                    task.deadline.before(Date()) &&
                    task.status != StatusTugas.SELESAI
                if (isOverdue) {
                    tvDeadline.setTextColor(ContextCompat.getColor(root.context, R.color.overdue_red))
                } else {
                    tvDeadline.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary))
                }

                // Prioritas badge
                chipPrioritas.text = task.prioritas.label
                val prioritasColor = when (task.prioritas) {
                    Prioritas.RENDAH -> R.color.prioritas_rendah
                    Prioritas.SEDANG -> R.color.prioritas_sedang
                    Prioritas.TINGGI -> R.color.prioritas_tinggi
                    Prioritas.URGENT -> R.color.prioritas_urgent
                }
                chipPrioritas.setChipBackgroundColorResource(prioritasColor)

                // Status badge
                chipStatus.text = task.status.label
                val statusColor = when (task.status) {
                    StatusTugas.BELUM_DIKERJAKAN -> R.color.status_belum
                    StatusTugas.SEDANG_DIKERJAKAN -> R.color.status_dikerjakan
                    StatusTugas.SELESAI -> R.color.status_selesai
                }
                chipStatus.setChipBackgroundColorResource(statusColor)

                // Checkbox status selesai
                checkboxSelesai.isChecked = task.status == StatusTugas.SELESAI
                checkboxSelesai.setOnClickListener {
                    val newStatus = if (checkboxSelesai.isChecked) {
                        StatusTugas.SELESAI
                    } else {
                        StatusTugas.BELUM_DIKERJAKAN
                    }
                    onStatusChange(task, newStatus)
                }

                // Tombol hapus
                btnHapus.setOnClickListener {
                    onDeleteClick(task)
                }

                // Klik item untuk melihat detail
                root.setOnClickListener {
                    onItemClick(task)
                }
            }
        }
    }
}

/**
 * DiffUtil callback untuk membandingkan item tugas secara efisien.
 */
class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}
