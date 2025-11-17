/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.util;

import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;
import com.github.javafaker.Faker;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

public class DistanceCalcEuclideanTest {
   @Test
    public void failOnPurpose() {
        fail("Test échoué volontairement pour tester le BAUDRY-ROLL");
    }
    
   @Test
    public void testCalcDistSamePoint() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        assertEquals(0.0, distCalc.calcDist(10.0, 20.0, 10.0, 20.0), 1e-12);
    }

    @Test
    public void testCalcDistNaN() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        assertTrue(Double.isNaN(distCalc.calcDist(Double.NaN, 0, 0, 0)));
    }

    @Test
    public void returnsFirstPointWhenFZero() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        GHPoint p = distCalc.intermediatePoint(0.0, 10.0, 20.0, 30.0, 40.0);
        assertEquals(10.0, p.getLat(), 1e-9);
        assertEquals(20.0, p.getLon(), 1e-9);
    }

    @Test
    public void returnsSecondPointWhenFOne() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        GHPoint p = distCalc.intermediatePoint(1.0, 10.0, 20.0, 30.0, 40.0);
        assertEquals(30.0, p.getLat(), 1e-9);
        assertEquals(40.0, p.getLon(), 1e-9);
    }

    @Test
    public void midpointIsCorrect() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        GHPoint p = distCalc.intermediatePoint(0.5, 0.0, 0.0, 10.0, 10.0);
        assertEquals(5.0, p.getLat(), 1e-9);
        assertEquals(5.0, p.getLon(), 1e-9);
    }

    @Test
    public void symmetricPointsGiveSameMidpoint() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        GHPoint p1 = distCalc.intermediatePoint(0.5, 0.0, 0.0, 10.0, 10.0);
        GHPoint p2 = distCalc.intermediatePoint(0.5, 10.0, 10.0, 0.0, 0.0);
        assertEquals(p1.getLat(), p2.getLat(), 1e-9);
        assertEquals(p1.getLon(), p2.getLon(), 1e-9);
    }

    @Test
    public void latLonWithinRange() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        double lat1 = -20, lon1 = -30, lat2 = 40, lon2 = 60;
        double f = 0.3;
        GHPoint p = distCalc.intermediatePoint(f, lat1, lon1, lat2, lon2);
        assertTrue(p.getLat() >= Math.min(lat1, lat2) - 1e-9);
        assertTrue(p.getLat() <= Math.max(lat1, lat2) + 1e-9);
        assertTrue(p.getLon() >= Math.min(lon1, lon2) - 1e-9);
        assertTrue(p.getLon() <= Math.max(lon1, lon2) + 1e-9);
    }

    // Java faker test
    @Test
    public void testCalcNormalizedEdgeDistance_RandomValues() {
        Faker faker = new Faker();
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();

        double ry = faker.number().randomDouble(6, -90, 90);
        double rx = faker.number().randomDouble(6, -180, 180);
        double ay = faker.number().randomDouble(6, -90, 90);
        double ax = faker.number().randomDouble(6, -180, 180);
        double by = faker.number().randomDouble(6, -90, 90);
        double bx = faker.number().randomDouble(6, -180, 180);

        double result = distanceCalc.calcNormalizedEdgeDistance(ry, rx, ay, ax, by, bx);

        assertFalse(Double.isNaN(result), "Le résultat ne doit pas être NaN");
        assertFalse(Double.isInfinite(result), "Le résultat ne doit pas être infini");

        assertTrue(result >= 0.0, "La distance au carré ne peut pas être négative");

        double zeroDist = distanceCalc.calcNormalizedEdgeDistance(1, 1, 1, 1, 1, 1);
        assertEquals(0.0, zeroDist, 1e-9, "La distance doit être 0 lorsque tous les points sont identiques");
    }

    @Test
    public void testCrossingPointToEdge() {
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();
        GHPoint point = distanceCalc.calcCrossingPointToEdge(0, 10, 0, 0, 10, 10);
        assertEquals(5, point.getLat(), 0);
        assertEquals(5, point.getLon(), 0);
    }

    @Test
    public void testCalcNormalizedEdgeDistance() {
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();
        double distance = distanceCalc.calcNormalizedEdgeDistance(0, 10, 0, 0, 10, 10);
        assertEquals(50, distance, 0);
    }

    @Test
    public void testCalcNormalizedEdgeDistance3dStartEndSame() {
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();
        double distance = distanceCalc.calcNormalizedEdgeDistance3D(0, 3, 4, 0, 0, 0, 0, 0, 0);
        assertEquals(25, distance, 0);
    }

    @Test
    public void testValidEdgeDistance() {
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();
        boolean validEdgeDistance = distanceCalc.validEdgeDistance(5, 15, 0, 0, 10, 10);
        assertEquals(false, validEdgeDistance);
        validEdgeDistance = distanceCalc.validEdgeDistance(15, 5, 0, 0, 10, 10);
        assertEquals(false, validEdgeDistance);
    }

    @Test
    public void testDistance3dEuclidean() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        assertEquals(1, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 1
        ), 1e-6);
        assertEquals(10, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 10
        ), 1e-6);
    }
}
