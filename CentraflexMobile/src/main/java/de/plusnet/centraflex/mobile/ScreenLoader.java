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
		((CallPageController)loader.getController()).setComponent(ret, telephony);
		return ret;
	}

//	//-------------------------------------------------------------------
//	public static Page loadSpellsPage() throws IOException {
//		FXMLLoader loader = new FXMLLoader(
//				CommLinkMain.class.getResource("fxml/SpellsPage.fxml"),
//				ResourceBundle.getBundle("de.rpgframework.shadowrun6.commlink.SpellsPage")
//				);
//		FXMLLoader.setDefaultClassLoader(FlexibleApplication.class.getClassLoader());
//		loader.setBuilderFactory(new ExtendedComponentBuilderFactory());
//		Page ret = loader.load();
//		((SpellsPageController)loader.getController()).setComponent(ret);
//		return ret;
//	}

//	//-------------------------------------------------------------------
//	public static Page loadBeastiaryPage() throws IOException {
//		FXMLLoader loader = new FXMLLoader(
//				CommLinkMain.class.getResource("fxml/BeastiaryPage.fxml"),
//				ResourceBundle.getBundle("de.rpgframework.splittermond.mondtor.BeastiaryPage")
//				);
//		FXMLLoader.setDefaultClassLoader(FlexibleApplication.class.getClassLoader());
//		loader.setBuilderFactory(new ExtendedComponentBuilderFactory());
//		Page ret = loader.load();
//		((BeastiaryPageController)loader.getController()).setComponent(ret);
//		return ret;
//	}

}
