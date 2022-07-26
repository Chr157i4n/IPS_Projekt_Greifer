package com.ur.urcap.ips.greifer;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.variable.GlobalVariable;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.api.domain.variable.VariableException;
import com.ur.urcap.api.domain.variable.VariableFactory;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.annotation.Img;
import com.ur.urcap.api.ui.component.InputEvent;
import com.ur.urcap.api.ui.component.InputTextField;
import com.ur.urcap.api.ui.component.LabelComponent;
import com.ur.urcap.api.ui.component.ImgComponent;
import com.ur.urcap.api.ui.annotation.Select;
import com.ur.urcap.api.ui.component.SelectDropDownList;
import com.ur.urcap.api.ui.component.SelectEvent;
import com.ur.urcap.api.ui.component.HTMLComponent;

import com.ur.urcap.api.ui.component.InputRadioButton;
import javax.imageio.ImageIO;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class IPSGreiferProgramNodeContribution implements ProgramNodeContribution {
	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;
	private final ProgramAPI programAPI;
	private final VariableFactory variableFactory;

	public IPSGreiferProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
		this.programAPI = api.getProgramAPIProvider().getProgramAPI();
		variableFactory = programAPI.getVariableModel().getVariableFactory();
	}

	@Img(id = "logo")
	private ImgComponent logoImage;

	@Input(id = "custom_command_textinput")
	private InputTextField customCommandTextInput;

	@Input(id = "custom_command_textinput")
	public void onTextChange1(InputEvent event){
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			saveAllToModel();
		}
	}

	@Select(id = "motor_power_select")
	private SelectDropDownList motorPowerSelect;
	
	@Select(id = "motor_power_select")
	public void onDropDownChange1(SelectEvent event){
		saveAllToModel();		
	}

	@Input(id = "command_choice1")
	private InputRadioButton selectRadioButton1;

	@Input(id = "command_choice1")
	public void onChoiceChange1(InputEvent event) {
		changeChoice();
	}

	@Input(id = "command_choice2")
	private InputRadioButton selectRadioButton2;

	@Input(id = "command_choice2")
	public void onChoiceChange2(InputEvent event) {
		changeChoice();
	}

	@Input(id = "command_choice3")
	private InputRadioButton selectRadioButton3;

	@Input(id = "command_choice3")
	public void onChoiceChange3(InputEvent event) {
		changeChoice();
	}

	@Input(id = "command_choice4")
	private InputRadioButton selectRadioButton4;

	@Input(id = "command_choice4")
	public void onChoiceChange4(InputEvent event) {
		changeChoice();
	}

	@Input(id = "command_choice5")
	private InputRadioButton selectRadioButton5;

	@Input(id = "command_choice5")
	public void onChoiceChange5(InputEvent event) {
		changeChoice();
	}

	@Input(id = "command_choice6")
	private InputRadioButton selectRadioButton6;

	@Input(id = "command_choice6")
	public void onChoiceChange6(InputEvent event) {
		changeChoice();
	}

	@Input(id = "motor_drive_numberinput")
	private InputTextField motorDriveTextInput;
	
	@Input(id = "motor_drive_numberinput")
	public void onTextChange2(InputEvent event){
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			saveAllToModel();
		}
	}
	
	@Input(id = "motor_close_numberinput")
	private InputTextField motorCloseTextInput;

	@Input(id = "motor_close_numberinput")
	public void onTextChange3(InputEvent event){
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			int maxForce = Integer.parseInt(getInstallation().model.get("max_force", "0"));
			if(Integer.parseInt(motorCloseTextInput.getText()) > maxForce){
				motorCloseTextInput.setText(String.valueOf(maxForce));
			}
			saveAllToModel();
		}
	}

	@Input(id = "return_var_save_as")
	private InputTextField returnVarSaveAs;

	public void onTextChange4(InputEvent event){
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			saveAllToModel();
		}
	}

	@Label(id = "motor_close_label")
	private LabelComponent motorCloseLabel;
  
	private InputRadioButton[] inputRadioButtonArray = new InputRadioButton[6];
	private HTMLComponent[] deactivatableElementsArray = new HTMLComponent[4];

	private void changeChoice() {
		int choiceNumber = getSelectedRadioButtonIndex();
		for(int i = 0; i < deactivatableElementsArray.length; i++){
			deactivatableElementsArray[i].setEnabled(choiceNumber == i);
		}

		saveAllToModel();
	}

	private void showPic(String picName) {
		try {
			logoImage.setImage(ImageIO.read(getClass().getResource(picName)));
		} catch (java.io.IOException exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public void openView() {

		motorPowerSelect.removeAllItems();
		motorPowerSelect.addItem("Aus");
		motorPowerSelect.addItem("An");


		inputRadioButtonArray[0] = selectRadioButton1;
		inputRadioButtonArray[1] = selectRadioButton2;
		inputRadioButtonArray[2] = selectRadioButton3;
		inputRadioButtonArray[3] = selectRadioButton4;
		inputRadioButtonArray[4] = selectRadioButton5;
		inputRadioButtonArray[5] = selectRadioButton6;

		selectRadioButton1.setText("Befehl:");
		selectRadioButton2.setText("Motor Power:");
		selectRadioButton3.setText("Greiferbacken um");
		selectRadioButton4.setText("Greifer schließen:");
		selectRadioButton5.setText("Greifer etwas öffnen");
		selectRadioButton6.setText("Greifer komplett öffnen");
		motorCloseLabel.setText("N (max "+getInstallation().model.get("max_force", "0").toString()+" N)");

		deactivatableElementsArray[0] = customCommandTextInput;
		deactivatableElementsArray[1] = motorPowerSelect;
		deactivatableElementsArray[2] = motorDriveTextInput;
		deactivatableElementsArray[3] = motorCloseTextInput;

		//changeChoice(1);


		loadAllFormModel();

		showPic("logo.png");

	}

	@Override
	public void closeView() {
		saveAllToModel();
	}

	@Override
	public String getTitle() {
		return "IPS Greifer"; // todo: open or close can be added later
	}

	@Override
	public boolean isDefined() {
		return getInstallation().isDefined();// && !getName().isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls
		// Note, alternatively plain sockets can be used.

		int selectedCommandIndex = Integer.parseInt(getFromModel("command_select_RadioButton", "0"));
		if(selectedCommandIndex==0){	//custom command
			writer.appendLine("ips_greifer_return_value = " + getInstallation().getXMLRPCVariable() + ".send_message(\"" + getFromModel("customCommandTextInput") + "\")");
			//writer.appendLine("popup(ips_greifer_return_value, \"ips_greifer_return_value\", False, False, blocking=True)");
		}else if(selectedCommandIndex==1){	//motor power
			writer.appendLine("ips_greifer_return_value = " + getInstallation().getXMLRPCVariable() + ".send_message(\"M1" + (8-Integer.parseInt(getFromModel("motorPowerSelect"))) + "\")");
			//writer.appendLine("popup(ips_greifer_return_value, \"ips_greifer_return_value\", False, False, blocking=True)");
		}else if(selectedCommandIndex==2){	// motor move
			writer.appendLine("ips_greifer_return_value = " + getInstallation().getXMLRPCVariable() + ".send_message(\"G0 X" + getFromModel("motorDriveTextInput") + "\")");
			//writer.appendLine("popup(ips_greifer_return_value, \"ips_greifer_return_value\", False, False, blocking=True)");
		}else if(selectedCommandIndex==3){	// motor close
			writer.appendLine("ips_greifer_return_value = " + getInstallation().getXMLRPCVariable() + ".send_message(\"G2 F" + getFromModel("motorCloseTextInput") + "\")");
			//writer.appendLine("popup(ips_greifer_return_value, \"ips_greifer_return_value\", False, False, blocking=True)");
		}else if(selectedCommandIndex==4){	// motor open
			writer.appendLine("ips_greifer_return_value = " + getInstallation().getXMLRPCVariable() + ".send_message(\"G3\")");
			//writer.appendLine("popup(ips_greifer_return_value, \"ips_greifer_return_value\", False, False, blocking=True)");	
		}else if(selectedCommandIndex==5){	// motor open
			writer.appendLine("ips_greifer_return_value = " + getInstallation().getXMLRPCVariable() + ".send_message(\"G28\")");
			//writer.appendLine("popup(ips_greifer_return_value, \"ips_greifer_return_value\", False, False, blocking=True)");	
		}
		writer.appendLine("if ips_greifer_return_value == \"E10\":");
		writer.appendLine("\tpopup(\"Greiferfehler \" + ips_greifer_return_value + \" : Sensor Kommunikationsfehler\", \"Greifer Fehler\", False, True, blocking=True)");
		writer.appendLine("elif ips_greifer_return_value == \"E20\":");
		writer.appendLine("\tpopup(\"Greiferfehler \" + ips_greifer_return_value + \" : Befehl nicht verfügbar\", \"Greifer Fehler\", False, True, blocking=True)");
		writer.appendLine("elif ips_greifer_return_value == \"E30\":");
		writer.appendLine("\tpopup(\"Greiferfehler \" + ips_greifer_return_value + \" : Befehlsparameter fehlerhaft\", \"Greifer Fehler\", False, True, blocking=True)");
		writer.appendLine("elif ips_greifer_return_value == \"E40\":");
		writer.appendLine("\tpopup(\"Daemonfehler \" + ips_greifer_return_value + \" : Fehlerhafte Antwort\", \"Greifer Fehler\", False, True, blocking=True)");
		writer.appendLine("elif ips_greifer_return_value == \"E50\":");
		writer.appendLine("\tpopup(\"Daemonfehler \" + ips_greifer_return_value + \" : Keine Antwort\", \"Greifer Fehler\", False, True, blocking=True)");
		writer.appendLine("elif ips_greifer_return_value == \"E60\":");
		writer.appendLine("\tpopup(\"Daemonfehler \" + ips_greifer_return_value + \" : Kein Befehl festgelegt\", \"Greifer Fehler\", False, True, blocking=True)");
		writer.appendLine("elif ips_greifer_return_value == \"W10\":");
		writer.appendLine("\tpopup(\"Greiferfehler \" + ips_greifer_return_value + \" : Position außerhalb von Limit\", \"Greifer Warnung\", True, False, blocking=True)");
		writer.appendLine("elif str_at(ips_greifer_return_value,0) == \"E\":");
		writer.appendLine("\tpopup(\"Greiferfehler \" + ips_greifer_return_value + \" : unbekannter Fehler\", \"Greifer Fehler\", False, True, blocking=True)");
		writer.appendLine("elif str_at(ips_greifer_return_value,0) == \"W\":");
		writer.appendLine("\tpopup(\"Greiferwarnung \" + ips_greifer_return_value + \" : unbekannte Warnung\", \"Greifer Warnung\", True, False, blocking=True)");
		writer.appendLine("end");

		if(getFromModel("returnVarSaveAs", "")!=""){
			writer.appendLine("if str_at(ips_greifer_return_value,0) == \"A\" :");
			writer.assign("global "+getFromModel("returnVarSaveAs"), "to_num(str_sub(ips_greifer_return_value, 1))");
			writer.appendLine("else:");
			writer.assign("global "+getFromModel("returnVarSaveAs"), "-666");
			writer.appendLine("end");
		}

		// writer.writeChildren();
	}

	private String getFromModel(String key, String defaultValue) {
		String value = model.get(key, defaultValue);

		System.out.println("loaded key: "+key + "\t|\t" + "value: "+value);
		return value;

	}

	private String getFromModel(String key) {
		return getFromModel(key, "");
	}

	private void setToModel(String key, String value) {
		System.out.println("saved value: "+value+"\t|\tkey: "+key);
		if (value.equals("")) {
			model.remove(key);
		} else {
			model.set(key, value);
		}
	}

	private void loadAllFormModel(){
		System.out.println("loading all from Model");
		customCommandTextInput.setText(getFromModel("customCommandTextInput"));
		motorPowerSelect.selectItemAtIndex(Integer.parseInt(getFromModel("motorPowerSelect", "0")));
		motorDriveTextInput.setText(getFromModel("motorDriveTextInput", "0"));
		motorCloseTextInput.setText(getFromModel("motorCloseTextInput", "0"));
		returnVarSaveAs.setText(getFromModel("returnVarSaveAs", ""));
		
		inputRadioButtonArray[Integer.parseInt(getFromModel("command_select_RadioButton", "0"))].setSelected();
	}

	private void saveAllToModel(){
		if (getSelectedRadioButtonIndex() != -1) {
		System.out.println("saving all to Model");
		setToModel("customCommandTextInput", customCommandTextInput.getText());
		setToModel("motorPowerSelect", Integer.toString(motorPowerSelect.getSelectedIndex()));
		setToModel("motorDriveTextInput", motorDriveTextInput.getText());
		setToModel("motorCloseTextInput", motorCloseTextInput.getText());
		
		model.set("returnVarSaveAs", returnVarSaveAs.getText());
		

		setToModel("command_select_RadioButton", Integer.toString(getSelectedRadioButtonIndex()));
		}
	}

	private int getSelectedRadioButtonIndex() {
		for(int i = 0; i < inputRadioButtonArray.length; i++){
			if(inputRadioButtonArray[i].isSelected())
			{
				return i;
			}
		}
		return -1;
	}

	private IPSGreiferInstallationNodeContribution getInstallation() {
		return api.getInstallationNode(IPSGreiferInstallationNodeContribution.class);
	}
}
