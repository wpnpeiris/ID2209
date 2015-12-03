/**
 * 
 */
package kth.id2209.nqueens;

/**
 * @author pradeeppeiris
 *
 */
public interface ChessBoardGui {

	void setAgent(ChessBoardAgent agent);
	
	void update(int row, int col, boolean on);
	
	void show(int n);
}
