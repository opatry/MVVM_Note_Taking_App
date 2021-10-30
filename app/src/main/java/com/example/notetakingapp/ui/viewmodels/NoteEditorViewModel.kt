package com.example.notetakingapp.ui.viewmodels

import android.util.DisplayMetrics
import androidx.lifecycle.*
import com.example.notetakingapp.models.Note
import com.myscript.iink.*
import com.myscript.iink.uireferenceimplementation.FontMetricsProvider
import com.myscript.util.IAutoCloseable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// duplicated AutoCloseable.kt impl from Kotlin stdlib for MyScript IAutoCloseable (before Java 1.8 compat)

/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * In case if the resource is being closed due to an exception occurred in [block], and the closing also fails with an exception,
 * the latter is added to the [suppressed][java.lang.Throwable.addSuppressed] exceptions of the former.
 *
 * @param block a function to process this [AutoCloseable] resource.
 * @return the result of [block] function invoked on this resource.
 */
public inline fun <T : IAutoCloseable?, R> T.use(block: (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            this == null -> {
            }
            exception == null -> close()
            else ->
                try {
                    close()
                } catch (closeException: Throwable) {
                    exception.addSuppressed(closeException)
                }
        }
    }
}

@HiltViewModel
class NoteEditorViewModel
@Inject constructor(
    private val rootDir: File,
    private val engine: Engine
) : ViewModel() {

    private var autoSaveTasks = mutableMapOf<Int, ScheduledFuture<*>?>()
    private val autoSaveScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private var editor: Editor? = null
    private var currentPackage: ContentPackage? = null
    private var currentPart: ContentPart? = null
    private var currentNote: Note? = null

    private val _iinkEditor = MutableLiveData<Editor?>(null)
    val iinkEditor: LiveData<Editor?>
        get() = _iinkEditor

    private fun noteFile(note: Note): File = File(rootDir, "${note.id}.iink")

    private fun getPart(note: Note): Pair<ContentPackage, ContentPart> {
        val noteFile = noteFile(note)
        return if (!noteFile.exists()) {
            val contentPackage = engine.createPackage(noteFile)
            val contentPart = contentPackage.createPart("Text")
            contentPackage.save()
            contentPackage to contentPart
        } else {
            val contentPackage = engine.openPackage(noteFile)
            val contentPart = contentPackage.getPart(0)
            contentPackage to contentPart
        }
    }

    private fun saveNote(note: Note) {
        engine.openPackage(noteFile(note)).use { contentPackage ->
            contentPackage.save()
        }
    }

    private fun startAutoSave(note: Note, period: Long = 10_000) {
        if (period > 0 && !autoSaveTasks.containsKey(note.id)) {
            autoSaveTasks[note.id] = autoSaveScheduler.scheduleAtFixedRate({
                engine.openPackage(noteFile(note)).use { contentPackage ->
                    contentPackage.saveToTemp()
                }
            }, 0, period, TimeUnit.MILLISECONDS)
        }
    }

    private fun stopAutoSave(note: Note) {
        autoSaveTasks[note.id]?.cancel(true)
        autoSaveTasks.remove(note.id)
    }

    fun openNote(note: Note) {
        // FIXME async + clear note/editor lifecycle
        val (contentPackage, contentPart) = getPart(note)
        currentPackage = contentPackage
        currentPart = contentPart
        currentNote = note
        startAutoSave(note)
    }

    fun closeNote() {
        // TODO shouldn't we close (without notif?) the editor if any is still opened?
        currentPart?.close()
        currentPart = null
        currentPackage?.close()
        currentPackage = null
        currentNote?.let { note ->
            stopAutoSave(note)
            saveNote(note)
        }
    }

    fun openEditor(displayMetrics: DisplayMetrics, renderTarget: IRenderTarget) {
        val renderer = engine.createRenderer(displayMetrics.xdpi, displayMetrics.ydpi, renderTarget)
        editor = engine.createEditor(renderer).apply {
            renderer.setViewOffset(0f, 0f)
            renderer.viewScale = 1f
            setFontMetricsProvider(FontMetricsProvider(displayMetrics, emptyMap()))
            part = currentPart
        }
        viewModelScope.launch(Dispatchers.Main) {
            _iinkEditor.value = editor
        }
    }

    fun closeEditor() {
        viewModelScope.launch(Dispatchers.Main) {
            _iinkEditor.value = null
        }
        editor?.let {
            it.setFontMetricsProvider(null)
            it.part = null
            it.renderer.close()
            it.close()
        }
        editor = null
    }

    suspend fun exportAsText(): String {
        return withContext(Dispatchers.IO) {
            editor?.export_(null, MimeType.TEXT) ?: ""
        }
    }
}