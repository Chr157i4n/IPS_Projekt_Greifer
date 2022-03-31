package com.ur.urcap.ips.greifer;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
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
import com.ur.urcap.api.ui.component.InputRadioButton;
import javax.imageio.ImageIO;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class MyDaemonProgramNodeContribution implements ProgramNodeContribution {
	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;

	public MyDaemonProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}

	@Img(id = "logo")
	private ImgComponent logoImage;

	@Input(id = "custom_command_TextInput")
	private InputTextField custom_command_TextInput;

	@Input(id = "custom_command_TextInput")
	public void onCustomCommandInput(InputEvent event) {
		saveAllToModel();
	}

	@Select(id = "motor_power_Select")
	private SelectDropDownList motor_power_select;

	@Select(id = "motor_power_Select")
	public void onSelectChange(SelectEvent event) {
		saveAllToModel();
	}

	@Input(id = "command_Choice1")
	private InputRadioButton selectRadioButton1;

	@Input(id = "command_Choice1")
	public void onChoiceChange1(InputEvent event) {
		saveAllToModel();
	}

	@Input(id = "command_Choice2")
	private InputRadioButton selectRadioButton2;

	@Input(id = "command_Choice2")
	public void onChoiceChange2(InputEvent event) {
		saveAllToModel();
	}

	@Input(id = "command_Choice3")
	private InputRadioButton selectRadioButton3;

	@Input(id = "command_Choice3")
	public void onChoiceChange3(InputEvent event) {
		saveAllToModel();
	}

	@Input(id = "motor_drive_NumberInput")
	private InputTextField motor_drive_TextInput;

	@Input(id = "motor_drive_NumberInput")
	public void onMotorDriveInput(InputEvent event) {
		saveAllToModel();
	}

	

	private InputRadioButton[] inputRadioButtonArray = new InputRadioButton[3];


	public void showPic(String s) {
		String picName = s;
		// if (s == "0") {

		// }
		try {
			logoImage.setImage(ImageIO.read(getClass().getResource(picName)));
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void openView() {

		motor_power_select.removeAllItems();
		motor_power_select.addItem("An");
		motor_power_select.addItem("Aus");

		inputRadioButtonArray[0] = selectRadioButton1;
		inputRadioButtonArray[1] = selectRadioButton2;
		inputRadioButtonArray[2] = selectRadioButton3;

		loadAllFormModel();

		showPic("logo.png");

	}

	@Override
	public void closeView() {
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

		// writer.assign("mydaemon_message", getInstallation().getXMLRPCVariable() +
		// ".get_message(\"" + getName() + "\")");
		// writer.assign("mydaemon_title", getInstallation().getXMLRPCVariable() +
		// ".get_title()");
		// writer.appendLine("popup(mydaemon_message, mydaemon_title, False, False,
		// blocking=True)");

		writer.assign("ips_greifer_command", "\"" + getFromModel("custom_command_TextInput") + "\"");
		writer.appendLine(
				"returnValue = " + getInstallation().getXMLRPCVariable() + ".send_message(ips_greifer_command)");
		writer.appendLine("popup(returnValue, \"returnValue\", False, False, blocking=True)");

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
		System.out.println("saved key: "+key + "\t|\t" + "value: "+value);
		if (value.equals("")) {
			model.remove(key);
		} else {
			model.set(key, value);
		}
	}

	private void loadAllFormModel(){
		System.out.println("loading all from Model");
		custom_command_TextInput.setText(getFromModel("custom_command_TextInput"));
		motor_power_select.selectItemAtIndex(Integer.parseInt(getFromModel("motor_power_Select", "0")));
		motor_drive_TextInput.setText(getFromModel("motor_drive_TextInput", "0"));
		
		inputRadioButtonArray[Integer.parseInt(getFromModel("command_select_RadioButton", "0"))].setSelected();
	}

	private void saveAllToModel(){
		System.out.println("saving all to Model");
		setToModel("custom_command_TextInput", custom_command_TextInput.getText());
		setToModel("motor_power_Select", Integer.toString(motor_power_select.getSelectedIndex()));
		setToModel("motor_drive_TextInput", motor_drive_TextInput.getText());

		for(int i = 0; i < inputRadioButtonArray.length; i++){
			if(inputRadioButtonArray[i].isSelected())
			{
				setToModel("command_select_RadioButton", Integer.toString(i));
				break;
			}
		}
	}

	private MyDaemonInstallationNodeContribution getInstallation() {
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
