package de.plusnet.centraflex.mobile;

import java.io.ByteArrayInputStream;

import org.prelle.javafx.JavaFXConstants;
import org.prelle.javafx.SymbolIcon;

import de.centraflex.contacts.Contact;
import de.plusnet.centraflex.mobile.cells.ContactCell;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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

	private Button btnCall;
	private Button btnDelete;

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
		
		btnCall = new Button("Anrufen", new SymbolIcon("phone"));
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
		if (data==null) {
			lbName.setText(null);
			lbPhone.setText(null);
			iView.setImage(new Image(ContactCell.class.getResourceAsStream("person.png")));
		} else {
			lbName.setText(data.getDisplayName());
			lbPhone.setText(data.getPhoneNumber());
			if (data.getImage()!=null) {
				iView.setImage(new Image(new ByteArrayInputStream(data.getImage())));
			} else {
				iView.setImage(new Image(ContactCell.class.getResourceAsStream("person.png")));				
			}
		}
	}

}
