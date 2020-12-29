package de.plusnet.centraflex.mobile;

import de.centraflex.telephony.CallHistoryEntry;

/**
 * @author prelle
 *
 */
public class CallHistoryItem implements CallListItem {

	private CallHistoryEntry data;
	
	//-------------------------------------------------------------------
	/**
	 */
	public CallHistoryItem(CallHistoryEntry item) {
		this.data = item;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.plusnet.centraflex.mobile.CallListItem#getName()
	 */
	@Override
	public String getName() {
		if ("Unavailable".equals(data.getName()))
			return data.getPhoneNumber();
		return data.getName();
	}

	//-------------------------------------------------------------------
	public CallHistoryEntry getEntry() {
		return data;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CallListItem other) {
		if (other instanceof ActiveCallItem)
			return 1;
		if (other instanceof SeparatorCallItem)
			return 0;
		
		CallHistoryItem o2 = (CallHistoryItem)other;
		return data.getTime().compareTo(o2.data.getTime());
	}

}
