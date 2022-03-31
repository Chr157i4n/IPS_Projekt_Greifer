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
	private static final String POPUPTITLE_KEY = "popuptitle";

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

	@Input(id = POPUPTITLE_KEY)
	private InputTextField popupTitleField;

	@Input(id = "txtFldSendMessage")
	private InputTextField sendMessageTextField;

	@Input(id = "btnEnableDaemon")
	private InputButton enableDaemonButton;

	@Input(id = "btnDisableDaemon")
	private InputButton disableDaemonButton;

	@Input(id = "btnmeasurementValue")
	private InputButton measurementValueButton;

	@Input(id = "btnSendMessage")
	private InputButton sendMessageButton;

	@Input(id = "btnMotorOn")
	private InputButton motorOnButton;

	@Input(id = "btnMotorOff")
	private InputButton motorOffButton;

	@Label(id = "lblDaemonStatus")
	private LabelComponent daemonStatusLabel;

	@Label(id = "lblmeasurementValue")
	private LabelComponent measurementValueLabel;

	@Label(id = "lblSendMessage")
	private LabelComponent sendMessageLabel;
	

	@Input(id = POPUPTITLE_KEY)
	public void onMessageChange(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setPopupTitle(popupTitleField.getText());
		}
	}

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

	@Input(id = "btnmeasurementValue")
	public void onMeasureClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_PRESSED) {
			try {
				String value = xmlRpcDaemonInterface.getMeasurementValueTest("channelTEST");
				measurementValueLabel.setText(value);
			} catch(Exception e){
				System.err.println("Could not get a measurement value:\n"+e.toString());
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
		enableDaemonButton.setText("Start Daemon");
		disableDaemonButton.setText("Stop daemon");
		measurementValueButton.setText("Messen");
		sendMessageButton.setText("Send Message");
		motorOnButton.setText("Motor On");
		motorOffButton.setText("Motor Off");
		popupTitleField.setText(getPopupTitle());

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
		DaemonContribution.State state = DaemonContribution.State.RUNNING;//getDaemonState();

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
		writer.appendLine(XMLRPC_VARIABLE + ".set_title(\"" + getPopupTitle() + "\")");
	}

	public String getPopupTitle() {
		if (!model.isSet(POPUPTITLE_KEY)) {
			resetToDefaultValue();
		}
		return model.get(POPUPTITLE_KEY, DEFAULT_VALUE);
	}

	private void setPopupTitle(String title) {
		if ("".equals(title)) {
			resetToDefaultValue();
		} else {
			model.set(POPUPTITLE_KEY, title);
			// Apply the new setting to the daemon for real-time preview purposes
			// Note this might influence a running program, since the actual state is stored in the daemon.
			setDaemonTitle(title);
		}
	}

	private void resetToDefaultValue() {
		popupTitleField.setText(DEFAULT_VALUE);
		model.set(POPUPTITLE_KEY, DEFAULT_VALUE);
		setDaemonTitle(DEFAULT_VALUE);
	}

	private void setDaemonTitle(String title) {
		try {
			xmlRpcDaemonInterface.setTitle(title);
		} catch(Exception e){
			System.err.println("Could not set the title in the daemon process.");
		}
	}

	private void applyDesiredDaemonStatus() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (isDaemonEnabled()) {
					// Download the daemon settings to the daemon process on initial start for real-time preview purposes
					try {
						awaitDaemonRunning(5000);
						xmlRpcDaemonInterface.setTitle(getPopupTitle());
					} catch (Exception e) {
						System.err.println("Could not set the title in the daemon process.");
					}
				} else {
					daemonService.getDaemon().stop();
				}
			}
		}).start();
	}

	private void awaitDaemonRunning(long timeOutMilliSeconds) throws InterruptedException {
		daemonService.getDaemon().start();
		long endTime = System.nanoTime() + timeOutMilliSeconds * 1000L * 1000L;
		while(System.nanoTime() < endTime && (daemonService.getDaemon().getState() != DaemonContribution.State.RUNNING || !xmlRpcDaemonInterface.isReachable())) {
			Thread.sleep(100);
		}
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

	public XmlRpcMyDaemonInterface getXmlRpcDaemonInterface() {return xmlRpcDaemonInterface; }
}
