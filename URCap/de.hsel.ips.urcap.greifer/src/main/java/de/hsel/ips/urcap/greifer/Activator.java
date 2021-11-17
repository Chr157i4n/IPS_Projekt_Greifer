package de.hsel.ips.urcap.greifer;

import com.ur.urcap.api.contribution.driver.gripper.GripperContribution;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.ur.urcap.api.contribution.InstallationNodeService;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.contribution.DaemonService;

public class Activator implements BundleActivator {

	@Override
	public void start(final BundleContext context) {
		
		MyDaemonDaemonService daemonService = new MyDaemonDaemonService();
		MyDaemonInstallationNodeService installationNodeService = new MyDaemonInstallationNodeService(daemonService);

		context.registerService(InstallationNodeService.class, installationNodeService, null);
		context.registerService(ProgramNodeService.class, new MyDaemonProgramNodeService(), null);
		context.registerService(DaemonService.class, daemonService, null);
		context.registerService(GripperContribution.class, new SimpleGripper(), null);
	}

	@Override
	public void stop(BundleContext context) {
	}
}

