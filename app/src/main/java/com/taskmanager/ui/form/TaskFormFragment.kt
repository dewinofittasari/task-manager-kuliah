package com.taskmanager.ui.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.taskmanager.R
import com.taskmanager.TaskManagerApp
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import com.taskmanager.data.model.Task
import com.taskmanager.databinding.FragmentTaskFormBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment form untuk menambah atau mengedit tugas.
 */
class TaskFormFragment : Fragment() {

    private var _binding: FragmentTaskFormBinding? = null
    private val binding get() = _binding!!

    private val args: TaskFormFragmentArgs by navArgs()

    private val viewModel: TaskFormViewModel by viewModels {
        TaskFormViewModelFactory(
            (requireActivity().application as TaskManagerApp).repository,
            args.taskId
        )
    }

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    private var selectedDeadline: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupDeadlinePicker()
        setupSaveButton()

        if (viewModel.isEditMode) {
            observeExistingTask()
        }
    }

    private fun setupDropdowns() {
        // Dropdown Prioritas
        val prioritasItems = Prioritas.entries.map { it.label }
        val prioritasAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            prioritasItems
        )
        binding.dropdownPrioritas.setAdapter(prioritasAdapter)
        binding.dropdownPrioritas.setText(Prioritas.SEDANG.label, false)

        // Dropdown Status
        val statusItems = StatusTugas.entries.map { it.label }
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            statusItems
        )
        binding.dropdownStatus.setAdapter(statusAdapter)
        binding.dropdownStatus.setText(StatusTugas.BELUM_DIKERJAKAN.label, false)
    }

    private fun setupDeadlinePicker() {
        binding.btnPilihDeadline.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnHapusDeadline.setOnClickListener {
            selectedDeadline = null
            binding.tvDeadlinePreview.text = "Belum dipilih"
            binding.btnHapusDeadline.visibility = View.GONE
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        selectedDeadline?.let { calendar.time = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                // Setelah pilih tanggal, pilih waktu
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        selectedDeadline = calendar.time
                        binding.tvDeadlinePreview.text = dateFormat.format(calendar.time)
                        binding.btnHapusDeadline.visibility = View.VISIBLE
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupSaveButton() {
        binding.btnSimpan.setOnClickListener {
            val judul = binding.etJudul.text.toString().trim()
            val deskripsi = binding.etDeskripsi.text.toString().trim()
            val mataKuliah = binding.etMataKuliah.text.toString().trim()

            // Validasi
            if (judul.isEmpty()) {
                binding.tilJudul.error = "Judul tugas tidak boleh kosong"
                return@setOnClickListener
            }
            binding.tilJudul.error = null

            val prioritas = Prioritas.entries.find {
                it.label == binding.dropdownPrioritas.text.toString()
            } ?: Prioritas.SEDANG

            val status = StatusTugas.entries.find {
                it.label == binding.dropdownStatus.text.toString()
            } ?: StatusTugas.BELUM_DIKERJAKAN

            viewModel.saveTask(
                judul = judul,
                deskripsi = deskripsi,
                mataKuliah = mataKuliah,
                deadline = selectedDeadline,
                prioritas = prioritas,
                status = status
            ) {
                val message = if (viewModel.isEditMode) "Tugas berhasil diperbarui" else "Tugas berhasil ditambahkan"
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun observeExistingTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.existingTask.collect { task ->
                    task?.let { populateForm(it) }
                }
            }
        }
    }

    private fun populateForm(task: Task) {
        binding.apply {
            etJudul.setText(task.judul)
            etDeskripsi.setText(task.deskripsi)
            etMataKuliah.setText(task.mataKuliah)
            dropdownPrioritas.setText(task.prioritas.label, false)
            dropdownStatus.setText(task.status.label, false)

            task.deadline?.let {
                selectedDeadline = it
                tvDeadlinePreview.text = dateFormat.format(it)
                btnHapusDeadline.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
