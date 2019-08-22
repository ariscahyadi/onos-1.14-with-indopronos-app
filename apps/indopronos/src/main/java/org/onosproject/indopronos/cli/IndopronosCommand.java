package org.onosproject.indopronos.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.intentsync.IntentSynchronizationService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.FilteredConnectPoint;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.MultiPointToSinglePointIntent;
import org.onosproject.net.intent.PointToPointIntent;
import org.onosproject.routing.bgp.BgpInfoService;
import org.onosproject.routing.bgp.BgpRouteEntry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * CLI to interact with the INDOPRONOS application.
 */

@Command(scope = "onos", name = "indopronos",
        description = "Manages the indopronos application")
public class IndopronosCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "communityLocalId",
            description = "Community attribute local identifier",
            required = true, multiValued = false)
    private String ActionCommunityLocalId = null;

    private static final String INDOPRONOS_APP = "org.onosproject.indopronos";

    private static final Integer PRIORITY = 250;

    private static final String SWITCH_DPID = "of:0000000000000001";
    private static final Integer PORT_NUMBER = 6;

    private static final FilteredConnectPoint EGRESS_POINT_TRIGGERED =
            new FilteredConnectPoint
                    (new ConnectPoint(
                            DeviceId.deviceId(SWITCH_DPID),
                            PortNumber.portNumber(PORT_NUMBER)));
    private static final MacAddress NEXT_HOP_MAC_TRIGGERED =
            MacAddress.valueOf("00:00:00:00:00:A3");


    @Override
    protected void execute() {

        BgpInfoService bgpService = AbstractShellCommand.get(BgpInfoService.class);
        IntentService intentService = AbstractShellCommand.get(IntentService.class);
        CoreService coreService = AbstractShellCommand.get(CoreService.class);
        IntentSynchronizationService intentSyncService =
                AbstractShellCommand.get(IntentSynchronizationService.class);

        Collection<BgpRouteEntry> routes4 = bgpService.getBgpRoutes4();
        Collection<BgpRouteEntry> routes6 = bgpService.getBgpRoutes6();

        Iterable<Intent> intents;
        intents = intentService.getIntents();

        ApplicationId AppId = coreService.getAppId(INDOPRONOS_APP);

        for (BgpRouteEntry route : routes4) {

            BgpRouteEntry.PathSegment firstPathSegment =
                    route.getAsPath().getPathSegments().get(0);
            long originatingAsn = firstPathSegment.getSegmentAsNumbers().get(0);
            log.debug("Found route prefix {} from AS {}",
                      route.prefix(), String.valueOf(originatingAsn));
            print("Found route prefix %s from AS %s",
                      route.prefix(), String.valueOf(originatingAsn));

            print ("Originating ASN %s", String.valueOf(originatingAsn));

            if (route.getCommunities() == null) {
                log.debug("No communities found route prefix {}",
                          route.prefix());
                continue;
            }

            ArrayList<BgpRouteEntry.Community> routeCommunities =
                        route.getCommunities().getCommunities();

            for (BgpRouteEntry.Community routeCommunity : routeCommunities) {

                Long communityAsn = routeCommunity.getCommunity().getLeft();
                Long communityLocalId = routeCommunity.getCommunity().getRight();

                print ("Originating ASN in Community %s", String.valueOf(communityAsn));
                print ("Route Community %s", String.valueOf(communityLocalId));

                if (String.valueOf(communityAsn).equals(String.valueOf(originatingAsn)) &&
                        (String.valueOf(communityLocalId).equals(ActionCommunityLocalId))) {

                    log.debug("Matching community for prefix {}",
                              route.prefix());
                    print("Matching community for prefix %s",
                              route.prefix().toString());
                    print("Action needed for prefix %s", route.prefix().toString());

                    MultiPointToSinglePointIntent routeIntent = getRouteIntent(route, intents);

                    // Build treatment: rewrite the destination MAC address
                    TrafficTreatment.Builder treatment = DefaultTrafficTreatment.builder()
                            .setEthDst(NEXT_HOP_MAC_TRIGGERED);

                    String intentKeyOne = String.valueOf(communityAsn) + ":"
                            + String.valueOf(communityLocalId) + "-One";
                    PointToPointIntent newIntentOne = PointToPointIntent.builder()
                            .appId(AppId)
                            .key(Key.of(intentKeyOne, AppId))
                            .filteredIngressPoint(routeIntent.filteredEgressPoint())
                            .filteredEgressPoint(EGRESS_POINT_TRIGGERED)
                            .selector(DefaultTrafficSelector.builder().build())
                            .treatment(treatment.build())
                            .priority(PRIORITY)
                            .build();
                    print("Intent need to be installed %s", newIntentOne.toString());
                    intentSyncService.submit(newIntentOne);

                    String intentKeyTwo = String.valueOf(communityAsn) + ":"
                            + String.valueOf(communityLocalId) + "-Two";
                    PointToPointIntent newIntentTwo = PointToPointIntent.builder()
                            .appId(AppId)
                            .key(Key.of(intentKeyTwo, AppId))
                            .filteredIngressPoint(EGRESS_POINT_TRIGGERED)
                            .filteredEgressPoint(routeIntent.filteredEgressPoint())
                            .selector(DefaultTrafficSelector.builder().build())
                            .treatment(routeIntent.treatment())
                            .priority(PRIORITY)
                            .build();
                print("Intent need to be installed %s", newIntentTwo.toString());
                intentSyncService.submit(newIntentTwo);

                } else {
                    log.debug("No matching community for prefix {}",
                              route.prefix());
                    print("No matching community for prefix {}",
                              route.prefix().toString());
                }
            }

        }

        for (BgpRouteEntry route : routes6) {

        }
    }


    /**
     * Get route multi-point-to-single-point intent from SDN IP.
     *
     * @param intents the community attribute to format
     * @return the Community as a string with colon
     */

    private MultiPointToSinglePointIntent getRouteIntent(BgpRouteEntry route,
                                                         Iterable<Intent> intents) {

        MultiPointToSinglePointIntent routeIntent = null;

        for (Intent intent : intents) {

            if (intent instanceof MultiPointToSinglePointIntent) {

                if (route.prefix().toString().equals(intent.key().toString())) {
                    routeIntent = (MultiPointToSinglePointIntent) intent;
                }
            }
        }
        return routeIntent;
    }



}
