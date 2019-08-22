/*
 * Copyright 2017-present Open Networking Foundation
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

package org.onosproject.routing.bgp;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.easymock.EasyMock;
import org.hamcrest.Matchers;
import org.javatuples.Quartet;
import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the BgpRouteEntry class.
 */
public class BgpRouteEntryTest {
    private BgpSession bgpSession;
    private static final Ip4Address BGP_SESSION_BGP_ID =
        Ip4Address.valueOf("10.0.0.1");
    private static final Ip4Address BGP_SESSION_IP_ADDRESS =
        Ip4Address.valueOf("20.0.0.1");

    private BgpSession bgpSession2;
    private static final Ip4Address BGP_SESSION_BGP_ID2 =
        Ip4Address.valueOf("10.0.0.2");
    private static final Ip4Address BGP_SESSION_IP_ADDRESS2 =
        Ip4Address.valueOf("20.0.0.1");

    private BgpSession bgpSession3;
    private static final Ip4Address BGP_SESSION_BGP_ID3 =
        Ip4Address.valueOf("10.0.0.1");
    private static final Ip4Address BGP_SESSION_IP_ADDRESS3 =
        Ip4Address.valueOf("20.0.0.2");

    private final BgpSessionInfo localInfo = new BgpSessionInfo();
    private final BgpSessionInfo remoteInfo = new BgpSessionInfo();

    private final BgpSessionInfo localInfo2 = new BgpSessionInfo();
    private final BgpSessionInfo remoteInfo2 = new BgpSessionInfo();

    private final BgpSessionInfo localInfo3 = new BgpSessionInfo();
    private final BgpSessionInfo remoteInfo3 = new BgpSessionInfo();

    @Before
    public void setUp() throws Exception {
        // Mock objects for testing
        bgpSession = EasyMock.createMock(BgpSession.class);
        bgpSession2 = EasyMock.createMock(BgpSession.class);
        bgpSession3 = EasyMock.createMock(BgpSession.class);

        // Setup the BGP Sessions
        remoteInfo.setIp4Address(BGP_SESSION_IP_ADDRESS);
        remoteInfo2.setIp4Address(BGP_SESSION_IP_ADDRESS2);
        remoteInfo3.setIp4Address(BGP_SESSION_IP_ADDRESS3);
        remoteInfo.setBgpId(BGP_SESSION_BGP_ID);
        remoteInfo2.setBgpId(BGP_SESSION_BGP_ID2);
        remoteInfo3.setBgpId(BGP_SESSION_BGP_ID3);

        EasyMock.expect(bgpSession.localInfo()).andReturn(localInfo).anyTimes();
        EasyMock.expect(bgpSession.remoteInfo()).andReturn(remoteInfo).anyTimes();
        EasyMock.expect(bgpSession2.localInfo()).andReturn(localInfo2).anyTimes();
        EasyMock.expect(bgpSession2.remoteInfo()).andReturn(remoteInfo2).anyTimes();
        EasyMock.expect(bgpSession3.localInfo()).andReturn(localInfo3).anyTimes();
        EasyMock.expect(bgpSession3.remoteInfo()).andReturn(remoteInfo3).anyTimes();

        EasyMock.replay(bgpSession);
        EasyMock.replay(bgpSession2);
        EasyMock.replay(bgpSession3);
    }

