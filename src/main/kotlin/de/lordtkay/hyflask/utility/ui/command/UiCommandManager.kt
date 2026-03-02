package de.lordtkay.hyflask.utility.ui.command

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder

class UiCommandManager : UiCommandInvoker {
    private val history: MutableList<UiCommand> = ArrayList()
    private val redoHistory: MutableList<UiCommand> = ArrayList()

    val hasHistory get() = history.isNotEmpty()
    val hasRedoHistory get() = redoHistory.isNotEmpty()

    override fun execute(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder, command: UiCommand) {
        val successful = command.execute(commandBuilder, eventBuilder)
        if (successful) {
            history.add(command)
            redoHistory.clear()
        }
    }

    override fun undo(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder) {
        if (history.isEmpty()) return
        val command = history[history.size - 1]

        val undoCommand = command.undo()
        undoCommand?.execute(commandBuilder, eventBuilder)
        val removedHistory = history.removeAt(history.size - 1)
        redoHistory.add(removedHistory)
    }

    override fun redo(commandBuilder: UICommandBuilder, eventBuilder: UIEventBuilder) {
        if (redoHistory.isEmpty()) return
        redoHistory[redoHistory.size - 1]

        val redoCommand = redoHistory.removeAt(redoHistory.size - 1)
        redoCommand.execute(commandBuilder, eventBuilder)
        history.add(redoCommand)
    }
}
