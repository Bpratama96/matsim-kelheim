package org.matsim.run;

import com.google.common.collect.Sets;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.application.MATSimApplication;
import org.matsim.application.options.SampleOptions;
import org.matsim.application.prepare.CreateLandUseShp;
import org.matsim.application.prepare.freight.tripExtraction.ExtractRelevantFreightTrips;
import org.matsim.application.prepare.network.CreateNetworkFromSumo;
import org.matsim.application.prepare.population.*;
import org.matsim.application.prepare.pt.CreateTransitScheduleFromGtfs;
import org.matsim.contrib.drt.extension.DrtWithExtensionsConfigGroup;
import org.matsim.contrib.drt.extension.companions.DrtCompanionParams;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.vsp.scenario.SnzActivities;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.RoutingConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.extensions.pt.routing.ptRoutingModes.PtIntermodalRoutingModesConfigGroup;
import org.matsim.run.prepare.PrepareNetwork;
import org.matsim.run.prepare.PreparePopulation;
import org.matsim.simwrapper.SimWrapperConfigGroup;
import org.matsim.simwrapper.SimWrapperModule;
import picocli.CommandLine;
import playground.vsp.pt.fare.DistanceBasedPtFareParams;
import playground.vsp.pt.fare.PtFareConfigGroup;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;

@MATSimApplication.Prepare({
	CreateNetworkFromSumo.class, CreateTransitScheduleFromGtfs.class, TrajectoryToPlans.class, GenerateShortDistanceTrips.class,
	MergePopulations.class,  DownSamplePopulation.class, PrepareNetwork.class, ExtractHomeCoordinates.class,
	CreateLandUseShp.class, ResolveGridCoordinates.class, PreparePopulation.class, CleanPopulation.class, FixSubtourModes.class, SplitActivityTypesDuration.class
})

public class RunKelheimScenarioBen extends MATSimApplication {
	public static final String VERSION = "3.1";
	private static final double WEIGHT_1_PASSENGER = 16517.;
	private static final double WEIGHT_2_PASSENGER = 2084.;
	private static final double WEIGHT_3_PASSENGER = 532.;
	private static final double WEIGHT_4_PASSENGER = 163.;
	private static final double WEIGHT_5_PASSENGER = 20.;
	private static final double WEIGHT_6_PASSENGER = 5.;
	private static final double WEIGHT_7_PASSENGER = 0.;
	private static final double WEIGHT_8_PASSENGER = 0.;
	@CommandLine.Mixin
	private final SampleOptions sample = new SampleOptions(25, 10, 1);


	public RunKelheimScenarioBen(@Nullable Config config) {
		super(config);
	}

	public RunKelheimScenarioBen() {
		super(String.format("input/v%s/kelheim-v%s-config.xml", VERSION, VERSION));
	}

	public static void main(String[] args) {
		MATSimApplication.run(RunKelheimScenarioBen.class,args );
	}

	public static void addDrtCompanionParameters(DrtWithExtensionsConfigGroup drtWithExtensionsConfigGroup) {
		DrtCompanionParams drtCompanionParams = new DrtCompanionParams();
		drtCompanionParams.setDrtCompanionSamplingWeights(List.of(
			WEIGHT_1_PASSENGER,
			WEIGHT_2_PASSENGER,
			WEIGHT_3_PASSENGER,
			WEIGHT_4_PASSENGER,
			WEIGHT_5_PASSENGER,
			WEIGHT_6_PASSENGER,
			WEIGHT_7_PASSENGER,
			WEIGHT_8_PASSENGER
		));
		drtWithExtensionsConfigGroup.addParameterSet(drtCompanionParams);
	}
	protected void prepareScenario(Scenario scenario) {

		for (Link link : scenario.getNetwork().getLinks().values()) {
			Set<String> modes = link.getAllowedModes();

			// allow freight traffic together with cars
			if (modes.contains("car")) {
				Set<String> newModes = Sets.newHashSet(modes);
				newModes.add("freight");

				link.setAllowedModes(newModes);
			}
		}

	}
	protected Config prepareConfig(Config config) {

		SnzActivities.addScoringParams(config);

		config.controller().setOutputDirectory(sample.adjustName(config.controller().getOutputDirectory()));
		config.plans().setInputFile(sample.adjustName(config.plans().getInputFile()));
		config.controller().setRunId(sample.adjustName(config.controller().getRunId()));

		config.qsim().setFlowCapFactor(sample.getSize() / 100.0);
		config.qsim().setStorageCapFactor(sample.getSize() / 100.0);

		config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.abort);
		config.routing().setAccessEgressType(RoutingConfigGroup.AccessEgressType.accessEgressModeToLink);


		SimWrapperConfigGroup sw = ConfigUtils.addOrGetModule(config, SimWrapperConfigGroup.class);

		// Relative to config
		sw.defaultParams().shp = "../shp/dilutionArea.shp";
		sw.defaultParams().mapCenter = "11.89,48.91";
		sw.defaultParams().mapZoomLevel = 11d;
		sw.defaultParams().sampleSize = sample.getSample();

		PtFareConfigGroup ptFareConfigGroup = ConfigUtils.addOrGetModule(config, PtFareConfigGroup.class);
		DistanceBasedPtFareParams distanceBasedPtFareParams = ConfigUtils.addOrGetModule(config, DistanceBasedPtFareParams.class);

		ptFareConfigGroup.setApplyUpperBound(true);
		ptFareConfigGroup.setUpperBoundFactor(1.5);

		// Minimum fare (e.g. short trip or 1 zone ticket)
		distanceBasedPtFareParams.setMinFare(2.0);
		// Division between long trip and short trip (unit: m)
		distanceBasedPtFareParams.setLongDistanceTripThreshold(50000);
		// y = ax + b --> a value, for short trips
		distanceBasedPtFareParams.setNormalTripSlope(0.00017);
		// y = ax + b --> b value, for short trips
		distanceBasedPtFareParams.setNormalTripIntercept(1.6);
		// y = ax + b --> a value, for long trips
		distanceBasedPtFareParams.setLongDistanceTripSlope(0.00025);
		// y = ax + b --> b value, for long trips
		distanceBasedPtFareParams.setLongDistanceTripIntercept(30);
		return config;
	}
	protected void prepareControler(Controler controler) {
		Config config = controler.getConfig();
		Network network = controler.getScenario().getNetwork();
	}
}