    /**
     * Generates a BGP Route Entry.
     *
     * @return a generated BGP Route Entry
     */
    private BgpRouteEntry generateBgpRouteEntry() {
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        // Setup the AS Path
        ArrayList<BgpRouteEntry.PathSegment> pathSegments = new ArrayList<>();
        byte pathSegmentType1 = (byte) BgpConstants.Update.AsPath.AS_SEQUENCE;
        ArrayList<Long> segmentAsNumbers1 = new ArrayList<>();
        segmentAsNumbers1.add(1L);
        segmentAsNumbers1.add(2L);
        segmentAsNumbers1.add(3L);
        BgpRouteEntry.PathSegment pathSegment1 =
            new BgpRouteEntry.PathSegment(pathSegmentType1, segmentAsNumbers1);
        pathSegments.add(pathSegment1);
        //
        byte pathSegmentType2 = (byte) BgpConstants.Update.AsPath.AS_SET;
        ArrayList<Long> segmentAsNumbers2 = new ArrayList<>();
        segmentAsNumbers2.add(4L);
        segmentAsNumbers2.add(5L);
        segmentAsNumbers2.add(6L);
        BgpRouteEntry.PathSegment pathSegment2 =
            new BgpRouteEntry.PathSegment(pathSegmentType2, segmentAsNumbers2);
        pathSegments.add(pathSegment2);
        //
        BgpRouteEntry.AsPath asPath = new BgpRouteEntry.AsPath(pathSegments);
        //
        long localPref = 100;
        long multiExitDisc = 20;

        // Setup Community Attribute
        ArrayList<BgpRouteEntry.Community> communityList = new ArrayList<>();
        long communityAsNumber1 = 64000;
        long communityLocalIdentifier1 = 100;
        BgpRouteEntry.Community community1 = new BgpRouteEntry.Community(
                Pair.of(communityAsNumber1, communityLocalIdentifier1));
        communityList.add(community1);
        long communityAsNumber2 = 65000;
        long communityLocalIdentifier2 = 200;
        BgpRouteEntry.Community community2 = new BgpRouteEntry.Community(
                Pair.of(communityAsNumber2, communityLocalIdentifier2));
        communityList.add(community2);
        BgpRouteEntry.Communities communities =
                new BgpRouteEntry.Communities(communityList);

        // Setup Extended Community Attribute
        ArrayList<BgpRouteEntry.extCommunity> extCommunityList = new ArrayList<>();
        BgpRouteEntry.extCommunity extCommunity1 =
                new BgpRouteEntry.extCommunity(Triple.of(Long.valueOf(0),
                                                         Long.valueOf(64000),
                                                         Long.valueOf(100)));
        BgpRouteEntry.extCommunity extCommunity2 =
                new BgpRouteEntry.extCommunity(Triple.of(Long.valueOf(0),
                                                         Long.valueOf(65000),
                                                         Long.valueOf(200)));
        extCommunityList.add(extCommunity1);
        extCommunityList.add(extCommunity2);
        BgpRouteEntry.extCommunities extCommunities =
                new BgpRouteEntry.extCommunities(extCommunityList);
        //
        BgpRouteEntry bgpRouteEntry =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);

        bgpRouteEntry.setMultiExitDisc(multiExitDisc);

