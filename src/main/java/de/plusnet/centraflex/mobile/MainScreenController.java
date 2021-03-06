package de.plusnet.centraflex.mobile;

import java.io.IOException;

import org.prelle.javafx.AppLayout;
import org.prelle.javafx.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * @author prelle
 *
 */
public class MainScreenController {
	
	private final static Logger logger = LogManager.getLogger(MainScreenController.class);

	private transient AppLayout screen;

	//-------------------------------------------------------------------
	public MainScreenController() {
	}

	//-------------------------------------------------------------------
	@FXML
	public void initialize() {
	}

	//-------------------------------------------------------------------
	public void setComponent(AppLayout screen) {
		this.screen = screen;
		try {
			Page page = ScreenLoader.loadCallPage(null);
			screen.navigateTo(page, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	@FXML
	public void navigateCharacters(ActionEvent ev) {
		logger.debug("Navigate Characters");
	}

	//-------------------------------------------------------------------
	@FXML
	public void navigateLibraries(ActionEvent ev) {
		logger.debug("Navigate Libraries");
		try {
			Page page = ScreenLoader.loadCallPage(null);

			screen.navigateTo(page, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
