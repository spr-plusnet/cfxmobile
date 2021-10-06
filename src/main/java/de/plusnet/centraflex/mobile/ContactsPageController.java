package de.plusnet.centraflex.mobile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.prelle.javafx.Page;
import org.prelle.javafx.ResponsiveControlManager;
import org.prelle.javafx.WindowMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.centraflex.callcontrol.events.CallControlEvent;
import de.centraflex.callcontrol.events.CallControlServiceListener;
import de.centraflex.contacts.Contact;
import de.centraflex.contacts.ContactManager.ContactManagerListener;
import de.centraflex.telephony.Service;
import de.centraflex.telephony.Setting;
import de.centraflex.telephony.TelephonyService;
import de.centraflex.telephony.TelephonyService.ServiceComponent;
import de.centraflex.telephony.TelephonyService.State;
import de.centraflex.telephony.TelephonyServiceEventBus;
import de.centraflex.telephony.TelephonyServiceListener;
import de.plusnet.centraflex.mobile.cells.ContactCell;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * @author prelle
 *
 */
public class ContactsPageController implements TelephonyServiceListener, CallControlServiceListener, ContactManagerListener {

	private final static Logger logger = LogManager.getLogger(ContactsPageController.class);
	private final static ResourceBundle RES = ResourceBundle.getBundle(ContactsPageController.class.getPackageName()+".ContactsPage");

	private transient Page page;

	@FXML
	private Label test;
	@FXML
	private CheckBox cbHideOffline;

	@FXML
	private TextField tfSearch;
	@FXML
	private Button    btnAction;
	@FXML
	private ListView<Contact> list;
	@FXML
	private HBox listBox;

	private transient TelephonyService telephony;
	private transient boolean initializing;

	//-------------------------------------------------------------------
	public ContactsPageController() {
	}

	//-------------------------------------------------------------------
	@FXML
	public void initialize() {
		assert list != null : "fx:id=\"list\" was not injected: check your FXML file";
		initInteractivity();

		list.setMaxHeight(Double.MAX_VALUE);
		list.setMinHeight(200);
		VBox.setVgrow(list, Priority.ALWAYS);

		list.prefWidthProperty().bind(((Region)list.getParent()).widthProperty());
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		tfSearch.setOnAction(ev -> search());
		btnAction.setOnAction(ev -> search());

		list.getSelectionModel().selectedItemProperty().addListener( (ov,o,n) -> selectContact(n));
	}

	//-------------------------------------------------------------------
	private void changeCallForwarding(Service serv, CheckBox cbAct, TextField tfTarget) {
		logger.info(serv+" active changed");
		if (initializing)
			return;
		Map<Setting, Object> values = new HashMap<>();
		values.put((Setting)cbAct.getUserData(), cbAct.isSelected());
		if (tfTarget.getText()!=null && !tfTarget.getText().isBlank())
			values.put((Setting)tfTarget.getUserData(), tfTarget.getText());
		if (telephony.getSettingService().configure(serv, values)) {
			refreshSettings();
		} else
			logger.error("Failed changing "+serv);
	}