        return bgpRouteEntry;
    }

    /**
     * Tests valid class constructor.
     */
    @Test
    public void testConstructor() {

        BgpRouteEntry bgpRouteEntry = generateBgpRouteEntry();

        String expectedString =
            "BgpRouteEntry{prefix=1.2.3.0/24, nextHop=5.6.7.8, " +
            "bgpId=10.0.0.1, origin=IGP, asPath=AsPath{pathSegments=" +
            "[PathSegment{type=AS_SEQUENCE, segmentAsNumbers=[1, 2, 3]}, " +
            "PathSegment{type=AS_SET, segmentAsNumbers=[4, 5, 6]}]}, " +
            "localPref=100, multiExitDisc=20, community=" +
            "Communities{CommunityList=[Community{CommunityAttribute=" +
            "(64000,100)}, Community{CommunityAttribute=(65000,200)}]}, " +
            "extCommunity=extCommunities{ExtendedCommunityList=" +
            "[extCommunity{ExtendedCommunityAttribute=(0,64000,100)}, " +
            "extCommunity{ExtendedCommunityAttribute=(0,65000,200)}]}}";
        assertThat(bgpRouteEntry.toString(), is(expectedString));
    }

    /**
     * Tests invalid class constructor for null BGP Session.
     */
    @Test(expected = NullPointerException.class)
    public void testInvalidConstructorNullBgpSession() {
        BgpSession bgpSessionNull = null;
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        // Setup the AS Path
        ArrayList<BgpRouteEntry.PathSegment> pathSegments = new ArrayList<>();
        BgpRouteEntry.AsPath asPath = new BgpRouteEntry.AsPath(pathSegments);
        //
        long localPref = 100;

        // Setup Community Attribute
        ArrayList<BgpRouteEntry.Community> communityList = new ArrayList<>();
        BgpRouteEntry.Communities communities =
                new BgpRouteEntry.Communities(communityList);

        // Setup Extended Community Attribute
        ArrayList<BgpRouteEntry.extCommunity> extCommunityList = new ArrayList<>();
        BgpRouteEntry.extCommunities extCommunities =
                new BgpRouteEntry.extCommunities(extCommunityList);

        new BgpRouteEntry(bgpSessionNull, prefix, nextHop, origin, asPath,
                    localPref, communities, extCommunities);
    }

    /**
     * Tests invalid class constructor for null AS Path.
     */
    @Test(expected = NullPointerException.class)
    public void testInvalidConstructorNullAsPath() {
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        BgpRouteEntry.AsPath asPath = null;
        long localPref = 100;

        // Setup Community Attribute
        ArrayList<BgpRouteEntry.Community> communityList = new ArrayList<>();
        BgpRouteEntry.Communities communities =
                new BgpRouteEntry.Communities(communityList);

        // Setup Extended Community Attribute
        ArrayList<BgpRouteEntry.extCommunity> extCommunityList = new ArrayList<>();
        BgpRouteEntry.extCommunities extCommunities =
                new BgpRouteEntry.extCommunities(extCommunityList);

        new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                          localPref, communities, extCommunities);
    }

    /**
     * Tests invalid class constructor for null AS Path.
     */
    @Test
    public void testConstructorNullCommunity() {
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        // Setup the AS Path
        ArrayList<BgpRouteEntry.PathSegment> pathSegments = new ArrayList<>();
        BgpRouteEntry.AsPath asPath = new BgpRouteEntry.AsPath(pathSegments);
        //
        long localPref = 100;

        BgpRouteEntry.Communities communities = null;
        BgpRouteEntry.extCommunities extCommunities = null;

        new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                          localPref, communities, extCommunities);
    }

    /**
     * Tests getting the fields of a BGP route entry.
     */
    @Test
    public void testGetFields() {
        // Create the fields to compare against
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        // Setup the AS Path
        ArrayList<BgpRouteEntry.PathSegment> pathSegments = new ArrayList<>();
        byte pathSegmentType1 = (byte) BgpConstants.Update.AsPath.AS_SEQUENCE;
        ArrayList<Long> segmentAsNumbers1 = new ArrayList<>();
        segmentAsNumbers1.add(1L);
        segmentAsNumbers1.add(2L);
        segmentAsNumbers1.add(3L);
        BgpRouteEntry.PathSegment pathSegment1 =
            new BgpRouteEntry.PathSegment(pathSegmentType1, segmentAsNumbers1);
        pathSegments.add(pathSegment1);
        //
        byte pathSegmentType2 = (byte) BgpConstants.Update.AsPath.AS_SET;
        ArrayList<Long> segmentAsNumbers2 = new ArrayList<>();
        segmentAsNumbers2.add(4L);
        segmentAsNumbers2.add(5L);
        segmentAsNumbers2.add(6L);
        BgpRouteEntry.PathSegment pathSegment2 =
            new BgpRouteEntry.PathSegment(pathSegmentType2, segmentAsNumbers2);
        pathSegments.add(pathSegment2);
        //
        BgpRouteEntry.AsPath asPath = new BgpRouteEntry.AsPath(pathSegments);
        //
        long localPref = 100;
        long multiExitDisc = 20;

        // Setup Community Attribute
        ArrayList<BgpRouteEntry.Community> communityList = new ArrayList<>();
        long communityAsNumber1 = 64000;
        long communityLocalIdentifier1 = 100;
        BgpRouteEntry.Community community1 = new BgpRouteEntry.Community(
                Pair.of(communityAsNumber1, communityLocalIdentifier1));
        communityList.add(community1);
        long communityAsNumber2 = 65000;
        long communityLocalIdentifier2 = 200;
        BgpRouteEntry.Community community2 = new BgpRouteEntry.Community(
                Pair.of(communityAsNumber2, communityLocalIdentifier2));
        communityList.add(community2);
        BgpRouteEntry.Communities communities =
                new BgpRouteEntry.Communities(communityList);

        // Generate the entry to test
        BgpRouteEntry bgpRouteEntry = generateBgpRouteEntry();

        assertThat(bgpRouteEntry.prefix(), is(prefix));
        assertThat(bgpRouteEntry.nextHop(), is(nextHop));
        assertThat(bgpRouteEntry.getBgpSession(), is(bgpSession));
        assertThat(bgpRouteEntry.getOrigin(), is(origin));
        assertThat(bgpRouteEntry.getAsPath(), is(asPath));
        assertThat(bgpRouteEntry.getLocalPref(), is(localPref));
        assertThat(bgpRouteEntry.getMultiExitDisc(), is(multiExitDisc));
        assertThat(bgpRouteEntry.getCommunities().toString(),
                   is(communities.toString()));

    }

    /**
     * Tests whether a BGP route entry is a local route.
     */
    @Test
    public void testIsLocalRoute() {
        //
        // Test non-local route
        //
        BgpRouteEntry bgpRouteEntry = generateBgpRouteEntry();
        assertThat(bgpRouteEntry.isLocalRoute(), is(false));

        //
        // Test local route with AS Path that begins with AS_SET
        //
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        // Setup the AS Path
        ArrayList<BgpRouteEntry.PathSegment> pathSegments = new ArrayList<>();
        byte pathSegmentType1 = (byte) BgpConstants.Update.AsPath.AS_SET;
        ArrayList<Long> segmentAsNumbers1 = new ArrayList<>();
        segmentAsNumbers1.add(1L);
        segmentAsNumbers1.add(2L);
        segmentAsNumbers1.add(3L);
        BgpRouteEntry.PathSegment pathSegment1 =
            new BgpRouteEntry.PathSegment(pathSegmentType1, segmentAsNumbers1);
        pathSegments.add(pathSegment1);
        //
        byte pathSegmentType2 = (byte) BgpConstants.Update.AsPath.AS_SEQUENCE;
        ArrayList<Long> segmentAsNumbers2 = new ArrayList<>();
        segmentAsNumbers2.add(4L);
        segmentAsNumbers2.add(5L);
        segmentAsNumbers2.add(6L);
        BgpRouteEntry.PathSegment pathSegment2 =
            new BgpRouteEntry.PathSegment(pathSegmentType2, segmentAsNumbers2);
        pathSegments.add(pathSegment2);
        //
        BgpRouteEntry.AsPath asPath = new BgpRouteEntry.AsPath(pathSegments);
        //
        long localPref = 100;
        long multiExitDisc = 20;

        BgpRouteEntry.Communities communities = null;
        BgpRouteEntry.extCommunities extCommunities = null;

        //
        bgpRouteEntry =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry.setMultiExitDisc(multiExitDisc);
        assertThat(bgpRouteEntry.isLocalRoute(), is(true));

        //
        // Test local route with empty AS Path
        //
        pathSegments = new ArrayList<>();
        asPath = new BgpRouteEntry.AsPath(pathSegments);
        bgpRouteEntry =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry.setMultiExitDisc(multiExitDisc);
        assertThat(bgpRouteEntry.isLocalRoute(), is(true));
    }

    /**
     * Tests getting the BGP Neighbor AS number for a route.
     */
    @Test
    public void testGetNeighborAs() {
        //
        // Get neighbor AS for non-local route
        //
        BgpRouteEntry bgpRouteEntry = generateBgpRouteEntry();
        assertThat(bgpRouteEntry.getNeighborAs(), is(1L));

        //
        // Get neighbor AS for a local route
        //
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        // Setup the AS Path
        ArrayList<BgpRouteEntry.PathSegment> pathSegments = new ArrayList<>();
        BgpRouteEntry.AsPath asPath = new BgpRouteEntry.AsPath(pathSegments);
        //
        long localPref = 100;
        long multiExitDisc = 20;

        BgpRouteEntry.Communities communities = null;
        BgpRouteEntry.extCommunities extCommunities = null;

        //
        bgpRouteEntry =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry.setMultiExitDisc(multiExitDisc);
        assertThat(bgpRouteEntry.getNeighborAs(), is(BgpConstants.BGP_AS_0));
    }

    /**
     * Tests whether a BGP route entry has AS Path loop.
     */
    @Test
    public void testHasAsPathLoop() {
        BgpRouteEntry bgpRouteEntry = generateBgpRouteEntry();

        // Test for loops: test each AS number in the interval [1, 6]
        for (int i = 1; i <= 6; i++) {
            assertThat(bgpRouteEntry.hasAsPathLoop(i), is(true));
        }

        // Test for non-loops
        assertThat(bgpRouteEntry.hasAsPathLoop(500), is(false));
    }

    /**
     * Tests the BGP Decision Process comparison of BGP routes.
     */
    @Test
    public void testBgpDecisionProcessComparison() {
        BgpRouteEntry bgpRouteEntry1 = generateBgpRouteEntry();
        BgpRouteEntry bgpRouteEntry2 = generateBgpRouteEntry();

        //
        // Compare two routes that are same
        //
        assertThat(bgpRouteEntry1.isBetterThan(bgpRouteEntry2), is(true));
        assertThat(bgpRouteEntry2.isBetterThan(bgpRouteEntry1), is(true));

        //
        // Compare two routes with different LOCAL_PREF
        //
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        // Setup the AS Path
        ArrayList<BgpRouteEntry.PathSegment> pathSegments = new ArrayList<>();
        byte pathSegmentType1 = (byte) BgpConstants.Update.AsPath.AS_SEQUENCE;
        ArrayList<Long> segmentAsNumbers1 = new ArrayList<>();
        segmentAsNumbers1.add(1L);
        segmentAsNumbers1.add(2L);
        segmentAsNumbers1.add(3L);
        BgpRouteEntry.PathSegment pathSegment1 =
            new BgpRouteEntry.PathSegment(pathSegmentType1, segmentAsNumbers1);
        pathSegments.add(pathSegment1);
        //
        byte pathSegmentType2 = (byte) BgpConstants.Update.AsPath.AS_SET;
        ArrayList<Long> segmentAsNumbers2 = new ArrayList<>();
        segmentAsNumbers2.add(4L);
        segmentAsNumbers2.add(5L);
        segmentAsNumbers2.add(6L);
        BgpRouteEntry.PathSegment pathSegment2 =
            new BgpRouteEntry.PathSegment(pathSegmentType2, segmentAsNumbers2);
        pathSegments.add(pathSegment2);
        //
        BgpRouteEntry.AsPath asPath = new BgpRouteEntry.AsPath(pathSegments);
        //
        long localPref = 50;                                    // Different
        long multiExitDisc = 20;

        BgpRouteEntry.Communities communities = null;
        BgpRouteEntry.extCommunities extCommunities = null;

        bgpRouteEntry2 =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry2.setMultiExitDisc(multiExitDisc);
        //
        assertThat(bgpRouteEntry1.isBetterThan(bgpRouteEntry2), is(true));
        assertThat(bgpRouteEntry2.isBetterThan(bgpRouteEntry1), is(false));
        localPref = bgpRouteEntry1.getLocalPref();              // Restore

        //
        // Compare two routes with different AS_PATH length
        //
        ArrayList<BgpRouteEntry.PathSegment> pathSegments2 = new ArrayList<>();
        pathSegments2.add(pathSegment1);
        // Different AS Path
        BgpRouteEntry.AsPath asPath2 = new BgpRouteEntry.AsPath(pathSegments2);
        bgpRouteEntry2 =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath2,
                              localPref, communities, extCommunities);
        bgpRouteEntry2.setMultiExitDisc(multiExitDisc);
        //
        assertThat(bgpRouteEntry1.isBetterThan(bgpRouteEntry2), is(false));
        assertThat(bgpRouteEntry2.isBetterThan(bgpRouteEntry1), is(true));

        //
        // Compare two routes with different ORIGIN
        //
        origin = BgpConstants.Update.Origin.EGP;                // Different
        bgpRouteEntry2 =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry2.setMultiExitDisc(multiExitDisc);
        //
        assertThat(bgpRouteEntry1.isBetterThan(bgpRouteEntry2), is(true));
        assertThat(bgpRouteEntry2.isBetterThan(bgpRouteEntry1), is(false));
        origin = bgpRouteEntry1.getOrigin();                    // Restore

        //
        // Compare two routes with different MULTI_EXIT_DISC
        //
        multiExitDisc = 10;                                     // Different
        bgpRouteEntry2 =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry2.setMultiExitDisc(multiExitDisc);
        //
        assertThat(bgpRouteEntry1.isBetterThan(bgpRouteEntry2), is(true));
        assertThat(bgpRouteEntry2.isBetterThan(bgpRouteEntry1), is(false));
        multiExitDisc = bgpRouteEntry1.getMultiExitDisc();      // Restore

        //
        // Compare two routes with different BGP ID
        //
        bgpRouteEntry2 =
            new BgpRouteEntry(bgpSession2, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry2.setMultiExitDisc(multiExitDisc);
        //
        assertThat(bgpRouteEntry1.isBetterThan(bgpRouteEntry2), is(true));
        assertThat(bgpRouteEntry2.isBetterThan(bgpRouteEntry1), is(false));

        //
        // Compare two routes with different BGP address
        //
        bgpRouteEntry2 =
            new BgpRouteEntry(bgpSession3, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry2.setMultiExitDisc(multiExitDisc);
        //
        assertThat(bgpRouteEntry1.isBetterThan(bgpRouteEntry2), is(true));
        assertThat(bgpRouteEntry2.isBetterThan(bgpRouteEntry1), is(false));
    }

    /**
     * Tests equality of {@link BgpRouteEntry}.
     */
    @Test
    public void testEquality() {
        BgpRouteEntry bgpRouteEntry1 = generateBgpRouteEntry();
        BgpRouteEntry bgpRouteEntry2 = generateBgpRouteEntry();

        assertThat(bgpRouteEntry1, is(bgpRouteEntry2));
    }

    /**
     * Tests non-equality of {@link BgpRouteEntry}.
     */
    @Test
    public void testNonEquality() {
        BgpRouteEntry bgpRouteEntry1 = generateBgpRouteEntry();

        // Setup BGP Route 2
        Ip4Prefix prefix = Ip4Prefix.valueOf("1.2.3.0/24");
        Ip4Address nextHop = Ip4Address.valueOf("5.6.7.8");
        byte origin = BgpConstants.Update.Origin.IGP;
        // Setup the AS Path
        ArrayList<BgpRouteEntry.PathSegment> pathSegments = new ArrayList<>();
        byte pathSegmentType1 = (byte) BgpConstants.Update.AsPath.AS_SEQUENCE;
        ArrayList<Long> segmentAsNumbers1 = new ArrayList<>();
        segmentAsNumbers1.add(1L);
        segmentAsNumbers1.add(2L);
        segmentAsNumbers1.add(3L);
        BgpRouteEntry.PathSegment pathSegment1 =
            new BgpRouteEntry.PathSegment(pathSegmentType1, segmentAsNumbers1);
        pathSegments.add(pathSegment1);
        //
        byte pathSegmentType2 = (byte) BgpConstants.Update.AsPath.AS_SET;
        ArrayList<Long> segmentAsNumbers2 = new ArrayList<>();
        segmentAsNumbers2.add(4L);
        segmentAsNumbers2.add(5L);
        segmentAsNumbers2.add(6L);
        BgpRouteEntry.PathSegment pathSegment2 =
            new BgpRouteEntry.PathSegment(pathSegmentType2, segmentAsNumbers2);
        pathSegments.add(pathSegment2);
        //
        BgpRouteEntry.AsPath asPath = new BgpRouteEntry.AsPath(pathSegments);
        //
        long localPref = 500;                                   // Different
        long multiExitDisc = 20;

        BgpRouteEntry.Communities communities = null;
        BgpRouteEntry.extCommunities extCommunities = null;

        BgpRouteEntry bgpRouteEntry2 =
            new BgpRouteEntry(bgpSession, prefix, nextHop, origin, asPath,
                              localPref, communities, extCommunities);
        bgpRouteEntry2.setMultiExitDisc(multiExitDisc);

        assertThat(bgpRouteEntry1, Matchers.is(Matchers.not(bgpRouteEntry2)));
    }

    /**
     * Tests object string representation.
     */
    @Test
    public void testToString() {
        BgpRouteEntry bgpRouteEntry = generateBgpRouteEntry();

        String expectedString =
             "BgpRouteEntry{prefix=1.2.3.0/24, nextHop=5.6.7.8, " +
             "bgpId=10.0.0.1, origin=IGP, asPath=AsPath{pathSegments=" +
             "[PathSegment{type=AS_SEQUENCE, segmentAsNumbers=[1, 2, 3]}, " +
             "PathSegment{type=AS_SET, segmentAsNumbers=[4, 5, 6]}]}, " +
             "localPref=100, multiExitDisc=20, community=" +
             "Communities{CommunityList=[Community{CommunityAttribute=" +
             "(64000,100)}, Community{CommunityAttribute=(65000,200)}]}, " +
             "extCommunity=extCommunities{ExtendedCommunityList=" +
             "[extCommunity{ExtendedCommunityAttribute=(0,64000,100)}, " +
             "extCommunity{ExtendedCommunityAttribute=(0,65000,200)}]}}";
        assertThat(bgpRouteEntry.toString(), is(expectedString));
    }
}
