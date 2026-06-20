package com.example.timedrop.data

import android.util.Log
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.CalendarEvent
import com.example.timedrop.data.monitoring.FirebaseMonitorStore
import com.example.timedrop.data.monitoring.OpLog
import com.example.timedrop.data.monitoring.OpType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.SupervisorJob

class SyncRepository(private val db: AppDatabase) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var eventsListener: ListenerRegistration? = null
    private var notesListener: ListenerRegistration? = null
    private var profileListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val userId: String?
        get() = auth.currentUser?.uid

    fun getUserIdFlow(): Flow<String?> = callbackFlow {
        // Enviar estado inicial
        trySend(auth.currentUser?.uid ?: "No conectado")
        
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid ?: "No conectado")
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Sincroniza un evento con Firestore.
     * Se guarda en la ruta: users/{userId}/events/{eventId}
     */
    suspend fun syncEvent(event: CalendarEvent) {
        val uid = userId ?: return
        
        try {
            val eventMap = hashMapOf(
                "id" to event.id,
                "title" to event.title,
                "date" to event.date,
                "time" to event.time,
                "colorArgb" to event.colorArgb,
                "description" to event.description,
                "isTask" to event.isTask,
                "isCompleted" to event.isCompleted,
                "repeatInterval" to event.repeatInterval,
                "completedAt" to event.completedAt,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .collection("events")
                .document(event.id.toString())
                .set(eventMap, SetOptions.merge())
            FirebaseMonitorStore.record(OpLog(type = OpType.WRITE, source = "event"))
            Log.d("SyncRepository", "Evento sincronizado: ${event.title}")
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error al sincronizar evento", e)
        }
    }

    /**
     * Elimina un evento de Firestore.
     */
    suspend fun deleteEvent(eventId: Int) {
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId.toString())
                .delete()
            FirebaseMonitorStore.record(OpLog(type = OpType.DELETE, source = "event"))
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error al eliminar evento de la nube", e)
        }
    }

    /**
     * Sincroniza una nota con Firestore.
     */
    suspend fun syncNote(note: com.example.timedrop.data.local.Note) {
        val uid = userId ?: return
        try {
            val noteMap = hashMapOf(
                "id" to note.id,
                "title" to note.title,
                "content" to note.content,
                "date" to note.date,
                "category" to note.category,
                "timestamp" to note.timestamp,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .collection("notes")
                .document(note.id.toString())
                .set(noteMap, SetOptions.merge())
            FirebaseMonitorStore.record(OpLog(type = OpType.WRITE, source = "note"))
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error al sincronizar nota", e)
        }
    }

    /**
     * Elimina una nota de Firestore.
     */
    suspend fun deleteNote(noteId: Int) {
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("notes")
                .document(noteId.toString())
                .delete()
            FirebaseMonitorStore.record(OpLog(type = OpType.DELETE, source = "note"))
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error al eliminar nota de la nube", e)
        }
    }

    /**
     * Inicia la escucha en tiempo real de todos los datos.
     */
    fun startRealtimeSync(onSettingsUpdate: ((Map<String, Any>) -> Unit)? = null) {
        stopRealtimeSync() // Evitar duplicados
        val uid = userId ?: return
        
        listenToEvents(uid)
        listenToNotes(uid)
        listenToProfile(uid)
        if (onSettingsUpdate != null) {
            listenToSettings(onSettingsUpdate)
        }
        Log.d("SyncRepository", "Sincronización en tiempo real activada")
    }

    /**
     * Detiene los listeners para ahorrar batería/datos.
     */
    fun stopRealtimeSync() {
        eventsListener?.remove()
        notesListener?.remove()
        profileListener?.remove()
        eventsListener = null
        notesListener = null
        profileListener = null
    }

    private fun listenToEvents(uid: String) {
        eventsListener = firestore.collection("users")
            .document(uid)
            .collection("events")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                snapshot?.documentChanges?.forEach { change ->
                    scope.launch {
                        try {
                            val doc = change.document
                            when (change.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    val event = CalendarEvent(
                                        id = doc.getLong("id")?.toInt() ?: 0,
                                        title = doc.getString("title") ?: "",
                                        date = doc.getString("date") ?: "",
                                        time = doc.getString("time") ?: "",
                                        colorArgb = doc.getLong("colorArgb")?.toInt() ?: 0,
                                        description = doc.getString("description") ?: "",
                                        isTask = doc.getBoolean("isTask") ?: false,
                                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                                        repeatInterval = doc.getString("repeatInterval") ?: "none",
                                        completedAt = doc.getLong("completedAt") ?: 0L
                                    )
                                    db.calendarEventDao().insertEvent(event)
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    val eventId = doc.getLong("id")?.toInt() ?: return@launch
                                    val existing = db.calendarEventDao().getEventById(eventId)
                                    if (existing != null) {
                                        db.calendarEventDao().deleteEvent(existing)
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            Log.e("SyncRepository", "Error procesando cambio de evento", ex)
                        }
                    }
                }
            }
    }

    private fun listenToNotes(uid: String) {
        notesListener = firestore.collection("users")
            .document(uid)
            .collection("notes")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                snapshot?.documentChanges?.forEach { change ->
                    scope.launch {
                        try {
                            val doc = change.document
                            when (change.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    val note = com.example.timedrop.data.local.Note(
                                        id = doc.getLong("id")?.toInt() ?: 0,
                                        title = doc.getString("title") ?: "",
                                        content = doc.getString("content") ?: "",
                                        date = doc.getString("date") ?: "",
                                        timestamp = doc.getLong("timestamp") ?: 0L,
                                        category = doc.getString("category") ?: "Ideas"
                                    )
                                    db.noteDao().insertNote(note)
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    val noteId = doc.getLong("id")?.toInt() ?: return@launch
                                    val existing = db.noteDao().getNoteById(noteId)
                                    if (existing != null) {
                                        db.noteDao().deleteNote(existing)
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            Log.e("SyncRepository", "Error procesando cambio de nota", ex)
                        }
                    }
                }
            }
    }

    /**
     * Sincroniza los ajustes y la racha del usuario.
     */
    suspend fun syncUserSettings(settingsMap: Map<String, Any>) {
        val uid = userId ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .collection("config")
                .document("settings")
                .set(settingsMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error al sincronizar ajustes", e)
        }
    }

    /**
     * Escucha cambios en la configuración (racha, perfil, etc.)
     */
    fun listenToSettings(onUpdate: (Map<String, Any>) -> Unit) {
        val uid = userId ?: return
        firestore.collection("users")
            .document(uid)
            .collection("config")
            .document("settings")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val data = snapshot.data ?: return@addSnapshotListener
                onUpdate(data)
            }
    }

    /**
     * Asegura que el usuario tenga una sesión iniciada.
     * Ya no usaremos inicio anónimo automático para evitar que el ID cambie.
     */
    suspend fun signInAnonymously(): String? {
        // Solo iniciamos anónimamente si NO hay un usuario ya logueado
        if (auth.currentUser != null) return auth.currentUser?.uid
        
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.uid
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error en inicio de sesión anónimo", e)
            null
        }
    }

    /**
     * Inicia sesión con correo y contraseña.
     */
    suspend fun signInWithEmail(email: String, password: String): String? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.uid
        } catch (e: Exception) {
            // Si el usuario no existe en Firebase pero sí en la app local, lo registramos
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.uid
            } catch (e2: Exception) {
                Log.e("SyncRepository", "Error en login/registro Firebase", e2)
                null
            }
        }
    }

    /**
     * Descarga todos los eventos del usuario desde la nube.
     */
    suspend fun downloadAllEvents(): List<CalendarEvent> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("events")
                .get()
                .await()
            
            val result = snapshot.documents.mapNotNull { doc ->
                try {
                    CalendarEvent(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        title = doc.getString("title") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        colorArgb = doc.getLong("colorArgb")?.toInt() ?: 0,
                        description = doc.getString("description") ?: "",
                        isTask = doc.getBoolean("isTask") ?: false,
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        repeatInterval = doc.getString("repeatInterval") ?: "none",
                        completedAt = doc.getLong("completedAt") ?: 0L
                    )
                } catch (e: Exception) { null }
            }
            if (result.isNotEmpty()) FirebaseMonitorStore.record(OpLog(type = OpType.READ, source = "batch_download", count = result.size))
            result
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error al descargar eventos", e)
            emptyList()
        }
    }

    /**
     * Descarga todas las notas del usuario desde la nube.
     */
    suspend fun downloadAllNotes(): List<com.example.timedrop.data.local.Note> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("notes")
                .get()
                .await()
            
            val result = snapshot.documents.mapNotNull { doc ->
                try {
                    com.example.timedrop.data.local.Note(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        date = doc.getString("date") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        category = doc.getString("category") ?: "Ideas"
                    )
                } catch (e: Exception) { null }
            }
            if (result.isNotEmpty()) FirebaseMonitorStore.record(OpLog(type = OpType.READ, source = "batch_download", count = result.size))
            result
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error al descargar notas", e)
            emptyList()
        }
    }

    /**
     * Sube todos los eventos en lotes (Batch) divididos para evitar límites de Firestore.
     */
    suspend fun uploadAllEvents(events: List<CalendarEvent>) {
        val uid = userId ?: return
        
        try {
            // Dividimos en fragmentos de 400 (límite de Firestore es 500)
            events.chunked(400).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { event ->
                    val docRef = firestore.collection("users").document(uid).collection("events").document(event.id.toString())
                    val eventMap = hashMapOf(
                        "id" to event.id,
                        "title" to event.title,
                        "date" to event.date,
                        "time" to event.time,
                        "colorArgb" to event.colorArgb,
                        "description" to event.description,
                        "isTask" to event.isTask,
                        "isCompleted" to event.isCompleted,
                        "repeatInterval" to event.repeatInterval,
                        "completedAt" to event.completedAt,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, eventMap, SetOptions.merge())
                }
                
                // Ejecutamos el commit sin .await() para que no se cuelgue si no hay internet
                // Firestore lo guardará localmente y sincronizará en segundo plano
                batch.commit()
                FirebaseMonitorStore.record(OpLog(type = OpType.WRITE, source = "batch_upload", count = chunk.size))
            }
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error en batch de eventos", e)
            throw e
        }
    }

    /**
     * Sube todas las notas en lotes (Batch) divididos para evitar límites de Firestore.
     */
    suspend fun uploadAllNotes(notes: List<com.example.timedrop.data.local.Note>) {
        val uid = userId ?: return
        
        try {
            notes.chunked(400).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { note ->
                    val docRef = firestore.collection("users").document(uid).collection("notes").document(note.id.toString())
                    val noteMap = hashMapOf(
                        "id" to note.id,
                        "title" to note.title,
                        "content" to note.content,
                        "date" to note.date,
                        "category" to note.category,
                        "timestamp" to note.timestamp,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, noteMap, SetOptions.merge())
                }
                
                batch.commit()
                FirebaseMonitorStore.record(OpLog(type = OpType.WRITE, source = "batch_upload", count = chunk.size))
            }
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error en batch de notas", e)
            throw e
        }
    }

    /**
     * Descarga los ajustes del usuario (Racha, etc.) de la nube.
     */
    suspend fun downloadUserSettings(): Map<String, Any>? {
        val uid = userId ?: return null
        return try {
            withTimeoutOrNull(20000) {
                firestore.collection("users")
                    .document(uid)
                    .collection("config")
                    .document("settings")
                    .get()
                    .await()
                    .data
            }
        } catch (e: Exception) {
            null
        }
    }
    /**
     * Sincroniza el perfil del usuario (nombre, alias, foto base64)
     */
    suspend fun updateUserProfile(fullName: String, alias: String, photoBase64: String?) {
        val uid = userId ?: return
        try {
            val profileMap = hashMapOf(
                "fullName" to fullName,
                "alias" to alias,
                "photoBase64" to photoBase64,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(uid)
                .collection("profile")
                .document("info")
                .set(profileMap, SetOptions.merge())
            FirebaseMonitorStore.record(OpLog(type = OpType.WRITE, source = "profile"))
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error al actualizar perfil en nube", e)
        }
    }

    private fun listenToProfile(uid: String) {
        profileListener = firestore.collection("users")
            .document(uid)
            .collection("profile")
            .document("info")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                
                scope.launch {
                    try {
                        val fullName = snapshot.getString("fullName") ?: return@launch
                        val alias = snapshot.getString("alias") ?: ""
                        val photoBase64 = snapshot.getString("photoBase64")
                        
                        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                        if (currentUserEmail != null) {
                            val existingUser = db.userDao().findUserByEmail(currentUserEmail)
                            if (existingUser != null) {
                                var photoPath = existingUser.photoUri
                                
                                // Si hay una foto nueva en base64, la guardamos localmente
                                if (photoBase64 != null) {
                                    photoPath = saveBase64ToFile(photoBase64)
                                }
                                
                                val updatedUser = existingUser.copy(
                                    fullName = fullName,
                                    alias = alias,
                                    photoUri = photoPath
                                )
                                db.userDao().insertUser(updatedUser)
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e("SyncRepository", "Error procesando cambio de perfil", ex)
                    }
                }
            }
    }

    private fun saveBase64ToFile(base64: String): String? {
        return try {
            val context = com.example.timedrop.TimeDropApplication.getAppContext()
            val byteArray = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val file = java.io.File(context.filesDir, "profile_photo_sync.jpg")
            java.io.FileOutputStream(file).use { it.write(byteArray) }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
