package com.ur.urcap.ips.greifer;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class IPSGreiferProgramNodeService implements ProgramNodeService {

	public IPSGreiferProgramNodeService() {
	}

	@Override
	public String getId() {
		return "ips_greifer";
	}

	@Override
	public String getTitle() {
		return "IPS Greifer";
	}

	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("/com/ur/urcap/ips/greifer/programnode.html");
		return is;
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public boolean isChildrenAllowed() {
		return false;
	}

	@Override
	public ProgramNodeContribution createNode(URCapAPI api, DataModel model) {
		return new IPSGreiferProgramNodeContribution(api, model);
	}
}
