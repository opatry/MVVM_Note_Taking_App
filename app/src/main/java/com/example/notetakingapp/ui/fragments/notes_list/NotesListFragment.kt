package com.example.notetakingapp.ui.fragments.notes_list

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.FragmentNotesListBinding
import com.example.notetakingapp.models.Note
import com.example.notetakingapp.models.SortBy
import com.example.notetakingapp.models.SortBy.*
import com.example.notetakingapp.ui.adapters.NotesAdapter
import com.example.notetakingapp.ui.viewmodels.NotesViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private lateinit var notesAdapter: NotesAdapter

@AndroidEntryPoint
class NotesListFragment : Fragment() {

    private val notesViewModel: NotesViewModel by viewModels()

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        hideKeyboard()

        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_notesListFragment_to_addNoteFragment)
        }

        notesViewModel.allNotes.observe(viewLifecycleOwner, Observer { notesList ->
            showEmptyNotesView(notesList.isEmpty())
            setNotesList(notesList = notesList)
            setNotesListAll(notesListAll = notesList) // In order to filter the list, need a copy of the original list!
            if (notesViewModel.readFromDataStore.value != null) {
                sortNotesList(SortBy.valueOf(notesViewModel.readFromDataStore.value!!))
            }
        })

        notesViewModel.readFromDataStore.observe(viewLifecycleOwner, Observer { sortBy ->
            sortNotesList(valueOf(sortBy))
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_list_menu, menu)
        val search = menu.findItem(R.id.menu_search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = "Search Notes..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query!!.isNotEmpty()) {
                    filterNotes(query.toLowerCase(Locale.getDefault()))
                } else {
                    setNotesList(notesAdapter.listOfNotesAll)
                }
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_date_newest -> {
                sortNotesList(DATE_NEWEST)
                notesViewModel.saveToDataStore(DATE_NEWEST.toString())
            }
            R.id.menu_date_oldest -> {
                sortNotesList(DATE_OLDEST)
                notesViewModel.saveToDataStore(DATE_OLDEST.toString())
            }
            R.id.menu_priority_high -> {
                sortNotesList(PRIORITY_HIGHEST)
                notesViewModel.saveToDataStore(PRIORITY_HIGHEST.toString())
            }
            R.id.menu_priority_low -> {
                sortNotesList(PRIORITY_LOWEST)
                notesViewModel.saveToDataStore(PRIORITY_LOWEST.toString())
            }
            R.id.menu_delete_all -> {
                deleteAllNotes()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sortNotesList(sortBy: SortBy) {
        var notes = notesAdapter.listOfNotes
        notes = when (sortBy) {
            DATE_NEWEST -> notes.sortedByDescending { it.date }
            DATE_OLDEST -> notes.sortedBy { it.date }
            PRIORITY_HIGHEST -> notes.sortedWith(compareBy<Note> { it.priority }.thenBy { it.date }).reversed()
            PRIORITY_LOWEST -> notes.sortedWith(compareBy<Note> { it.priority }.thenByDescending { it.date })
        }
        setNotesList(notes)
        setNotesListAll(notes)
    }

    private fun filterNotes(query: String) {
        val allNotes = notesAdapter.listOfNotesAll
        val filteredNotes = mutableListOf<Note>()
        allNotes.forEach { note ->
            if (note.title.toLowerCase(Locale.getDefault()).contains(query) ||
                    note.content.toLowerCase(Locale.getDefault()).contains(query)
            ) {
                filteredNotes.add(note)
            }
        }
        setNotesList(filteredNotes)
    }

    private fun setNotesList(notesList: List<Note>) {
        notesAdapter.setNotesList(notesList)
    }

    private fun setNotesListAll(notesListAll: List<Note>) {
        notesAdapter.setNotesListAll(notesListAll)
    }

    private fun deleteAllNotes() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setIcon(R.drawable.ic_delete_forever)
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to delete everything? This cannot be undone.")
        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            notesViewModel.deleteAllNotes()
            Toast.makeText(requireContext(), "Successfully deleted everything!", Toast.LENGTH_SHORT)
                    .show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.create().show()
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter()
        binding.notesListRv.apply {
            adapter = notesAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            swipeToDelete(this)
            this.itemAnimator = jp.wasabeef.recyclerview.animators.ScaleInAnimator().apply {
                addDuration = 250
                removeDuration = 250
                moveDuration = 250
                changeDuration = 250
            }
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback =
                object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    override fun onMove(
                            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean {
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val noteToDelete = notesAdapter.listOfNotes[viewHolder.adapterPosition]
                        notesViewModel.deleteNote(noteToDelete)
                        showUndoDeleteSnackbar(viewHolder.itemView, noteToDelete)
                    }
                }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showUndoDeleteSnackbar(itemView: View, noteToDelete: Note) {
        val snackbar = Snackbar.make(itemView, "Delete note!", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo") {
            notesViewModel.insertNote(noteToDelete)
        }.show()
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

    private fun hideKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
