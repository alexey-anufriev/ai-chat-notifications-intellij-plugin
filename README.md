# AI Chat Notifications

![JetBrains Plugin Version](https://img.shields.io/jetbrains/plugin/v/com.alexey-anufriev.ai-chat-notifications-intellij-plugin)
![JetBrains Plugin Downloads](https://img.shields.io/jetbrains/plugin/d/com.alexey-anufriev.ai-chat-notifications-intellij-plugin)
![JetBrains Plugin Rating](https://img.shields.io/jetbrains/plugin/r/rating/com.alexey-anufriev.ai-chat-notifications-intellij-plugin)

AI Chat Notifications is an IntelliJ Platform Plugin that shows a desktop notification
when an AI Coding Agent that runs in the IDE needs your attention.

## Use Case

You start a development task, ask an AI Agent to do the work, then minimize the IDE, or switch to another app while it runs.
When the agent needs approval, input, or a decision, that prompt is easy to miss.

This plugin observes JetBrains AI Assistant chat session state and sends a desktop notification
when an agent starts waiting for your attention. It does not depend on the AI chat UI being visible,
so it continues to work while the IDE window is minimized.

When another IDE project window is active, the plugin shows a native IDE notification with an
**Open** action that focuses the project window where the agent needs input. Notifications are not
shown for requests in the currently active project window.

![AI Chat Notifications popup](docs/notification.png)

## License

This project is licensed under the [Apache License 2.0](LICENSE).
