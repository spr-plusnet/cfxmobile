package de.plusnet.centraflex.mobile.cells;

import java.io.ByteArrayInputStream;
import java.util.Random;

import org.prelle.javafx.Persona;
import org.prelle.javafx.Persona.Size;

import de.centraflex.contacts.Contact;
import de.centraflex.telephony.TelephonyService;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;

public class ContactCell extends ListCell<Contact> {
	
	private final static Random RANDOM = new Random();
	
	private Persona persona;

	//-------------------------------------------------------------------
	public ContactCell() {
		persona = new Persona(Size.SIZE_48, null);
	}

	//-------------------------------------------------------------------
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	public void updateItem(Contact item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			persona.setText(item.getDisplayName());
			persona.setSecondaryText(item.getPhoneNumber());
			if (item.getImage()!=null) {
				persona.setImage(new Image(new ByteArrayInputStream(item.getImage())));
			} else {
				String file = "face_"+(RANDOM.nextBoolean()?"m":"f")+(RANDOM.nextInt(7)+1)+".jpg";
				file = "face_m"+(RANDOM.nextInt(7)+1)+".jpg";
				Image image = new Image(ContactCell.class.getResourceAsStream(file));
				persona.setImage(image);
			}
			setGraphic(persona);
		}
	}
}