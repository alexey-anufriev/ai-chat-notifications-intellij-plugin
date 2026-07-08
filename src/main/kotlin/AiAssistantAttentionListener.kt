package com.alexey_anufriev.ai_chat_notifications


import com.intellij.ml.llm.aui.events.api.ApprovableBlockUpdatedEvent
import com.intellij.ml.llm.chat.shared.ChatSessionMessageBlockEvent
import com.intellij.ml.llm.core.chat.session.ChatSession
import com.intellij.ml.llm.core.chat.session.ChatSessionHost
import com.intellij.ml.llm.core.chat.session.ChatSessionHostListener
import com.intellij.ml.llm.core.chat.session.impl.ChatSessionImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object AiAssistantAttentionListener {

    fun install(project: Project) {
        project.getService(AiAssistantAttentionListenerService::class.java).install(project)
    }
}

@Service(Service.Level.PROJECT)
class AiAssistantAttentionListenerService : Disposable {

    private val log = Logger.getInstance(AiAssistantAttentionListenerService::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val observationJobs = mutableMapOf<String, Job>()
    private val pendingRequestsByChat = mutableMapOf<String, MutableMap<String, String>>()

    fun install(project: Project) {
        val sessionHost = ChatSessionHost.getInstance(project)
        project.messageBus.connect(this).subscribe(
            ChatSessionHostListener.UPDATE_TOPIC,
            object : ChatSessionHostListener {
                override fun chatCreated(session: ChatSession) {
                    observe(project, session)
                }

                override fun chatRemoved(session: ChatSession) {
                    release(session)
                }
            },
        )

        sessionHost.getAllChats().forEach { session -> observe(project, session) }
        log.info("AI Assistant attention listener installed for project '${project.name}'")
    }

    private fun observe(project: Project, session: ChatSession) {
        val frontendSession = (session as? ChatSessionImpl)?.frontendSession
        if (frontendSession == null) {
            log.warn("Unsupported AI Assistant chat session implementation: ${session.javaClass.name}")
            return
        }

        synchronized(observationJobs) {
            if (observationJobs.containsKey(session.uid)) {
                return
            }

            observationJobs[session.uid] = frontendSession.getEventsFlow()
                .filterIsInstance<ChatSessionMessageBlockEvent>()
                .onEach { event ->
                    val block = event.event as? ApprovableBlockUpdatedEvent ?: return@onEach
                    val stepId = block.stepId ?: return@onEach
                    val requestId = block.approvalInputRequest?.id
                    if (updatePendingRequest(session.uid, stepId, requestId)) {
                        log.info("AI Assistant input request detected for chat '${session.uid}', step '$stepId'")
                        AttentionNotifier.show(
                            "Agent needs your attention",
                            "Attention required in ${project.name} project.",
                        )
                    }
                }
                .catch { error ->
                    log.warn("AI Assistant attention observation failed for chat '${session.uid}'", error)
                }
                .launchIn(scope)

            log.info("Observing AI Assistant events for chat '${session.uid}'")
        }
    }

    private fun release(session: ChatSession) {
        synchronized(observationJobs) {
            observationJobs.remove(session.uid)?.cancel()
        }
        synchronized(pendingRequestsByChat) {
            pendingRequestsByChat.remove(session.uid)
        }
    }

    private fun updatePendingRequest(chatId: String, stepId: String, requestId: String?): Boolean {
        synchronized(pendingRequestsByChat) {
            if (requestId == null) {
                val requestsByStep = pendingRequestsByChat[chatId] ?: return false
                requestsByStep.remove(stepId)
                if (requestsByStep.isEmpty()) {
                    pendingRequestsByChat.remove(chatId)
                }
                return false
            }

            val requestsByStep = pendingRequestsByChat.getOrPut(chatId, ::mutableMapOf)
            return requestsByStep.put(stepId, requestId) != requestId
        }
    }

    override fun dispose() {
        scope.cancel()
    }
}
