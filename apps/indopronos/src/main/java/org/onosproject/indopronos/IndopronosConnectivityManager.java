/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.indopronos;

import org.onlab.packet.MacAddress;
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
import org.onosproject.routeservice.ResolvedRoute;
import org.onosproject.routeservice.RouteEvent;
import org.onosproject.routeservice.RouteListener;
import org.onosproject.routeservice.RouteService;
import org.onosproject.routing.bgp.BgpInfoService;
import org.onosproject.routing.bgp.BgpRouteEntry;
import org.onosproject.routing.bgp.RouteEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * FIB component of INDOPRONOS Application.
 */

public class IndopronosConnectivityManager {

    // Condition to execute policy routing
    private static final String ACTION_COMMUNITY_LOCAL_ID = "100";

    // Intent Priority to override MP2SP of SDN-IP
    private static final Integer PRIORITY = 250;

    // Premium Link / Private Peer Definition
    private static final String SWITCH_DPID = "of:0000000000000001";
    private static final Integer PORT_NUMBER = 6;
    private static final FilteredConnectPoint EGRESS_POINT_PRIVATE =
            new FilteredConnectPoint
                    (new ConnectPoint(
                            DeviceId.deviceId(SWITCH_DPID),
                            PortNumber.portNumber(PORT_NUMBER)));
    private static final MacAddress NEXT_HOP_MAC_PRIVATE =
            MacAddress.valueOf("00:00:00:00:00:A3");

    private final BgpInfoService bgpService;
    private final IntentService intentService;
    private final CoreService coreService;
    private final IntentSynchronizationService intentSyncService;
    private final RouteService routeService;

    private static final Logger log = LoggerFactory.getLogger(
            IndopronosConnectivityManager.class);

    private final ApplicationId appId;

    private final InternalRouteListener routeListener = new InternalRouteListener();

    /**
     * Creates a new Indopronos ConnectivityManager.
     *
     * @param appId             the application ID
     * @param bgpService        the BGP service
     * @param coreService       the core Service
     * @param intentService     the intent service
     * @param intentSyncService the intent synchronizer service
     */
    public IndopronosConnectivityManager(ApplicationId appId,
                                         BgpInfoService bgpService,
                                         CoreService coreService,
                                         IntentService intentService,
                                         IntentSynchronizationService intentSyncService,
                                         RouteService routeService) {
        this.appId = appId;
        this.bgpService = bgpService;
        this.coreService = coreService;
        this.intentService = intentService;
        this.intentSyncService = intentSyncService;
        this.routeService = routeService;
    }

    /**
     * Starts the Indopronos connectivity manager.
     */
    public void start() {
        routeService.addListener(routeListener);

        Collection<BgpRouteEntry> routes4 = bgpService.getBgpRoutes4();
        Collection<BgpRouteEntry> routes6 = bgpService.getBgpRoutes6();

        for (BgpRouteEntry route : routes4) {
            setUpConnectivity(route);
        }

        for (BgpRouteEntry route : routes6) {
            setUpConnectivity(route);
        }
    }

    /**
     * Stops the Indopronos connectivity manager.
     */
    public void stop() {
        routeService.removeListener(routeListener);
    }

    /**
     * Stops the Indopronos connectivity manager.
     */
    public void update(ResolvedRoute resolvedRoute) {

        log.info("Resolved route: {}", resolvedRoute.prefix());

        Iterable<Intent> intents = intentService.getIntents();

        for (Intent intent : intents) {
            if (intent.appId().equals(appId)) {
                intentSyncService.withdraw(intent);
            }
        }

        Collection<BgpRouteEntry> routes4 = bgpService.getBgpRoutes4();
        Collection<BgpRouteEntry> routes6 = bgpService.getBgpRoutes6();

        for (BgpRouteEntry route : routes4) {

            if (route.prefix().equals(resolvedRoute.prefix())) {
                setUpConnectivity(route);
            }

        }

        for (BgpRouteEntry route : routes6) {

            if (route.prefix().equals(resolvedRoute.prefix())) {
                setUpConnectivity(route);
            }
        }

    }

