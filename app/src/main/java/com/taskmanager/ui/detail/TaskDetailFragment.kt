package com.taskmanager.ui.detail

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taskmanager.R
import com.taskmanager.TaskManagerApp
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import com.taskmanager.data.model.Task
import com.taskmanager.databinding.FragmentTaskDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment untuk menampilkan detail tugas.
 */
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val args: TaskDetailFragmentArgs by navArgs()

    private val viewModel: TaskDetailViewModel by viewModels {
        TaskDetailViewModelFactory(
            (requireActivity().application as TaskManagerApp).repository,
            args.taskId
        )
    }

    private val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("id", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        observeTask()
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            val action = TaskDetailFragmentDirections
                .actionTaskDetailFragmentToTaskFormFragment(args.taskId)
            findNavController().navigate(action)
        }

        binding.btnHapus.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun observeTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    task?.let { displayTask(it) }
                }
            }
        }
    }

    private fun displayTask(task: Task) {
        binding.apply {
            tvJudul.text = task.judul
            tvDeskripsi.text = task.deskripsi.ifEmpty { "Tidak ada deskripsi" }
            tvMataKuliah.text = task.mataKuliah.ifEmpty { "-" }
            tvDeadline.text = task.deadline?.let { dateFormat.format(it) } ?: "Tidak ada deadline"
            tvTanggalDibuat.text = dateFormat.format(task.tanggalDibuat)
            tvTanggalDiubah.text = dateFormat.format(task.tanggalDiubah)

            // Prioritas
            chipPrioritas.text = task.prioritas.label
            val prioritasColor = when (task.prioritas) {
                Prioritas.RENDAH -> R.color.prioritas_rendah
                Prioritas.SEDANG -> R.color.prioritas_sedang
                Prioritas.TINGGI -> R.color.prioritas_tinggi
                Prioritas.URGENT -> R.color.prioritas_urgent
            }
            chipPrioritas.setChipBackgroundColorResource(prioritasColor)

            // Status
            chipStatus.text = task.status.label
            val statusColor = when (task.status) {
                StatusTugas.BELUM_DIKERJAKAN -> R.color.status_belum
                StatusTugas.SEDANG_DIKERJAKAN -> R.color.status_dikerjakan
                StatusTugas.SELESAI -> R.color.status_selesai
            }
            chipStatus.setChipBackgroundColorResource(statusColor)

            // Cek overdue
            val isOverdue = task.deadline != null &&
                task.deadline.before(Date()) &&
                task.status != StatusTugas.SELESAI
            if (isOverdue) {
                tvDeadline.setTextColor(ContextCompat.getColor(requireContext(), R.color.overdue_red))
                tvLabelOverdue.visibility = View.VISIBLE
            } else {
                tvDeadline.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                tvLabelOverdue.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Tugas")
            .setMessage("Apakah Anda yakin ingin menghapus tugas ini?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteTask {
                    Snackbar.make(binding.root, "Tugas berhasil dihapus", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
