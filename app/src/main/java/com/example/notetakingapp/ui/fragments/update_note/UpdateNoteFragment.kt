package com.example.notetakingapp.ui.fragments.update_note

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.FragmentUpdateNoteBinding

class UpdateNoteFragment : Fragment() {

    private var _binding: FragmentUpdateNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateNoteBinding.inflate(inflater, container, false)
        val view = binding.root

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_note_menu, menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}