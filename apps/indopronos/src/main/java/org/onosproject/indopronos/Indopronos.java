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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.app.ApplicationService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.intentsync.IntentSynchronizationService;
import org.onosproject.net.intent.IntentService;
import org.onosproject.routeservice.RouteService;
import org.onosproject.routing.bgp.BgpInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application component for Inter Domain Policy Routing with ONOS (InDoPRoNOS)
 */

@Component(immediate = true)
public class Indopronos {

    public static final String INDOPRONOS_APP = "org.onosproject.indopronos";
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected BgpInfoService bgpService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentSynchronizationService intentSyncService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ApplicationService applicationService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected RouteService routeService;

    private IndopronosConnectivityManager IndopronosConnectivity;

    private ApplicationId appId;

    @Activate
    protected void activate() {

        appId = coreService.registerApplication(INDOPRONOS_APP);
        IndopronosConnectivity = new IndopronosConnectivityManager(appId,
                                                       bgpService,
                                                       coreService,
                                                       intentService,
                                                       intentSyncService,
                                                       routeService);
        IndopronosConnectivity.start();

        applicationService.registerDeactivateHook(appId,
                           () -> intentSyncService.removeIntentsByAppId(appId));

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        IndopronosConnectivity.stop();
        log.info("Stopped");
    }

}
