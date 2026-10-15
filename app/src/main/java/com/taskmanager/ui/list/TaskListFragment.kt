package com.taskmanager.ui.list

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taskmanager.R
import com.taskmanager.TaskManagerApp
import com.taskmanager.data.model.Prioritas
import com.taskmanager.data.model.StatusTugas
import com.taskmanager.data.model.Task
import com.taskmanager.databinding.FragmentTaskListBinding
import kotlinx.coroutines.launch

/**
 * Fragment untuk menampilkan daftar tugas.
 * Menyediakan fitur pencarian, filter, dan navigasi ke detail/form tugas.
 */
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by viewModels {
        TaskListViewModelFactory((requireActivity().application as TaskManagerApp).repository)
    }

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupFilterChips()
        setupMenu()
        observeData()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onItemClick = { task ->
                // Navigasi ke detail tugas
                val action = TaskListFragmentDirections
                    .actionTaskListFragmentToTaskDetailFragment(task.id)
                findNavController().navigate(action)
            },
            onStatusChange = { task, newStatus ->
                viewModel.updateTaskStatus(task, newStatus)
            },
            onDeleteClick = { task ->
                showDeleteConfirmation(task)
            }
        )

        binding.recyclerViewTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFab() {
        binding.fabTambahTugas.setOnClickListener {
            // Navigasi ke form tambah tugas baru (taskId = -1L)
            val action = TaskListFragmentDirections
                .actionTaskListFragmentToTaskFormFragment(-1L)
            findNavController().navigate(action)
        }
    }

    private fun setupFilterChips() {
        // Filter Prioritas
        binding.chipGroupPrioritas.setOnCheckedStateChangeListener { group, checkedIds ->
            val prioritas = when {
                checkedIds.isEmpty() -> null
                else -> {
                    val chip = group.findViewById<Chip>(checkedIds.first())
                    when (chip?.text.toString()) {
                        "Rendah" -> Prioritas.RENDAH
                        "Sedang" -> Prioritas.SEDANG
                        "Tinggi" -> Prioritas.TINGGI
                        "Urgent" -> Prioritas.URGENT
                        else -> null
                    }
                }
            }
            viewModel.setFilterPrioritas(prioritas)
        }

        // Filter Status
        binding.chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            val status = when {
                checkedIds.isEmpty() -> null
                else -> {
                    val chip = group.findViewById<Chip>(checkedIds.first())
                    when (chip?.text.toString()) {
                        "Belum" -> StatusTugas.BELUM_DIKERJAKAN
                        "Dikerjakan" -> StatusTugas.SEDANG_DIKERJAKAN
                        "Selesai" -> StatusTugas.SELESAI
                        else -> null
                    }
                }
            }
            viewModel.setFilterStatus(status)
        }

        // Tombol hapus filter
        binding.btnHapusFilter.setOnClickListener {
            viewModel.clearFilters()
            binding.chipGroupPrioritas.clearCheck()
            binding.chipGroupStatus.clearCheck()
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_task_list, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "Cari tugas..."
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.setSearchQuery(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredTasks.collect { tasks ->
                    taskAdapter.submitList(tasks)
                    // Tampilkan pesan kosong jika tidak ada tugas
                    binding.tvEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerViewTasks.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun showDeleteConfirmation(task: Task) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Tugas")
            .setMessage("Apakah Anda yakin ingin menghapus tugas \"${task.judul}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteTask(task)
                Snackbar.make(binding.root, "Tugas berhasil dihapus", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
