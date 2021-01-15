package de.plusnet.centraflex.mobile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.javafx.Page;
import org.prelle.javafx.Card;
import org.prelle.javafx.JavaFXConstants;
import org.prelle.javafx.Persona;
import org.prelle.javafx.SymbolIcon;

import de.centraflex.callcontrol.Call;
import de.centraflex.callcontrol.Call.Personality;
import de.centraflex.callcontrol.CallControlService;
import de.centraflex.callcontrol.events.CallControlEvent;
import de.centraflex.callcontrol.events.CallControlServiceListener;
import de.centraflex.profile.ProfileService.Registration;
import de.centraflex.telephony.CallHistoryEntry;
import de.centraflex.telephony.Service;
import de.centraflex.telephony.Setting;
import de.centraflex.telephony.TelephonyService;
import de.centraflex.telephony.TelephonyService.ServiceComponent;
import de.centraflex.telephony.TelephonyService.State;
import de.centraflex.telephony.TelephonyServiceEventBus;
import de.centraflex.telephony.TelephonyServiceListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
public class CallPageController implements TelephonyServiceListener, CallControlServiceListener {
	
	private final static Logger logger = LogManager.getLogger(CallPageController.class);
	private final static ResourceBundle RES = ResourceBundle.getBundle(CallPageController.class.getPackageName()+".CallPage");

	private enum RingTargetLocation {
		All,
		Primary,
		BroadworksAnywhere,
		SharedCallAppearance,
		Mobility
	}
	private class RingTarget {
		RingTargetLocation location;
		String address;
		String userAgent;
		public RingTarget(RingTargetLocation loc) {
			this.location = loc;
			this.userAgent= "allen Geräten";
		}
		public RingTarget(RingTargetLocation loc, String addr, String ua) {
			this.location = loc;
			this.address  = addr;
			this.userAgent= ua;
		}
		public String toString() {
			return userAgent;
		}
	}
	
	private transient Page page;
	
	@FXML
	private CheckBox roEnabled;
	@FXML
	private TextField roTarget;
	
	@FXML
	private CheckBox cfaEnabled;
	@FXML
	private TextField cfaTarget;
	@FXML
	private CheckBox cfbEnabled;
	@FXML
	private TextField cfbTarget;
	@FXML
	private CheckBox cfnaEnabled;
	@FXML
	private TextField cfnaTarget;
	@FXML
	private CheckBox cfnrEnabled;
	@FXML
	private TextField cfnrTarget;
	
	@FXML
	private TextField tfDialTarget;
	@FXML
	private Button btnDial;
	@FXML
	private VBox bxCurrentCalls;
	@FXML
	private ListView<CallListItem> list;
	@FXML
	private Label cfHeading;
	@FXML
	private VBox ringControl;
	@FXML
	private VBox bxFront;
	@FXML
	private CheckBox cbDND;
	@FXML
	private ChoiceBox<String> cbCLID;
	@FXML
	private ChoiceBox<RingTarget> cbRingTarget;
	
	private transient TelephonyService telephony;
	private transient boolean initializing;

	//-------------------------------------------------------------------
	public CallPageController() {
	}

