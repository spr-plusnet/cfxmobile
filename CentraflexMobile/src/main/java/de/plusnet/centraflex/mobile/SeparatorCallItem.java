package de.plusnet.centraflex.mobile;

/**
 * @author prelle
 *
 */
public class SeparatorCallItem implements CallListItem {
	
	private String text;

	//-------------------------------------------------------------------
	public SeparatorCallItem(String text) {
		this.text = text;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.plusnet.centraflex.mobile.CallListItem#getName()
	 */
	@Override
	public String getName() {
		return text;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CallListItem o) {
		return 0;
	}

}
