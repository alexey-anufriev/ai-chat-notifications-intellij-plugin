package com.alexey_anufriev.ai_chat_notifications

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.SystemNotifications

object AttentionNotifier {

    private val log = Logger.getInstance(AttentionNotifier::class.java)

    fun show(title: String, message: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                SystemNotifications.getInstance().notify("AI Chat Attention Required", title, message)
            } catch (error: Throwable) {
                log.warn("Failed to request IntelliJ system notification", error)
            }
        }
    }

}
