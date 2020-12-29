package de.plusnet.centraflex.mobile;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.javafx.AppLayout.NavigationStyle;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.FlexibleApplication;
import org.prelle.javafx.NavigationItem;
import org.prelle.javafx.Page;
import org.prelle.javafx.SymbolIcon;

import de.centraflex.callcontrol.events.CallControlEvent;
import de.centraflex.callcontrol.events.CallControlServiceListener;
import de.centraflex.telephony.TelephonyService;
import de.centraflex.telephony.TelephonyServiceFactory;
import de.centraflex.telephony.config.xsi.XSIConfigParameters;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class CFXMobileMain extends FlexibleApplication implements CallControlServiceListener {
	
	private final static Logger logger = LogManager.getLogger(CFXMobileMain.class.getName());
	private final static String PREF_USER = "eden.user";
	private final static String PREF_PASS = "eden.pass";
	
	private ResourceBundle RES = ResourceBundle.getBundle(CFXMobileMain.class.getName(), CFXMobileMain.class.getModule());
	private Preferences pref;
	private TelephonyService telephony;
	
	private NavigationItem navCalls;
	
	private Map<NavigationItem,Page> pageMap;

	private int noLoginAttempts;

    //-------------------------------------------------------------------
    public static void main(String[] args) {
        launch();
    }

	//-------------------------------------------------------------------
	public CFXMobileMain() throws IOException {
		pref = Preferences.userNodeForPackage(CFXMobileMain.class);
		initComponents();
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		telephony = TelephonyServiceFactory.getInstance();
		
//		navContacts = new NavigationItem("Personen" , new SymbolIcon("Contact"));
//		navActivity = new NavigationItem("Aktivität", new SymbolIcon("browsephotos"));
		navCalls    = new NavigationItem("Anrufe"   , new SymbolIcon("Phone"));
//		navSettings = new NavigationItem("Einstellung", new SymbolIcon("setting"));
//		addPage(navContacts, new ContactsPage(telephony));
		
		pageMap = new HashMap<>();
		try {
			pageMap.put(navCalls, ScreenLoader.loadCallPage(telephony));
		} catch (Exception e) {
			logger.fatal("Could not set up UI",e);
			stop();
		}
	}

    //-------------------------------------------------------------------
    /**
     * @see org.prelle.javafx.FlexibleApplication#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage stage) throws Exception {
       	super.start(stage);
//       	this.getAppLayout().setNavigationStyle(NavigationStyle.MOBILE);
       	stage.setWidth(360);
       	stage.setHeight(574);
//       	String javaVersion = System.getProperty("java.version");
//        String javafxVersion = System.getProperty("javafx.version");
//        Label label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
//
//        ImageView imageView = new ImageView(new Image(CFXMobileMain.class.getResourceAsStream("openduke.png")));
//        imageView.setFitHeight(200);
//        imageView.setPreserveRatio(true);
//
//        VBox root = new VBox(30, imageView, label);
//        root.setAlignment(Pos.CENTER);
//
//        Scene scene = new Scene(root, 640, 480);
//        scene.getStylesheets().add(CFXMobileMain.class.getResource("styles.css").toExternalForm());
//        stage.setScene(scene);
//        stage.show();
       	stage.getScene().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
  		getAppLayout().getNavigationPane().getItems().addAll(navCalls);
		getAppLayout().getNavigationPane().getSelectionModel().select(navCalls);
       
        prepareXSILogin();
        login();
    }

	//-------------------------------------------------------------------
	/**
	 * @see javafx.application.Application#stop()
	 */
	@Override
	public void stop() {
		logger.info("Quitting");
//		centraflex.logout();
		System.exit(0);
	}
  
    //-------------------------------------------------------------------
    private void prepareXSILogin() {
		Authenticator.setDefault(new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				logger.warn("getPasswordAuthentication after "+noLoginAttempts+" login attempts  "+getRequestingHost());
				LoginDialog dialog = new LoginDialog(noLoginAttempts);
				dialog.setLogin(pref.get(PREF_USER, null));
				dialog.setPassword(pref.get(PREF_PASS, null));
				 Object foo = CFXMobileMain.this.showAndWait(dialog);
				 logger.warn("Returned "+foo);
				 if (foo!=CloseType.OK) {
					stop();
					 return null;
				 }
				pref.put(PREF_USER, dialog.getLogin());
				pref.put(PREF_PASS, dialog.getPassword());
				noLoginAttempts++;
				return new PasswordAuthentication (dialog.getLogin(), dialog.getPassword().toCharArray());
			}
		});
    }

	//-------------------------------------------------------------------
	private void login() {
		String user = "04212025933@qsc.de";
		String host = "web4.bmcag.com";
		
		
		Properties ccConfig = new Properties();
		ccConfig.setProperty(XSIConfigParameters.XSI_USER, user);
		ccConfig.setProperty(XSIConfigParameters.XSI_HOST, host);
		telephony.configure(ccConfig);
		telephony.getCallControlService().addListener(this);
//		centraflex = CentraflexService.createSession(host, Constants.APPNAME);
//		if (!centraflex.login(user, pass)) {
//			// Login failed
//			logger.fatal("Login failed");
//			System.exit(1);
//		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.javafx.FlexibleApplication#createPage(javafx.scene.control.MenuItem)
	 */
	@Override
	public Page createPage(MenuItem menuItem) {
		logger.info("createPage("+menuItem+")");
		Page page = pageMap.get(menuItem);
		if (page!=null)
			return page;
		logger.warn("No page for "+menuItem.getText());
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.centraflex.callcontrol.events.CallControlServiceListener#handleCallControlEvent(de.centraflex.callcontrol.events.CallControlEvent)
	 */
	@Override
	public void handleCallControlEvent(CallControlEvent event) {
		logger.info("handleCallControl: "+event);
		
	}

}