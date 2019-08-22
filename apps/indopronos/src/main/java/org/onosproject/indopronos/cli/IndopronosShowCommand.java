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

package org.onosproject.indopronos.cli;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.MultiPointToSinglePointIntent;
import org.onosproject.routing.bgp.BgpInfoService;
import org.onosproject.routing.bgp.BgpRouteEntry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * CLI to interact with the INDOPRONOS application.
 * To show BGP route, community and related intents.
 */

@Command(scope = "onos", name = "indopronos-show",
        description = "Show bgp route and intent for indopronos application")
public class IndopronosShowCommand extends AbstractShellCommand {

    @Override
    protected void execute() {

        BgpInfoService bgpService = AbstractShellCommand.get(BgpInfoService.class);
        IntentService intentService = AbstractShellCommand.get(IntentService.class);

        Collection< BgpRouteEntry > routes4 = bgpService.getBgpRoutes4();
        Collection< BgpRouteEntry > routes6 = bgpService.getBgpRoutes6();

        Iterable<Intent> intents;
        intents = intentService.getIntents();

        MultiPointToSinglePointIntent routeIntent = null;

        // The IPv4 routes
        print(" IPv4 Network     BGP Community      Intent Key");
        for (BgpRouteEntry route : routes4) {

            String community = "None";

            if (route.getCommunities() != null) {
                community = communityCli(route.getCommunities());
            }

            routeIntent = getRouteIntent(route, intents);

            print("%-18s    %-15s   %s",
                  route.prefix().toString(),
                  community,
                  routeIntent.key().toString());
        }

        print ("");                 // Empty separator line

        // The IPv6 routes
        print(" IPv6 Network     BGP Community      Intent Key");
        for (BgpRouteEntry route : routes6) {

            String community = "None";

            if (route.getCommunities() != null) {
                community = communityCli(route.getCommunities());
            }

            routeIntent = getRouteIntent(route, intents);

            print("%-18s    %-15s   %s",
                  route.prefix().toString(),
                  community,
                  routeIntent.key().toString());
        }

        print ("");                 // Empty separator line

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

    /**
     * Formats the BGP Communities as a string that can be shown on the CLI.
     *
     * @param communities the community attributes to format
     * @return the community attributes as a string
     */
    private String communityCli(BgpRouteEntry.Communities communities) {

        ArrayList<BgpRouteEntry.Community> communityList = communities.getCommunities();

        final StringBuilder builder = new StringBuilder();

        for (BgpRouteEntry.Community community : communityList) {

            builder.append(community.getCommunity().getLeft().toString() + ":"
                                   + community.getCommunity().getRight().toString() +" ");

        }

        return builder.toString();

    }

}

