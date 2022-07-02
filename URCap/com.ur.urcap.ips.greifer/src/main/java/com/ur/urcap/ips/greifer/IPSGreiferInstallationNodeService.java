package com.ur.urcap.ips.greifer;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.InstallationNodeService;
import com.ur.urcap.api.domain.URCapAPI;

import java.io.InputStream;

import com.ur.urcap.api.domain.data.DataModel;

public class IPSGreiferInstallationNodeService implements InstallationNodeService {

	private final IPSGreiferDaemonService daemonService;

	public IPSGreiferInstallationNodeService(IPSGreiferDaemonService daemonService) {
		this.daemonService = daemonService;
	}

	@Override
	public InstallationNodeContribution createInstallationNode(URCapAPI api, DataModel model) {
		return new IPSGreiferInstallationNodeContribution(daemonService, model);
	}

	@Override
	public String getTitle() {
		return "IPS Greifer";
	}

	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("/com/ur/urcap/ips/greifer/installationnode.html");
		return is;
	}
}
