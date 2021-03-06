package com.ur.urcap.ips.greifer;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.DaemonService;

import java.net.MalformedURLException;
import java.net.URL;


public class IPSGreiferDaemonService implements DaemonService {

	private DaemonContribution daemonContribution;

	public IPSGreiferDaemonService() {
	}

	@Override
	public void init(DaemonContribution daemonContribution) {
		this.daemonContribution = daemonContribution;
		try {
			daemonContribution.installResource(new URL("file:com/ur/urcap/ips/greifer/daemon/"));
		} catch (MalformedURLException e) {	}
	}

	@Override
	public URL getExecutable() {
		try {
			// Two equivalent example daemons are available:
			return new URL("file:com/ur/urcap/ips/greifer/daemon/IPS_Greifer_RS485_Deamon.py"); // Python executable
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public DaemonContribution getDaemon() {
		return daemonContribution;
	}

}
