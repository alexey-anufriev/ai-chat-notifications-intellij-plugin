package com.alexey_anufriev.ai_chat_notifications

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class AiAssistantNotificationActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        AiAssistantAttentionListener.install(project)
    }
}
