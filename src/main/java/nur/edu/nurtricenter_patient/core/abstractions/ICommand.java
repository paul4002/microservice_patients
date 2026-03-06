package nur.edu.nurtricenter_patient.core.abstractions;

import an.awesome.pipelinr.Command;

/**
 * Marker interface for commands (write operations / mutations).
 * All command records should implement this instead of Command<R> directly.
 */
public interface ICommand<R> extends Command<R> {}
