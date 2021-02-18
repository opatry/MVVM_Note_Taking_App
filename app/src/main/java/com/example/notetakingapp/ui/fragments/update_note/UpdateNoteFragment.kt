package com.example.notetakingapp.ui.fragments.update_note

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.FragmentUpdateNoteBinding
import com.example.notetakingapp.models.Note
import com.example.notetakingapp.models.Priority
import com.example.notetakingapp.ui.viewmodels.NotesViewModel
import com.example.notetakingapp.util.TimeUtil

class UpdateNoteFragment : Fragment() {

    private val notesViewModel: NotesViewModel by viewModels()

    private var _binding: FragmentUpdateNoteBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<UpdateNoteFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateNoteBinding.inflate(inflater, container, false)
        val view = binding.root

        setHasOptionsMenu(true)

        setUpdateViewsFromArgs()

        return view
    }

    private fun setUpdateViewsFromArgs() {
        binding.updateTitleEt.setText(args.currentNote.title)
        binding.updateContentEt.setText(args.currentNote.content)
        // TODO - Chip Group
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_note_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_update_note) {
            updateNote()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateNote() {
        val titleText = binding.updateTitleEt.text.toString()
        val contentText = binding.updateContentEt.text.toString()
        if (notesViewModel.noteIsValid(titleText, contentText)) {
            val note = Note(
                args.currentNote.id,
                titleText,
                contentText,
                TimeUtil.getCurrentTime(),
                Priority.HIGH // #TODO
            )
            notesViewModel.updateNote(note)
            Toast.makeText(context, "Successfully updated note", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateNoteFragment_to_notesListFragment)
        } else {
            Toast.makeText(context, "Please fill in the fields!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}