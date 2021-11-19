package com.ur.urcap.ips.greifer;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.InstallationNodeService;
import com.ur.urcap.api.domain.URCapAPI;

import java.io.InputStream;

import com.ur.urcap.api.domain.data.DataModel;

public class MyDaemonInstallationNodeService implements InstallationNodeService {

	private final MyDaemonDaemonService daemonService;

	public MyDaemonInstallationNodeService(MyDaemonDaemonService daemonService) {
		this.daemonService = daemonService;
	}

	@Override
	public InstallationNodeContribution createInstallationNode(URCapAPI api, DataModel model) {
		return new MyDaemonInstallationNodeContribution(daemonService, model);
	}

	@Override
	public String getTitle() {
		return "Mein Demon getTitle()";
	}

	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("/com/ur/urcap/ips/greifer/installation.html");
		return is;
	}
}
