package cormoran.pepper.metrics;

import java.util.List;

public interface ITaskActivityEvent {

	/**
	 * 
	 * @return the object marked as source of this event
	 */
	Object getSource();

	/**
	 * @return the {@link List} of names identifying this event. It
	 */
	List<?> getNames();

}