    /**
     * Sets up paths to override the connectivity between BGP router who send the
     * community and private peer BGP router.
     */
    private void setUpConnectivity(BgpRouteEntry route) {

        //Collection<BgpRouteEntry> routes4 = bgpService.getBgpRoutes4();
        //Collection<BgpRouteEntry> routes6 = bgpService.getBgpRoutes6();

        Iterable<Intent> intents;
        intents = intentService.getIntents();

            if (route.getCommunities() == null) {
                log.info("No communities found in prefix {}", route.prefix());
                return;
            }

            BgpRouteEntry.PathSegment firstPathSegment =
                    route.getAsPath().getPathSegments().get(0);
            long originatingAsn = firstPathSegment.getSegmentAsNumbers().get(0);
            log.info("Prefix {} originated from AS {}", route.prefix(), originatingAsn);

            ArrayList<BgpRouteEntry.Community> routeCommunities =
                    route.getCommunities().getCommunities();

            for (BgpRouteEntry.Community routeCommunity : routeCommunities) {

                Long communityAsn = routeCommunity.getCommunity().getLeft();
                Long communityLocalId = routeCommunity.getCommunity().getRight();

                if (String.valueOf(communityAsn).equals(String.valueOf(originatingAsn)) &&
                        (String.valueOf(communityLocalId).equals(ACTION_COMMUNITY_LOCAL_ID))) {

                    log.info("Matched community ASN and Local ID for prefix {}", route.prefix());
                    MultiPointToSinglePointIntent routeIntent = getRouteIntent(route, intents);

                    // Build treatment: rewrite the destination MAC address
                    TrafficTreatment.Builder treatment = DefaultTrafficTreatment.builder()
                            .setEthDst(NEXT_HOP_MAC_PRIVATE);

                    String intentKeyOne = String.valueOf(communityAsn) + ":"
                            + String.valueOf(communityLocalId) + "-One";
                    PointToPointIntent newIntentOne = PointToPointIntent.builder()
                            .appId(appId)
                            .key(Key.of(intentKeyOne, appId))
                            .filteredIngressPoint(routeIntent.filteredEgressPoint())
                            .filteredEgressPoint(EGRESS_POINT_PRIVATE)
                            .selector(DefaultTrafficSelector.builder().build())
                            .treatment(treatment.build())
                            .priority(PRIORITY)
                            .build();
                    log.info("Intent to be installed {}", newIntentOne);
                    intentSyncService.submit(newIntentOne);

                    String intentKeyTwo = String.valueOf(communityAsn) + ":"
                            + String.valueOf(communityLocalId) + "-Two";
                    PointToPointIntent newIntentTwo = PointToPointIntent.builder()
                            .appId(appId)
                            .key(Key.of(intentKeyTwo, appId))
                            .filteredIngressPoint(EGRESS_POINT_PRIVATE)
                            .filteredEgressPoint(routeIntent.filteredEgressPoint())
                            .selector(DefaultTrafficSelector.builder().build())
                            .treatment(routeIntent.treatment())
                            .priority(PRIORITY)
                            .build();
                    log.info("Intent to be installed {}", newIntentTwo);
                    intentSyncService.submit(newIntentTwo);
                } else {
                    log.info("Community ASN and Local ID in prefix {} are not matched",
                              route.prefix());
                }
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

    private class InternalRouteListener implements RouteListener {
        @Override
        public void event(RouteEvent event) {
            switch (event.type()) {
                case ROUTE_ADDED:
                    update(event.subject());
                    break;
                case ROUTE_UPDATED:
                    update(event.subject());
                    break;
                case ROUTE_REMOVED:
                    update(event.subject());
                    break;
                default:
                    break;
            }
        }
    }
}
