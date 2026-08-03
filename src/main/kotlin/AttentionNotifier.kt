package com.alexey_anufriev.ai_chat_notifications

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.ui.SystemNotifications

object AttentionNotifier {

    private val log = Logger.getInstance(AttentionNotifier::class.java)

    fun show(project: Project, title: String, message: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val frame = WindowManager.getInstance().getFrame(project)
            if (frame?.isActive == true) {
                return@executeOnPooledThread
                }

            try {
                if (ApplicationManager.getApplication().isActive) {
                    notifyFromInactiveProjectWindow(project, title, message)
                } else {
                    SystemNotifications.getInstance().notify("AI Chat Attention Required", title, message)
                }
            } catch (error: Throwable) {
                log.warn("Failed to request IntelliJ system notification", error)
            }
        }
    }

    private fun notifyFromInactiveProjectWindow(project: Project, title: String, message: String) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("AI Chat Notifications")
            .createNotification(title, message, NotificationType.INFORMATION)

        notification.addAction(NotificationAction.createSimpleExpiring("Open", Runnable {
            WindowManager.getInstance().getFrame(project)?.apply {
                toFront()
                requestFocus()
            }
        }))

        Notifications.Bus.notify(notification)
    }

}
