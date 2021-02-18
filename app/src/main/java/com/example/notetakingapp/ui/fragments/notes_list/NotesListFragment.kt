package com.example.notetakingapp.ui.fragments.notes_list

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.FragmentNotesListBinding
import com.example.notetakingapp.ui.adapters.NotesAdapter
import com.example.notetakingapp.ui.viewmodels.NotesViewModel

private lateinit var notesAdapter: NotesAdapter

class NotesListFragment : Fragment() {

    private val notesViewModel: NotesViewModel by viewModels()

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_notesListFragment_to_addNoteFragment)
        }

        notesViewModel.allNotes.observe(viewLifecycleOwner, Observer { notesList ->
            showEmptyNotesView(notesList.isEmpty())
            notesAdapter.setNotesList(notesList)
        })

        setHasOptionsMenu(true)

        return view
    }

    private fun showEmptyNotesView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.lottieAnimationView.visibility = View.VISIBLE
            binding.emptyNotesTv.visibility = View.VISIBLE
        } else {
            binding.lottieAnimationView.visibility = View.INVISIBLE
            binding.emptyNotesTv.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> {
            }
            R.id.menu_delete_all -> {
                deleteAllNotes()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllNotes() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            notesViewModel.deleteAllNotes()
            Toast.makeText(requireContext(), "Successfully delete everything!", Toast.LENGTH_SHORT)
                .show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to remove everything? This cannot be undone.")
        builder.create().show()
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter()
        binding.notesListRv.apply {
            adapter = notesAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
