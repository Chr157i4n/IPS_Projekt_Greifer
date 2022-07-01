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

public class MyDaemonInstallationNodeContribution implements InstallationNodeContribution {

	private static final String XMLRPC_VARIABLE = "my_daemon";
	private static final String ENABLED_KEY = "enabled";
	private static final String DEFAULT_VALUE = "HelloWorld";

	private DataModel model;
	private final MyDaemonDaemonService daemonService;
	private XmlRpcMyDaemonInterface xmlRpcDaemonInterface;
	private Timer uiTimer;

	public MyDaemonInstallationNodeContribution(MyDaemonDaemonService daemonService, DataModel model) {
		this.daemonService = daemonService;
		this.model = model;
		xmlRpcDaemonInterface = new XmlRpcMyDaemonInterface("127.0.0.1", 40405);
		applyDesiredDaemonStatus();
	}

	@Input(id = "txtFldSendMessage")
	private InputTextField sendMessageTextField;

	@Input(id = "txtFldmaxForce")
	private InputTextField maxForceTextField;

	@Input(id = "btnEnableDaemon")
	private InputButton enableDaemonButton;

	@Input(id = "btnDisableDaemon")
	private InputButton disableDaemonButton;

	@Input(id = "btnTestConnection")
	private InputButton testConnectionButton;

	@Input(id = "btnSendMessage")
	private InputButton sendMessageButton;

	@Input(id = "btnMotorOn")
	private InputButton motorOnButton;

	@Input(id = "btnMotorOff")
	private InputButton motorOffButton;

	@Label(id = "lblDaemonStatus")
	private LabelComponent daemonStatusLabel;

	@Label(id = "lblTestConnection")
	private LabelComponent testConnectionLabel;

	@Label(id = "lblSendMessage")
	private LabelComponent sendMessageLabel;

	@Label(id = "lblPositionValue")
	private LabelComponent positionValueLabel;

	@Label(id = "lblForceValue")
	private LabelComponent forceValueLabel;
	
	@Input(id = "btnEnableDaemon")
	public void onStartClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setDaemonEnabled(true);
			applyDesiredDaemonStatus();
		}
	}

	@Input(id = "btnDisableDaemon")
	public void onStopClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setDaemonEnabled(false);
			applyDesiredDaemonStatus();
		}
	}

	@Input(id = "btnTestConnection")
	public void onTestConnectionClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_PRESSED) {
			try {
				String value = xmlRpcDaemonInterface.testConnection();
				testConnectionLabel.setText(value);
			} catch(Exception e){
				System.err.println("Error while testing connection:\n"+e.toString());
			}
		}
	}

	@Input(id = "btnSendMessage")
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

	@Input(id = "txtFldmaxForce")
	public void onMaxForceChange(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setToModel("max_force", maxForceTextField.getText());
		}
	}
	
	@Input(id = "btnMotorOn")
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

	@Input(id = "btnMotorOff")
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
		System.out.println("Open View 1");
		enableDaemonButton.setText("Daemon starten");
		disableDaemonButton.setText("Daemon stoppen");
		testConnectionButton.setText("Testen");
		sendMessageButton.setText("Sende Nachricht");
		motorOnButton.setText("Motor An");
		motorOffButton.setText("Motor Aus");

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
		System.out.println("Open View 2");
	}

	private void updateUI() {
		System.out.println("Update UI 1");
		DaemonContribution.State state = DaemonContribution.State.RUNNING;
		//DaemonContribution.State state = getDaemonState();

		if (state == DaemonContribution.State.RUNNING) {
			enableDaemonButton.setEnabled(false);
			disableDaemonButton.setEnabled(true);
		} else {
			enableDaemonButton.setEnabled(true);
			disableDaemonButton.setEnabled(false);
		}

		enableDaemonButton.setEnabled(true);
		disableDaemonButton.setEnabled(true);

		String text = "";
		switch (state) {
		case RUNNING:
			text = "My Daemon runs";
			break;
		case STOPPED:
			text = "My Daemon stopped";
			break;
		case ERROR:
			text = "My Daemon failed";
			break;
		}
		daemonStatusLabel.setText(text);

		if(state == DaemonContribution.State.RUNNING){
			try {				
				String position = xmlRpcDaemonInterface.sendMessage("M44 S1");
				positionValueLabel.setText(position.substring(1));

				String force = xmlRpcDaemonInterface.sendMessage("M44 S3");
				forceValueLabel.setText(force.substring(1));
			} catch(Exception e){
				System.err.println("Error while updating position and force:\n"+e.toString());
			}
		}

		System.out.println("Update UI 2");
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
		// Apply the settings to the daemon on program start in the Installation pre-amble
		// writer.appendLine(XMLRPC_VARIABLE + ".set_title(\"" + getPopupTitle() + "\")");
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

	private DaemonContribution.State getDaemonState(){
		return daemonService.getDaemon().getState();
	}

	private Boolean isDaemonEnabled() {
		return model.get(ENABLED_KEY, true); //This daemon is enabled by default
	}

	private void setDaemonEnabled(Boolean enable) {
		model.set(ENABLED_KEY, enable);
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

	public XmlRpcMyDaemonInterface getXmlRpcDaemonInterface() {return xmlRpcDaemonInterface; }
}