	//-------------------------------------------------------------------
	public void setComponent(Page page, TelephonyService telephony) {
		this.page = page;
		this.telephony = telephony;
		if (telephony==null)
			throw new NullPointerException("TelephonyService may not be null");
		TelephonyServiceEventBus.addListener(this);
		initializing = true;
		list.setCellFactory( lv -> new ContactCell());

		logger.warn("Set TelephonyService to "+telephony);
		((Region)page.getContent()).setMaxWidth(Double.MAX_VALUE);

		page.setOnEnterAction(ev -> {
			logger.info("Entering ContactsPage");
//			refreshCallHistory();
			refreshSettings();
			refreshList();
//			telephony.getCallControlService().addListener(this);
		});
//		page.setOnLeaveAction(ev -> {
//			telephony.getCallControlService().removeListener(this);
//		});
		page.setOnSwipeDown(ev -> {
			logger.info("Refresh call history");
//			refreshCallHistory();
		});

		refreshList();
		// Request to receive updates if list changes
		telephony.getContactManager().addListener(this);
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.centraflex.telephony.TelephonyServiceListener#telephonyServiceChanged(de.centraflex.telephony.TelephonyService.State)
	 */
	@Override
	public void telephonyServiceChanged(ServiceComponent component, State newState) {
		logger.warn("-----------------State of "+component+" changed to "+newState+" in "+telephony);
		if (component==ServiceComponent.CALL_CONTROL && newState==State.READY) {
			try {
				telephony.getCallControlService().addListener(this);
				Platform.runLater( () -> refreshList());
				initializing = false;
				refreshSettings();
			} catch (Exception e) {
				logger.error("Error reading settings",e);
			}
//			refreshCallHistory();
//			btnDial.setDisable(false);
		}
	}

	//-------------------------------------------------------------------
	private void refreshSettings() {
		if (initializing)
			return;

		Platform.runLater( () -> {
			initializing = true;
			logger.debug("START: refreshSettings");
//			try {
//				StringBuffer header = new StringBuffer();
//
//				Map<Setting, Object> ro = telephony.getSettingService().getConfiguration(Service.REMOTE_OFFICE);
//				if (!ro.isEmpty()) {
//					roEnabled.setSelected( (Boolean)ro.get(Setting.REMOTE_OFFICE_ENABLED));
//					roTarget.setText( (String)ro.get(Setting.REMOTE_OFFICE_TARGET));
//					roEnabled.setDisable(false);
//					if (roEnabled.isSelected()) {
//						header.append("Remote Office: "+roTarget.getText());
//					} else {
//						header.append("Kein Remote Office");
//					}
//				} else {
//					roEnabled.setSelected( false);
//					roTarget.setText( "?");
//					roEnabled.setDisable(true);
//					page.setSecondaryHeader(new Label("From Controller"));
//				}
//
//				/*
//				 * Call Forwarding Always
//				 */
//				updateService(Service.CALL_FORWARDING_ALWAYS, Setting.CFA_ENABLED, Setting.CFA_TARGET, cfaEnabled, cfaTarget, header);
//				updateService(Service.CALL_FORWARDING_BUSY, Setting.CFB_ENABLED, Setting.CFB_TARGET, cfbEnabled, cfbTarget, header);
//				updateService(Service.CALL_FORWARDING_NO_ANSWER, Setting.CFNA_ENABLED, Setting.CFNA_TARGET, cfnaEnabled, cfnaTarget, header);
//				updateService(Service.CALL_FORWARDING_NOT_REACHABLE, Setting.CFNR_ENABLED, Setting.CFNR_TARGET, cfnrEnabled, cfnrTarget, header);
//
//				page.setSecondaryHeader(new Label(header.toString()));
//
//				telephony.getProfileService().getDevices();
//				List<RingTarget> listRT = new ArrayList<RingTarget>();
//				listRT.add(new RingTarget(RingTargetLocation.All));
//				for (Registration regis : telephony.getProfileService().getRegistrations()) {
//					switch (regis.type) {
//					case PRIMARY:
//						listRT.add(new RingTarget(RingTargetLocation.Primary, regis.lineport, regis.userAgent));
//						break;
//					case SCA:
//						listRT.add(new RingTarget(RingTargetLocation.SharedCallAppearance, regis.lineport, regis.userAgent));
//						break;
//					default:
//						break;
//
//					}
//				}
//				cbRingTarget.getItems().setAll(listRT);
//				cbRingTarget.getSelectionModel().select(0);
//			} finally {
//				initializing = false;
//				logger.debug("STOP : refreshSettings");
//			}
		});

	}

	//-------------------------------------------------------------------
	private static String getDateText(Instant now, Instant value) {
		int today = now.atZone(ZoneId.systemDefault()).getDayOfYear();
		int dow   = value.atZone(ZoneId.systemDefault()).getDayOfYear();
		//		int days = (int) ChronoUnit.DAYS.between(now, value);
		int days = dow -today;
		//		int days = -calcWeekDays1(LocalDate.ofInstant(value, ZoneOffset.UTC), LocalDate.ofInstant(now, ZoneOffset.UTC));
		if (days==0)
			return "Heute";
		if (days==-1)
			return "Gestern";
		else if (days==-2)
			return "Vorgestern";

		// If today is monday or tuesday, all
		int todayWeek = now.atZone(ZoneId.systemDefault()).get(WeekFields.ISO.weekOfYear());
		int week = value.atZone(ZoneId.systemDefault()).get(WeekFields.ISO.weekOfYear());
		if (todayWeek==week)
			return "Diese Woche";
		int weekDiff = todayWeek - week;

		if (weekDiff==1)
			return "Letzte Woche";
		if (weekDiff==2)
			return "Vorletzte Woche";

		return "KW "+week;
	}

	//-------------------------------------------------------------------
	private void refreshList() {
		logger.debug("refreshList: "+telephony);
//		bxCurrentCalls.getChildren().clear();
		List<Contact> data = telephony.getContactManager().getContacts();
		list.getItems().setAll(data);
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.centraflex.callcontrol.events.CallControlServiceListener#handleCallControlEvent(de.centraflex.callcontrol.events.CallControlEvent)
	 */
	@Override
	public void handleCallControlEvent(CallControlEvent event) {
		// TODO Auto-generated method stub
		logger.info("handleCallControlEvent "+event);
		Platform.runLater( () -> refreshList());
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.centraflex.contacts.ContactManager.ContactManagerListener#contactsChanged()
	 */
	@Override
	public void contactsChanged() {
		logger.debug("Contacts changed");
		Platform.runLater( () -> refreshList());
	}

	//-------------------------------------------------------------------
	private void selectContact(Contact n) {
		logger.info("Selected "+n);
		
		SingleContactView box = new SingleContactView(n);
		if (ResponsiveControlManager.getCurrentMode()==WindowMode.MINIMAL) {
			Page newPage = new Page(ResourceI18N.get(RES, "label.contact"), box);
			newPage.setId("single-contact");
			page.getAppLayout().navigateTo(newPage, false);
		} else {
			listBox.getChildren().retainAll(list);
			listBox.getChildren().add(box);
		}
	}

	//-------------------------------------------------------------------
	private void search() {
		logger.debug("search "+tfSearch.getText());
		String key = tfSearch.getText();
		if (key==null || key.length()<3) {
			refreshList();
			return;
		}
		
		List<Contact> result = telephony.getDirectoryService().search(key);
		list.getItems().setAll(result);
			
	}
}