	//-------------------------------------------------------------------
	@FXML
	public void initialize() {
		assert roEnabled != null : "fx:id=\"roEnabled\" was not injected: check your FXML file";
		assert roTarget  != null : "fx:id=\"roTarget\"  was not injected: check your FXML file";
		initInteractivity();
		
		bxFront.setMaxHeight(Double.MAX_VALUE);
		
		list.setMaxHeight(Double.MAX_VALUE);
		list.setMinHeight(200);
		VBox.setVgrow(list, Priority.ALWAYS);
		
		cfHeading.setStyle("-fx-text-fill: rot; -fx-font-weight: bold"); 
		bxCurrentCalls.setStyle("-fx-padding: 0 2em 0 2em;"); 
		VBox.setVgrow(bxCurrentCalls, Priority.SOMETIMES);
		list.setCellFactory( lv -> new CallListItemCell());
		list.prefWidthProperty().bind(((Region)list.getParent()).widthProperty());
		
		cfnrTarget.setPrefColumnCount(10);
		
		cfaEnabled.setUserData(Setting.CFA_ENABLED);
		cfbEnabled.setUserData(Setting.CFB_ENABLED);
		cfnaEnabled.setUserData(Setting.CFNA_ENABLED);
		cfnrEnabled.setUserData(Setting.CFNR_ENABLED);
		cfaTarget.setUserData(Setting.CFA_TARGET);
		cfbTarget.setUserData(Setting.CFB_TARGET);
		cfnaTarget.setUserData(Setting.CFNA_TARGET);
		cfnrTarget.setUserData(Setting.CFNR_TARGET);
		
		cbDND.setDisable(true);
		cbCLID.setDisable(true);
		
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		tfDialTarget.setOnAction(ev -> dial());
		btnDial.setOnAction(ev -> dial());
		
		roTarget.disableProperty().bind(roEnabled.selectedProperty().not());
		roTarget.textProperty().addListener( (ov,o,n) -> {
			logger.info("Remote office target changed to '"+n+"'");
			Map<Setting, Object> values = new HashMap<>();
			values.put(Setting.REMOTE_OFFICE_ENABLED, roEnabled.isSelected());
			values.put(Setting.REMOTE_OFFICE_TARGET, n);
			telephony.getSettingService().configure(Service.REMOTE_OFFICE, values);
			refreshSettings();
		});
		roEnabled.selectedProperty().addListener( (ov,o,n) -> {
			logger.info("Remote office active changed to '"+n+"'");
			Map<Setting, Object> values = new HashMap<>();
			values.put(Setting.REMOTE_OFFICE_ENABLED, n);
			if (roTarget.getText()!=null && !roTarget.getText().isBlank())
				values.put(Setting.REMOTE_OFFICE_TARGET, roTarget.getText());
			telephony.getSettingService().configure(Service.REMOTE_OFFICE, values);
			refreshSettings();
		});
		
		//----- CFA -------------------------
		cfaTarget.disableProperty().bind(cfaEnabled.selectedProperty().not());
		cfaTarget.textProperty().addListener( (ov,o,n) -> changeCallForwarding(Service.CALL_FORWARDING_ALWAYS, cfaEnabled, cfaTarget));
		cfaEnabled.selectedProperty().addListener( (ov,o,n) -> changeCallForwarding(Service.CALL_FORWARDING_ALWAYS, cfaEnabled, cfaTarget));
		
		//----- CFB -------------------------
		cfbTarget.disableProperty().bind(cfbEnabled.selectedProperty().not());
		cfbTarget.textProperty().addListener( (ov,o,n) -> changeCallForwarding(Service.CALL_FORWARDING_BUSY, cfbEnabled, cfbTarget));
		cfbEnabled.selectedProperty().addListener( (ov,o,n) -> changeCallForwarding(Service.CALL_FORWARDING_BUSY, cfbEnabled, cfbTarget));
		
		//----- CFNA -------------------------
		cfnaTarget.disableProperty().bind(cfnaEnabled.selectedProperty().not());
		cfnaTarget.textProperty().addListener( (ov,o,n) -> changeCallForwarding(Service.CALL_FORWARDING_NO_ANSWER, cfnaEnabled, cfnaTarget));
		cfnaEnabled.selectedProperty().addListener( (ov,o,n) -> changeCallForwarding(Service.CALL_FORWARDING_NO_ANSWER, cfnaEnabled, cfnaTarget));
		
		//----- CFNR -------------------------
		cfnrTarget.disableProperty().bind(cfnrEnabled.selectedProperty().not());
		cfnrTarget.textProperty().addListener( (ov,o,n) -> changeCallForwarding(Service.CALL_FORWARDING_NOT_REACHABLE, cfnrEnabled, cfnrTarget));
		cfnrEnabled.selectedProperty().addListener( (ov,o,n) -> changeCallForwarding(Service.CALL_FORWARDING_NOT_REACHABLE, cfnrEnabled, cfnrTarget));
		
		bxCurrentCalls.heightProperty().addListener( (ov,o,n) -> {
			logger.info("Active Calls Height: "+n);
		});
		list.heightProperty().addListener( (ov,o,n) -> {
			logger.info("Call List Height: "+n);
		});
	}

