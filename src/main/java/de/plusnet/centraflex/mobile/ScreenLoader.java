package de.plusnet.centraflex.mobile;

import java.io.IOException;
import java.util.ResourceBundle;

import org.prelle.javafx.AppLayout;
import org.prelle.javafx.ExtendedComponentBuilderFactory;
import org.prelle.javafx.FlexibleApplication;
import org.prelle.javafx.Page;

import de.centraflex.telephony.TelephonyService;
import javafx.fxml.FXMLLoader;

/**
 * @author prelle
 *
 */
public class ScreenLoader {

	//-------------------------------------------------------------------
	public static AppLayout loadMainScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(
				CFXMobileMain.class.getResource("fxml/MainScreen.fxml"),
				ResourceBundle.getBundle(CFXMobileMain.class.getName())
				);
		FXMLLoader.setDefaultClassLoader(FlexibleApplication.class.getClassLoader());
		loader.setBuilderFactory(new ExtendedComponentBuilderFactory());
		AppLayout ret = loader.load();
		((MainScreenController)loader.getController()).setComponent(ret);
		return ret;
	}

	//-------------------------------------------------------------------
	public static Page loadCallPage(TelephonyService telephony) throws IOException {
		FXMLLoader loader = new FXMLLoader(
				CFXMobileMain.class.getResource("fxml/CallPage.fxml"),
				ResourceBundle.getBundle("de.plusnet.centraflex.mobile.CallPage")
				);
		FXMLLoader.setDefaultClassLoader(FlexibleApplication.class.getClassLoader());
		loader.setBuilderFactory(new ExtendedComponentBuilderFactory());
		Page ret = loader.load();
		ret.setId("calls");
		((CallPageController)loader.getController()).setComponent(ret, telephony);
		return ret;
	}

	//-------------------------------------------------------------------
	public static Page loadContactsPage(TelephonyService telephony) throws IOException {
		FXMLLoader loader = new FXMLLoader(
				CFXMobileMain.class.getResource("fxml/ContactsPage.fxml"),
				ResourceBundle.getBundle("de.plusnet.centraflex.mobile.ContactsPage")
				);
		FXMLLoader.setDefaultClassLoader(FlexibleApplication.class.getClassLoader());
		loader.setBuilderFactory(new ExtendedComponentBuilderFactory());
		Page ret = loader.load();
		ret.setId("contacts");
		((ContactsPageController)loader.getController()).setComponent(ret, telephony);
		return ret;
	}

}
