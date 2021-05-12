package de.plusnet.centraflex.mobile;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.prelle.javafx.CloseType;
import org.prelle.javafx.FlexibleApplication;
import org.prelle.javafx.NavigationItem;
import org.prelle.javafx.NavigationPane;
import org.prelle.javafx.Page;
import org.prelle.javafx.SymbolIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import  com.gluonhq.attach.settings.SettingsService;

import de.centraflex.callcontrol.events.CallControlEvent;
import de.centraflex.callcontrol.events.CallControlServiceListener;
import de.centraflex.telephony.TelephonyService;
import de.centraflex.telephony.TelephonyServiceFactory;
import de.centraflex.telephony.config.xsi.XSIConfigParameters;
import de.centraflex.telephony.xsi.TelephonyServiceXSI;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class CFXMobileMain extends FlexibleApplication implements CallControlServiceListener {
	
	private final static String CFGKEY_USER = "cfxclient.user";
	private final static String CFGKEY_PASS = "cfxclient.pass";
	
	private final static Logger logger = LogManager.getLogger(CFXMobileMain.class.getName());
	
	private ResourceBundle RES = ResourceBundle.getBundle(CFXMobileMain.class.getName(), CFXMobileMain.class.getModule());
	private TelephonyService telephony;
	private Optional<SettingsService> settings;
	
	private NavigationItem navCalls;
	private NavigationItem navContacts;
	
	private Map<NavigationItem,Page> pageMap;

	private int noLoginAttempts;

    //-------------------------------------------------------------------
    public static void main(String[] args) {
        launch();
    }

	//-------------------------------------------------------------------
	public CFXMobileMain() throws IOException {
		settings = SettingsService.create();
		initComponents();
	}

	//-------------------------------------------------------------------
	private void initComponents() {
//		telephony = TelephonyServiceFactory.getInstance();
		telephony = new TelephonyServiceXSI();
		TelephonyServiceFactory.setInstance(telephony);
		
		navContacts = new NavigationItem("Personen" , new SymbolIcon("Contact"));
//		navActivity = new NavigationItem("Aktivität", new SymbolIcon("browsephotos"));
		navCalls    = new NavigationItem("Anrufe"   , new SymbolIcon("Phone"));
//		navSettings = new NavigationItem("Einstellung", new SymbolIcon("setting"));
//		addPage(navContacts, new ContactsPage(telephony));
		
		pageMap = new HashMap<>();
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.javafx.FlexibleApplication#populateNavigationPane(org.prelle.javafx.NavigationPane)
	 */
	@Override
	public void populateNavigationPane(NavigationPane drawer) {
		ImageView headerImage = new ImageView(new Image(CFXMobileMain.class.getResourceAsStream("Logo2_150.png")));
		headerImage.setFitWidth(150);
		headerImage.setPreserveRatio(true);
		drawer.setFooter(headerImage);
		drawer.getItems().add(navCalls);
		drawer.getItems().add(navContacts);
	}

    //-------------------------------------------------------------------
    /**
     * @see org.prelle.javafx.FlexibleApplication#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage stage) throws Exception {
       	try {
			super.start(stage);
			Font foo =Font.loadFont(CFXMobileMain.class.getResourceAsStream("fonts/Atlas_Grotesk_Web_Light_Regular.ttf"), 12.0);
			System.err.println("Loaded font 1 "+foo);
	        ImageIO.getImageReadersByMIMEType("image/jpeg");
			foo = Font.loadFont(CFXMobileMain.class.getResourceAsStream("fonts/Stratos_Web.ttf"), 12.0);
			System.err.println("Loaded font 2 "+foo);
//       	this.getAppLayout().setNavigationStyle(NavigationStyle.MOBILE);
			stage.setWidth(460);
			stage.setHeight(574);
			setStyle(stage.getScene(),FlexibleApplication.LIGHT_STYLE);
			stage.getScene().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

			System.err.println("2");
			getAppLayout().getNavigationPane().getSelectionModel().select(navCalls);
			System.err.println("2b");
			getAppLayout().getNavigationPane().setSettingsVisible(false);
			System.err.println("3");
     
			prepareXSILogin();
			System.err.println("4");
			login();
			System.err.println("5");
		} catch (Throwable e) {
			System.err.println(e.toString());
			logger.error("Error starting application",e);
			System.exit(1);
		}
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
				String user = null;
				String pass = null;
				logger.debug("Settings module present = "+settings.isPresent());
				if (settings.isPresent()) {
					user = settings.get().retrieve(CFGKEY_USER);
					pass = settings.get().retrieve(CFGKEY_PASS);
				}

				LoginDialog dialog = new LoginDialog(noLoginAttempts);
				dialog.setLogin(user);
				dialog.setPassword(pass);
				 Object foo = CFXMobileMain.this.showAndWait(dialog);
				 logger.warn("Returned "+foo);
				 if (foo!=CloseType.OK) {
					stop();
					 return null;
				 }
				 if (settings.isPresent()) {
					 settings.get().store(CFGKEY_USER, dialog.getLogin());
					 settings.get().store(CFGKEY_PASS, dialog.getPassword());
				 }
				noLoginAttempts++;
				return new PasswordAuthentication (dialog.getLogin(), dialog.getPassword().toCharArray());
			}
		});
    }

	//-------------------------------------------------------------------
	private void login() {
		String user = null;
		String pass = null;
		logger.debug("Settings module present = "+settings.isPresent());
		if (settings.isPresent()) {
			user = settings.get().retrieve(CFGKEY_USER);
			pass = settings.get().retrieve(CFGKEY_PASS);
		}
//		String user = "04212025933@qsc.de";
		String host = "web4.bmcag.com";
		
		while (user==null) {
			LoginDialog dialog = new LoginDialog(noLoginAttempts);
			dialog.setLogin(user);
			dialog.setPassword(pass);
			 Object foo = CFXMobileMain.this.showAndWait(dialog);
			 logger.warn("Returned "+foo);
			 if (foo!=CloseType.OK) {
				stop();
			 }
			 user = dialog.getLogin();
			 pass = dialog.getPassword();
			 if (settings.isPresent()) {
				 settings.get().store(CFGKEY_USER, dialog.getLogin());
				 settings.get().store(CFGKEY_PASS, dialog.getPassword());
			 }
		}
		
		Properties ccConfig = new Properties();
		ccConfig.setProperty(XSIConfigParameters.XSI_USER, user);
		ccConfig.setProperty(XSIConfigParameters.XSI_PASSWORD, pass);
		ccConfig.setProperty(XSIConfigParameters.XSI_HOST, host);
		ccConfig.setProperty(XSIConfigParameters.XSI_APPNAME, "CFXMobile");
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
		if (menuItem==navCalls) {
			try {
				page = ScreenLoader.loadCallPage(telephony);
				pageMap.put(navCalls, page);
			} catch (Exception e) {
				logger.error("Could not set up UI",e);
				stop();
			}
			return page;
		}
		if (menuItem==navContacts) {
			try {
				page = ScreenLoader.loadContactsPage(telephony);
				pageMap.put(navContacts, page);
			} catch (Exception e) {
				logger.error("Could not set up UI",e);
				stop();
			}
			return page;
		}
		
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