	//-------------------------------------------------------------------
	private void changeCallForwarding(Service serv, CheckBox cbAct, TextField tfTarget) {
		logger.info(serv+" active changed");
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
		TelephonyServiceEventBus.addListener(this);
		initializing = true;
		
		logger.warn("Set TelephonyService to "+telephony);
		((Region)page.getContent()).setMaxWidth(Double.MAX_VALUE);
		
		page.setOnEnterAction(ev -> {
			refreshCallHistory();
			refreshSettings();
			refreshCurrentCalls();
			telephony.getCallControlService().addListener(this);
		});
		page.setOnLeaveAction(ev -> {
			telephony.getCallControlService().removeListener(this);
		});
		
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
				Platform.runLater( () -> refreshCurrentCalls());
				initializing = false;
				refreshSettings();
			} catch (Exception e) {
				logger.error("Error reading settings",e);
			}
			refreshCallHistory();
			btnDial.setDisable(false);
		}
	}

	//-------------------------------------------------------------------
	private void refreshSettings() {
		if (initializing)
			return;
		
		Platform.runLater( () -> {
			initializing = true;
			StringBuffer header = new StringBuffer();
			
			Map<Setting, Object> ro = telephony.getSettingService().getConfiguration(Service.REMOTE_OFFICE);
			if (!ro.isEmpty()) {
				roEnabled.setSelected( (Boolean)ro.get(Setting.REMOTE_OFFICE_ENABLED));
				roTarget.setText( (String)ro.get(Setting.REMOTE_OFFICE_TARGET));
				System.err.println("Set "+roTarget.getText());
				roEnabled.setDisable(false);
				if (roEnabled.isSelected()) {
					header.append("Remote Office: "+roTarget.getText());
				} else {
					header.append("Kein Remote Office");
				}
			} else {
				roEnabled.setSelected( false);
				roTarget.setText( "?");
				roEnabled.setDisable(true);
				page.setSecondaryHeader(new Label("From Controller"));
			}
			
			/*
			 * Call Forwarding Always
			 */
			updateService(Service.CALL_FORWARDING_ALWAYS, Setting.CFA_ENABLED, Setting.CFA_TARGET, cfaEnabled, cfaTarget, header);
			updateService(Service.CALL_FORWARDING_BUSY, Setting.CFB_ENABLED, Setting.CFB_TARGET, cfbEnabled, cfbTarget, header);
			updateService(Service.CALL_FORWARDING_NO_ANSWER, Setting.CFNA_ENABLED, Setting.CFNA_TARGET, cfnaEnabled, cfnaTarget, header);
			updateService(Service.CALL_FORWARDING_NOT_REACHABLE, Setting.CFNR_ENABLED, Setting.CFNR_TARGET, cfnrEnabled, cfnrTarget, header);

			page.setSecondaryHeader(new Label(header.toString()));

			telephony.getProfileService().getDevices();
			List<RingTarget> listRT = new ArrayList<RingTarget>();
			listRT.add(new RingTarget(RingTargetLocation.All));
			for (Registration regis : telephony.getProfileService().getRegistrations()) {
				switch (regis.type) {
				case PRIMARY:
					listRT.add(new RingTarget(RingTargetLocation.Primary, regis.lineport, regis.userAgent));
					break;
				case SCA:
					listRT.add(new RingTarget(RingTargetLocation.SharedCallAppearance, regis.lineport, regis.userAgent));
					break;
				default:
					break;
				
				}
			}
			cbRingTarget.getItems().setAll(listRT);
			cbRingTarget.getSelectionModel().select(0);
			initializing = false;
		});
		
	}
	
	//-------------------------------------------------------------------
	private void updateService(Service service, Setting ENABLED, Setting TARGET, CheckBox cbEnable, TextField tfTarget, StringBuffer header) {
		Map<Setting, Object> cfb = telephony.getSettingService().getConfiguration(service);
		if (!cfb.isEmpty()) {
			cbEnable.setSelected( (Boolean)cfb.get(ENABLED));
			tfTarget.setText( (String)cfb.get(TARGET));
			cbEnable.setDisable(false);
			if (cbEnable.isSelected()) {
				header.append(", "+ENABLED.name().substring(0, ENABLED.name().indexOf("_"))+" aktiv");
			}
		} else {
			cbEnable.setSelected( false);
			tfTarget.setText( "?");
			cbEnable.setDisable(true);
		}
		
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
	private void refreshCurrentCalls() {		
		bxCurrentCalls.getChildren().clear();
		for (Call call : telephony.getCallControlService().getActiveCalls()) {
			logger.info("Call with state "+call.getState()+" in personality "+call.getPersonality()+" remote "+call.getRemoteParty());
			
			Card persona = new Card(call.getRemoteParty().getName());
			// Strip "tel:"
			if (call.getRemoteParty().getAddress().startsWith("tel:")) {
				persona.setSecondaryText(call.getRemoteParty().getAddress().substring(4));
			} else {
				persona.setSecondaryText(call.getRemoteParty().getAddress());
			}
			Button btnHold = new Button(ResourceI18N.get(RES, "call.hold"), new SymbolIcon("pause"));
			btnHold.getStyleClass().addAll("card-action","call-action-hold");
			Button btnHangup = new Button(ResourceI18N.get(RES, "call.hangup"), new SymbolIcon("stop"));
			btnHangup.getStyleClass().addAll("card-action","call-action-hangup");
			Button btnResume = new Button(ResourceI18N.get(RES, "call.resume"), new SymbolIcon("play"));
			btnResume.getStyleClass().addAll("card-action","call-action-resume");
			switch (call.getState()) {
			case ALERTING:
				if (call.getPersonality()==Personality.TERMINATOR) {
					btnHangup.setText(ResourceI18N.get(RES, "call.reject"));
				}
				break;
			case ACTIVE:
				persona.getButtons().add(btnHold);
				break;
			case HELD:
				persona.getButtons().add(btnResume);
				break;
			}
			persona.getButtons().add(btnHangup);
			bxCurrentCalls.getChildren().add(persona);
			
			btnHangup.setOnAction(ev -> {
				logger.info("Release call");
				telephony.getCallControlService().releaseCall(call);
			});
			btnHold.setOnAction(ev -> {
				logger.info("Hold call");
				telephony.getCallControlService().holdCall(call);
			});
			btnResume.setOnAction(ev -> {
				logger.info("Resume call");
				telephony.getCallControlService().resumeCall(call);
			});
		}
	}

	//-------------------------------------------------------------------
	private void refreshCallHistory() {		
		/* 
		 * Build a list of call list items.
		 * Start with the call history 
		 */
		List<CallListItem> items = new ArrayList<CallListItem>();
		
		Instant now = Instant.now();
		String lastText = "";
		for (CallHistoryEntry tmp : telephony.getCallLogs()) {
//			int days = (int) ChronoUnit.DAYS.between(now, tmp.getTime());
			String text = getDateText(now, tmp.getTime());;
//			if (days==-1)
//				text = "Gestern";
//			else if (days==-2)
//				text = "Vorgestern";
//			else {
//				text = getDateText(now, tmp.getTime());
//			}
			if (!lastText.equals(text)) {
				items.add(new SeparatorCallItem(text));
				lastText = text;
			}
			items.add(new CallHistoryItem(tmp));
		}
		
		
		list.getItems().clear();		
		list.getItems().addAll(items);
	}
	
	//-------------------------------------------------------------------
	private void dial() {
		String target = tfDialTarget.getText();
		logger.info("Dial '"+target+"' with ring target "+cbRingTarget.getValue());
		
		CallControlService callCtrl = telephony.getCallControlService();
		Map<String,String> params = new HashMap<String, String>();
		if (cbRingTarget.getValue()!=null) {
			RingTarget target2 = cbRingTarget.getValue();
			switch (target2.location) {
			case Primary:
				params.put("location", "Primary");
				break;
			case SharedCallAppearance:
				params.put("location", target2.location.name());
				params.put("locationAddress", target2.address);
				break;
			default:
				break;
			}
		}
		callCtrl.createCall(target, params);
		refreshCurrentCalls();
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.centraflex.callcontrol.events.CallControlServiceListener#handleCallControlEvent(de.centraflex.callcontrol.events.CallControlEvent)
	 */
	@Override
	public void handleCallControlEvent(CallControlEvent event) {
		// TODO Auto-generated method stub
		logger.info("handleCallControlEvent "+event);
		Platform.runLater( () -> refreshCurrentCalls());
	}

}

class CallListItemCell extends ListCell<CallListItem> {
	
	private static DateTimeFormatter formatter =
		    DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
            .withLocale( Locale.GERMANY )
            .withZone( ZoneId.systemDefault() );
	
	private HistoryCallPane personaPane = new HistoryCallPane();
	
	public void updateItem(CallListItem item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			if (item instanceof SeparatorCallItem) {
				Label sep = new Label(  ((SeparatorCallItem)item).getName());
				sep.getStyleClass().add(JavaFXConstants.STYLE_HEADING5);
				setGraphic(sep);
				setText(null);
			} else if (item instanceof CallHistoryItem) {
				CallHistoryEntry history = ((CallHistoryItem)item).getEntry();
				Persona persona = personaPane.getPersona();
				persona.setText(item.getName());
				persona.setSecondaryText(formatter.format(history.getTime()));
				switch (history.getType()) {
				case PLACED:
					personaPane.getIcon().setSymbol("OutgoingCall");
					personaPane.getIcon().setStyle("-fx-fill: green; -fx-font-size: 20pt;");
					break;
				case RECEIVED:
					personaPane.getIcon().setSymbol("IncomingCall");
					personaPane.getIcon().setStyle("-fx-fill: green; -fx-font-size: 20pt;");
					break;
				case MISSED:
					personaPane.getIcon().setSymbol("IncomingCall");
					personaPane.getIcon().setStyle("-fx-fill: red; -fx-font-size: 20pt;");
					break;
				}
				setGraphic(personaPane);
			} else {
				Persona persona = personaPane.getPersona();
				persona.setText(item.getName());
				
				setGraphic(persona);
			}
		}
	}
}

class HistoryCallPane extends HBox {
	
	private SymbolIcon icon;
	private Persona persona;
	
	public HistoryCallPane() {
		super(10);
		persona = new Persona();
		icon = new SymbolIcon("Phone");
		icon.setStyle("-fx-font-size: 20pt");
		getChildren().addAll(icon, persona);
	}
	public Persona getPersona() { 
		return persona;
	}
	public SymbolIcon getIcon() { 
		return icon;
	}
}