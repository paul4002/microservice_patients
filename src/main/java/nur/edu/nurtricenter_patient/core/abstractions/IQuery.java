package nur.edu.nurtricenter_patient.core.abstractions;

import an.awesome.pipelinr.Command;

/**
 * Marker interface for queries (read-only operations).
 * All query records should implement this instead of Command<R> directly.
 */
public interface IQuery<R> extends Command<R> {}
