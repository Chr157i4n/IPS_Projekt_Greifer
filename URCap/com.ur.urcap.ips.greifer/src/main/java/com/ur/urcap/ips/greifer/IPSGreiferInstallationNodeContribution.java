package com.ur.urcap.ips.greifer;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.component.InputButton;
import com.ur.urcap.api.ui.component.InputEvent;
import com.ur.urcap.api.ui.component.InputTextField;
import com.ur.urcap.api.ui.component.LabelComponent;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;

public class IPSGreiferInstallationNodeContribution implements InstallationNodeContribution {

	private static final String XMLRPC_VARIABLE = "ips_greifer_daemon";

	public DataModel model;
	private final IPSGreiferDaemonService daemonService;
	private XmlRpcIPSGreiferInterface xmlRpcDaemonInterface;
	private Timer uiTimer;

	public IPSGreiferInstallationNodeContribution(IPSGreiferDaemonService daemonService, DataModel model) {
		this.daemonService = daemonService;
		this.model = model;
		xmlRpcDaemonInterface = new XmlRpcIPSGreiferInterface("127.0.0.1", 40405);
		applyDesiredDaemonStatus();
	}

	@Input(id = "max_force")
	private InputTextField maxForceTextField;

	@Input(id = "enable_daemon")
	private InputButton enableDaemonButton;

	@Input(id = "disable_daemon")
	private InputButton disableDaemonButton;

	@Input(id = "send_message_txtfld")
	private InputTextField sendMessageTextField;

	@Input(id = "send_message_btn")
	private InputButton sendMessageButton;

	@Label(id = "send_message_lbl")
	private LabelComponent sendMessageLabel;

	@Input(id = "motor_on")
	private InputButton motorOnButton;

	@Input(id = "motor_off")
	private InputButton motorOffButton;

	@Label(id = "daemon_status")
	private LabelComponent daemonStatusLabel;

	@Label(id = "gripper_status")
	private LabelComponent gripperStatusLabel;

	@Label(id = "position_value")
	private LabelComponent positionValueLabel;

	@Label(id = "force_value")
	private LabelComponent forceValueLabel;
	
	@Input(id = "enable_daemon")
	public void onStartClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setDaemonEnabled(true);
			applyDesiredDaemonStatus();
		}
	}

	@Input(id = "disable_daemon")
	public void onStopClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setDaemonEnabled(false);
			applyDesiredDaemonStatus();
		}
	}

	@Input(id = "send_message_btn")
	public void onMessageSendClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_PRESSED) {
			try {
				String value = xmlRpcDaemonInterface.sendMessage(sendMessageTextField.getText());
				sendMessageLabel.setText(value);
			} catch(Exception e){
				System.err.println("Error while sending message:\n"+e.toString());
			}
		}
	}

	@Input(id = "max_force")
	public void onMaxForceChange(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setToModel("max_force", maxForceTextField.getText());
		}
	}
	
	@Input(id = "motor_on")
	public void onMotorOnClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_PRESSED) {
			try {
				String value = xmlRpcDaemonInterface.sendMessage("M17");
				sendMessageLabel.setText(value);
			} catch(Exception e){
				System.err.println("Error while sending message:\n"+e.toString());
			}
		}
	}

	@Input(id = "motor_off")
	public void onMotorOffClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_PRESSED) {
			try {
				String value = xmlRpcDaemonInterface.sendMessage("M18");
				sendMessageLabel.setText(value);
			} catch(Exception e){
				System.err.println("Error while sending message:\n"+e.toString());
			}
		}
	}

	@Override
	public void openView() {
		System.out.println("Open View");
		enableDaemonButton.setText("Daemon starten");
		disableDaemonButton.setText("Daemon stoppen");
		sendMessageButton.setText("Sende Nachricht");
		motorOnButton.setText("Motor An");
		motorOffButton.setText("Motor Aus");
		maxForceTextField.setText(model.get("max_force", "0").toString());

		//UI updates from non-GUI threads must use EventQueue.invokeLater (or SwingUtilities.invokeLater)
		uiTimer = new Timer(true);
		uiTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateUI();
					}
				});
			}
		}, 0, 1000);
	}

	private void updateUI() {
		System.out.println("Update UI");
		boolean daemonRunning = false;
		try {				
			String answer = xmlRpcDaemonInterface.testConnection();
			daemonRunning = true;
		} catch(Exception e){
			System.err.println("Error while getting daemon state");
			daemonRunning = false;
		}		

		if (daemonRunning) {
			enableDaemonButton.setEnabled(false);
			disableDaemonButton.setEnabled(true);
			daemonStatusLabel.setText("Daemonstatus:  läuft");
		} else {
			enableDaemonButton.setEnabled(true);
			disableDaemonButton.setEnabled(false);
			daemonStatusLabel.setText("Daemonstatus:  läuft nicht");
		}

		if(daemonRunning){
			try {				
				String position = xmlRpcDaemonInterface.sendMessage("M44 S1");
				if(position.substring(0,1) == "A") {
					positionValueLabel.setText(position.substring(1));
				}

				String force = xmlRpcDaemonInterface.sendMessage("M44 S3");
				if(position.substring(0,1) == "A"){
					forceValueLabel.setText(force.substring(1));
				}

				gripperStatusLabel.setText("Greiferstatus: verbunden");
			} catch(Exception e){
				System.err.println("Error while updating position and force:\n"+e.toString());
				gripperStatusLabel.setText("Greiferstatus: nicht verbunden");
			}
		} else {
			gripperStatusLabel.setText("Greiferstatus: nicht verbunden");
		}
	}

	@Override
	public void closeView() {
		if (uiTimer != null) {
			uiTimer.cancel();
		}
	}

	public boolean isDefined() {
		return true;//getDaemonState() == DaemonContribution.State.RUNNING;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.globalVariable(XMLRPC_VARIABLE, "rpc_factory(\"xmlrpc\", \"http://127.0.0.1:40405/RPC2\")");
	}

	private void applyDesiredDaemonStatus() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (isDaemonEnabled()) {
					try {
						daemonService.getDaemon().start();
					} catch (Exception e) {
						System.err.println("Error while starting the Daemon");
					}
				} else {
					daemonService.getDaemon().stop();
				}
			}
		}).start();
	}

	private Boolean isDaemonEnabled() {
		return model.get("enabled", true); //This daemon is enabled by default
	}

	private void setDaemonEnabled(Boolean enable) {
		model.set("enabled", enable);
	}

	public String getXMLRPCVariable(){
		return XMLRPC_VARIABLE;
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

	public XmlRpcIPSGreiferInterface getXmlRpcDaemonInterface() {return xmlRpcDaemonInterface; }
}
