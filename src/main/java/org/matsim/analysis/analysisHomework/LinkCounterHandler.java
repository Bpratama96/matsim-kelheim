package org.matsim.analysis.analysisHomework;

import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;

public class LinkCounterHandler implements LinkEnterEventHandler {
	public int counter = 0;

	@Override
	public void handleEvent(LinkEnterEvent linkEnterEvent) {
		counter++;
	}
}
