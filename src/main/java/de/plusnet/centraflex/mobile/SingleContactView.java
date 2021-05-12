package de.plusnet.centraflex.mobile;

import org.prelle.javafx.JavaFXConstants;
import org.prelle.javafx.SymbolIcon;

import de.centraflex.contacts.Contact;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * @author prelle
 *
 */
public class SingleContactView extends VBox {
	
	private Contact data;

	private ImageView iView;
	private SymbolIcon dummy;
	private Label lbName;
	private Label lbDepartment;
	private Label lbPhone;
	private Label lbMobile;

	
	//-------------------------------------------------------------------
	public SingleContactView(Contact data) {
		this.data = data;
		
		initComponents();
		initLayout();
		refresh();
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		iView  = new ImageView();
		dummy  = new SymbolIcon("Person");
		dummy.setStyle("-fx-font-isze: 800%");
		lbName = new Label();
		lbName.getStyleClass().add(JavaFXConstants.STYLE_HEADING4);
		lbDepartment = new Label();
		
		lbPhone = new Label(null, new SymbolIcon("phone"));
		lbMobile= new Label();
	}

	//-------------------------------------------------------------------
	private void initLayout() {
		setAlignment(Pos.TOP_CENTER);
		
		getChildren().add(iView);
		getChildren().add(lbName);
		getChildren().add(lbDepartment);
		
		TilePane tile = new TilePane(10, 10);
		tile.getChildren().addAll(lbPhone, lbMobile);
		getChildren().add(tile);
	}

	//-------------------------------------------------------------------
	private void refresh() {
		lbName.setText(data.getDisplayName());
		lbPhone.setText(data.getPhoneNumber());
	}

}
