package org.matsim.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkUtils;

public class Highway_Script {
	public static void main(String[] args) {

		// use a utils class to load the file
		var network = NetworkUtils.readNetwork("G:\\TU Stuff\\MatSim\\HA1\\kelheim-v3.0-network-with-pt.xml");

		// then change something
		for (Link link : network.getLinks().values()) {
			if (link.getId().equals(Id.createLinkId("pt_44728")) || link.getId().equals(Id.createLinkId("pt_45039"))){
				link.setFreespeed(33.);
			}
			if (link.getId().equals(Id.createLinkId("pt_44726")) || link.getId().equals(Id.createLinkId("pt_44925"))){
				link.setFreespeed(33.);
			}
			if (link.getId().equals(Id.createLinkId("pt_45040")) || link.getId().equals(Id.createLinkId("pt_44727"))){
				link.setFreespeed(33.);
			}
			if (link.getId().equals(Id.createLinkId("pt_45058")) || link.getId().equals(Id.createLinkId("pt_45041"))){
				link.setFreespeed(33.);
			}
		}

		// use a utils class to write the result into a file
		NetworkUtils.writeNetwork(network, "G:\\TU Stuff\\MatSim\\HA1\\kelheim-v3.0-network-with-pt-bearbeitet.xml");
	}
}
