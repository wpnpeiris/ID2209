/**
 * 
 */
package kth.id2209.profiler;

/**
 * @author pradeeppeiris
 *
 */
public interface ProfilerGui {
	/**
	 * Set Profiler Agent
	 * @param agent
	 * 			Profiler Agent
	 */			
	void setAgent(ProfilerAgent agent);

	/**
	 * Show GUI
	 */
	void show();
	
	/**
	 * Update Tour suggestions
	 */
	void updateTourSuggestions(String content);
}
