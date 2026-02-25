package de.lordtkay.hyflask.utility.command

class CommandManager : CommandInvoker {
    private val history: MutableList<Command> = ArrayList<Command>()
    private val redoHistory: MutableList<Command> = ArrayList<Command>()

    override fun execute(command: Command) {
        try {
            val successful = command.execute()
            if (successful) {
                history.add(command)
                redoHistory.clear()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun undo() {
        if (history.isEmpty()) return
        val command = history[history.size - 1]

        try {
            val undoCommand = command.undo()
            undoCommand?.execute()
            val removedHistory = history.removeAt(history.size - 1)
            redoHistory.add(removedHistory)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun redo() {
        if (redoHistory.isEmpty()) return
        redoHistory[redoHistory.size - 1]

        try {
            val redoCommand = redoHistory.removeAt(redoHistory.size - 1)
            redoCommand.execute()
            history.add(redoCommand)
        } catch (e: Exception) {
            throw e
        }
    }
}
