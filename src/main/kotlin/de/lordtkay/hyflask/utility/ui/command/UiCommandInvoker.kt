package de.lordtkay.hyflask.utility.ui.command

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder

/**
 * Interface for managing the execution and history of commands in the Command Pattern.
 *
 * The CommandInvoker is responsible for executing commands, as well as supporting undo and redo functionality by maintaining
 * a history of executed commands. This enables reversible and re-executable operations in systems that implement this pattern.
 */
interface UiCommandInvoker {
    /**
     * Executes the given command.
     *
     * This method triggers the execution of the provided command. If the command executes successfully,
     * it is expected to be added to the execution history, allowing it to be undone or redone
     * as part of the Command Pattern's functionality.
     *
     * @param command The command to be executed. The command should encapsulate a specific action
     *                that can be performed, undone, and possibly redone.
     */
    fun execute(
        commandBuilder: UICommandBuilder,
        eventBuilder: UIEventBuilder,
        command: UiCommand
    )

    /**
     * Undoes the most recently executed command in the command history.
     *
     * This method reverts the changes made by the last successfully executed command
     * in the history. If the command supports an inverse operation, it is executed
     * as part of the undo process. The undone command is removed from the history
     * and added to the redo stack for potential re-execution.
     *
     * If there are no commands in the history, the method performs no operation.
     *
     * @throws Exception if an error occurs while undoing the command or its inverse operation.
     */
    fun undo(
        commandBuilder: UICommandBuilder,
        eventBuilder: UIEventBuilder
    )

    /**
     * Re-executes the most recently undone command in the redo history.
     *
     * This method retrieves the last command from the redo stack and executes it again, effectively reapplying
     * the changes made by the original execution. Once the redo operation is completed, the command is
     * added back to the execution history to allow for potential undo operations in the future.
     *
     * If there are no commands in the redo history, the method performs no operation.
     *
     * @throws Exception if an error occurs while re-executing the command.
     */
    fun redo(
        commandBuilder: UICommandBuilder,
        eventBuilder: UIEventBuilder
    )
}
