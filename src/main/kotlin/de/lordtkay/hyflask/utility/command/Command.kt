package de.lordtkay.hyflask.utility.command

/**
 * Interface for a command in the Command Pattern.
 * 
 * Commands are executed by a [CommandInvoker] and stored in a history to enable undo/redo functionality.
 */
interface Command {
    /**
     * Executes the command.
     *
     * This method performs the action associated with the command.
     * Upon successful execution, it is expected to return true, indicating that the command can be added to the execution history.
     * If the command fails to execute properly, it should return false.
     *
     * @return true if the command was executed successfully; false otherwise.
     */
    fun execute(): Boolean

    /**
     * Reverts the changes made by the command when it was executed.
     *
     * This method is responsible for undoing the operation associated with the command.
     * In some cases, it may return a new command that represents the inverse of the undo operation,
     * allowing for further execution if necessary.
     *
     * @return a new command representing the inverse operation of the undo action, or null if no further action is required.
     */
    fun undo(): Command?
}
