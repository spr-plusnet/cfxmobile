package de.plusnet.centraflex.mobile;

/**
 * @author prelle
 *
 */
public class ActiveCallItem implements CallListItem {

	//-------------------------------------------------------------------
	/**
	 */
	public ActiveCallItem() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CallListItem o) {
		// TODO Auto-generated method stub
		return 0;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.plusnet.centraflex.mobile.CallListItem#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "LÖSCH MICH";
	}

}
