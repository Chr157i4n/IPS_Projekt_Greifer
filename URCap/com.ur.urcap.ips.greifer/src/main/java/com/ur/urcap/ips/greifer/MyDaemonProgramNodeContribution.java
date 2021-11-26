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
import javax.imageio.ImageIO;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class MyDaemonProgramNodeContribution implements ProgramNodeContribution {
	private static final String NAME = "name";
	private static final String COMMAND = "r0";

	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;

	public MyDaemonProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}

	@Input(id = "yourname")
	private InputTextField nameTextField;

	@Input(id = "command")
	private InputTextField commandTextField;

	@Label(id = "titlePreviewLabel")
	private LabelComponent titlePreviewLabel;

	@Label(id = "messagePreviewLabel")
	private LabelComponent messagePreviewLabel;



	@Img(id = "logo")
	private ImgComponent logoImage;


	@Input(id = "yourname")
	public void onInput(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setName(nameTextField.getText());
			updatePreview();
		}
	}

	@Input(id = "command")
	public void onCommandInput(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setCommand(commandTextField.getText());
			updatePreview();
		}
	}


	public void showPic(String s) {
		String picName = s;
	//	if (s == "0") {

	//	}
		try{
			logoImage.setImage(ImageIO.read(getClass().getResource(picName)));
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void openView() {
		nameTextField.setText(getName());
		commandTextField.setText(getCommand());

		showPic("logo.png");

		//UI updates from non-GUI threads must use EventQueue.invokeLater (or SwingUtilities.invokeLater)
		uiTimer = new Timer(true);
		uiTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updatePreview();
					}
				});
			}
		}, 0, 1000);
	}

	@Override
	public void closeView() {
		uiTimer.cancel();
	}

	@Override
	public String getTitle() {
		return "IPS Greifer"; //todo: open or close can be added later
	}

	
		
	

	@Override
	public boolean isDefined() {
		return getInstallation().isDefined();// && !getName().isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls
		// Note, alternatively plain sockets can be used.
		
		//writer.assign("mydaemon_message", getInstallation().getXMLRPCVariable() + ".get_message(\"" + getName() + "\")");
		//writer.assign("mydaemon_title", getInstallation().getXMLRPCVariable() + ".get_title()");
		//writer.appendLine("popup(mydaemon_message, mydaemon_title, False, False, blocking=True)");
		
		writer.assign("ips_greifer_command", "\"" + getCommand() + "\"");
		writer.appendLine("returnValue = " + getInstallation().getXMLRPCVariable() + ".send_message(ips_greifer_command)");
		writer.appendLine("popup(returnValue, \"returnValue\", False, False, blocking=True)");


		//writer.writeChildren();
	}

	private void updatePreview() {
		String title = "";
		String message = "";
		try {
			// Provide a real-time preview of the daemon state
			title = getInstallation().getXmlRpcDaemonInterface().getTitle();
			message = getInstallation().getXmlRpcDaemonInterface().getMessage(getName());
			//logoImg.setImage(ImageIO.read(getClass().getResource("icon.png")));
		} catch (Exception e) {
			System.err.println("Could not retrieve essential data from the daemon process for the preview.");
			title = message = "<Daemon disconnected>";
		}

		titlePreviewLabel.setText(title);
		messagePreviewLabel.setText(message);
	}

	private String getName() {
		return model.get(NAME, "");
	}

	private void setName(String name) {
		if ("".equals(name)){
			model.remove(NAME);
		}else{
			model.set(NAME, name);
		}
	}

	private String getCommand() {
		return model.get(COMMAND, "");
	}

	private void setCommand(String command) {
		if ("".equals(command)){
			model.remove(COMMAND);
		}else{
			model.set(COMMAND, command);
		}
	}

	private MyDaemonInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(MyDaemonInstallationNodeContribution.class);
	}

}
