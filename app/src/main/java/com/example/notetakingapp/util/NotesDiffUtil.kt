package com.example.notetakingapp.util

import androidx.recyclerview.widget.DiffUtil
import com.example.notetakingapp.models.Note

class NotesDiffUtil(
        private val oldNotesList: List<Note>,
        private val newNotesList: List<Note>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldNotesList.size

    override fun getNewListSize(): Int = newNotesList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldNotesList[oldItemPosition].id == newNotesList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldNotesList[oldItemPosition].equals(newNotesList[newItemPosition])
    }

}