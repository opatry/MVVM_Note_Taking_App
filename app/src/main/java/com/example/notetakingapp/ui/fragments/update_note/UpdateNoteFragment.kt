package com.example.notetakingapp.ui.fragments.update_note

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.FragmentUpdateNoteBinding
import com.example.notetakingapp.models.Note
import com.example.notetakingapp.models.Priority
import com.example.notetakingapp.ui.viewmodels.NoteEditorViewModel
import com.example.notetakingapp.ui.viewmodels.NotesViewModel
import com.example.notetakingapp.util.TimeUtil
import com.google.android.material.chip.Chip
import com.myscript.iink.Editor
import com.myscript.iink.uireferenceimplementation.ImageLoader
import com.myscript.iink.uireferenceimplementation.InputController
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class UpdateNoteFragment : Fragment() {

    private val notesViewModel: NotesViewModel by viewModels()
    private val noteEditorViewModel: NoteEditorViewModel by viewModels()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteEditorViewModel.openEditor(view.resources.displayMetrics, binding.editorView)

        noteEditorViewModel.iinkEditor.observe(viewLifecycleOwner) { iinkEditor ->
            with(binding) {
                editorView.isVisible = iinkEditor != null
                if (iinkEditor != null) {
                    editorView.doOnLayout {
                        if (!iinkEditor.isClosed) {
                            bindEditor(iinkEditor)
                        }
                    }
                } else {
                    unbindEditor()
                }
            }
        }
    }

    private fun bindEditor(editor: Editor) {
        with(binding) {
            val displayMetrics = editorView.resources.displayMetrics
            editor.configuration?.let { conf ->
                val verticalMarginPx = 100// TODO resources.getDimension(R.dimen.vertical_margin)
                val horizontalMarginPx = 30// TODO resources.getDimension(R.dimen.horizontal_margin)
                val verticalMarginMM = 25.4f * verticalMarginPx / displayMetrics.ydpi
                val horizontalMarginMM = 25.4f * horizontalMarginPx / displayMetrics.xdpi
                conf.setNumber("text.margin.top", verticalMarginMM)
                conf.setNumber("text.margin.left", horizontalMarginMM)
                conf.setNumber("text.margin.right", horizontalMarginMM)
                conf.setNumber("math.margin.top", verticalMarginMM)
                conf.setNumber("math.margin.bottom", verticalMarginMM)
                conf.setNumber("math.margin.left", horizontalMarginMM)
                conf.setNumber("math.margin.right", horizontalMarginMM)
            }

            //iinkEditor.theme = stylesheet(editorView.context)
            editorView.imageLoader = ImageLoader(editor)
            // needs to be done prior to setEditor
            editorView.typefaces = emptyMap()
            val inputController = InputController(editorView.context, editorView, editor).apply {
                inputMode = InputController.INPUT_MODE_AUTO
            }
            editorView.editor = editor
            editorView.setOnTouchListener(inputController)
            editorView.requestFocus()
        }
    }

    private fun unbindEditor() {
        binding.editorView.let {
            it.setOnTouchListener(null)
            it.imageLoader = null
            it.editor = null
            it.clearFocus()
        }
    }

    private fun setUpdateViewsFromArgs() {
        binding.updateTitleEt.setText(args.currentNote.title)
        binding.updateContentEt.setText(args.currentNote.content)

        noteEditorViewModel.openNote(args.currentNote)

        when (args.currentNote.priority) {
            Priority.LOW -> {
                binding.updateChipGroup.check(binding.updateChipGroup[0].id)
            }
            Priority.MEDIUM -> {
                binding.updateChipGroup.check(binding.updateChipGroup[1].id)
            }
            Priority.HIGH -> {
                binding.updateChipGroup.check(binding.updateChipGroup[2].id)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_note_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_update_note -> {
                updateNote()
            }
            R.id.menu_delete_note -> {
                deleteNote()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteNote() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setIcon(R.drawable.ic_delete_forever)
        builder.setTitle("Delete this note?")
        builder.setMessage("Are you sure you want to remove this note? This cannot be undone.")
        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            notesViewModel.deleteNote(args.currentNote)
            findNavController().navigate(R.id.action_updateNoteFragment_to_notesListFragment)
            Toast.makeText(requireContext(), "Successfully deleted note!", Toast.LENGTH_SHORT)
                    .show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.create().show()

    }

    private fun updateNote() {
        val titleText = binding.updateTitleEt.text.toString()
//        val contentText = binding.updateContentEt.text.toString()
        lifecycleScope.launchWhenCreated {
            val contentText = noteEditorViewModel.exportAsText()
            noteEditorViewModel.closeNote()
            if (notesViewModel.noteIsValid(titleText, contentText)) {
                val note = Note(
                        args.currentNote.id,
                        titleText,
                        contentText,
                        TimeUtil.getCurrentTime(),
                        getChipPriority()
                )
                notesViewModel.updateNote(note)
                Toast.makeText(context, "Successfully updated note", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_updateNoteFragment_to_notesListFragment)
            } else {
                Toast.makeText(context, "Please fill in the fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getChipPriority(): Priority {
        val chipsCount = binding.updateChipGroup.childCount
        var i = 0
        while (i < chipsCount) {
            val chip = binding.updateChipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                return Priority.valueOf(chip.text.toString().toUpperCase(Locale.getDefault()))
            }
            i++
        }
        return Priority.LOW
    }

    override fun onDestroyView() {
        super.onDestroyView()
        noteEditorViewModel.closeEditor()
        _binding = null
    }

}