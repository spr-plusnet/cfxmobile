module cfxmobile {
	exports de.plusnet.centraflex.mobile.cells;
	exports de.plusnet.centraflex.mobile;

	requires centraflex.telephony;
	requires centraflex.telephony.xsi;
	requires com.gluonhq.attach.settings;
	requires de.centraflex.libxsi;
	requires java.desktop;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.extensions;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.apache.logging.log4j;
	requires java.compiler;
	
	opens de.plusnet.centraflex.mobile to javafx.fxml;
	opens de.plusnet.centraflex.mobile.cells to javafx.fxml;
	
}
