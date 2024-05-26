package org.matsim.analysis.analysisHomework;

import org.matsim.core.events.EventsUtils;

public class StartAnalysis {
	public static void main(String[] args) {
	var handler = new LinkCounterHandler();
	var manager = EventsUtils.createEventsManager();
	manager.addHandler(handler);
	EventsUtils.readEvents(manager,"/Users/benedictusadriantopratama/Downloads/kelheim-v3.1-25pct.0.events.xml.gz");

	System.out.println(handler.counter);
	}
}
