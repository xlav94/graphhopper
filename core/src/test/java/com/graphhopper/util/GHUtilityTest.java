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

import com.graphhopper.coll.GHIntLongHashMap;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.List;

/**
 * @author Peter Karich
 */
public class GHUtilityTest {

    @Test
    public void testEdgeStuff() {
        assertEquals(2, GHUtility.createEdgeKey(1, false));
        assertEquals(3, GHUtility.createEdgeKey(1, true));
    }

    @Test
    public void testZeroValue() {
        GHIntLongHashMap map1 = new GHIntLongHashMap();
        assertFalse(map1.containsKey(0));
        // assertFalse(map1.containsValue(0));
        map1.put(0, 3);
        map1.put(1, 0);
        map1.put(2, 1);

        // assertTrue(map1.containsValue(0));
        assertEquals(3, map1.get(0));
        assertEquals(0, map1.get(1));
        assertEquals(1, map1.get(2));

        // instead of assertEquals(-1, map1.get(3)); with hppc we have to check before:
        assertTrue(map1.containsKey(0));

        // trove4j behaviour was to return -1 if non existing:
//        TIntLongHashMap map2 = new TIntLongHashMap(100, 0.7f, -1, -1);
//        assertFalse(map2.containsKey(0));
//        assertFalse(map2.containsValue(0));
//        map2.add(0, 3);
//        map2.add(1, 0);
//        map2.add(2, 1);
//        assertTrue(map2.containsKey(0));
//        assertTrue(map2.containsValue(0));
//        assertEquals(3, map2.get(0));
//        assertEquals(0, map2.get(1));
//        assertEquals(1, map2.get(2));
//        assertEquals(-1, map2.get(3));
    }

    // Nouveaux tests développés dans le cadre de la tâche 3
    @Test
    void testGetAdjNode_withValidEdge() {
        // Arrange
        Graph graph = mock(Graph.class);
        EdgeIteratorState edgeState = mock(EdgeIteratorState.class);

        int edge = 5;
        int adjNode = 7;

        when(graph.getEdgeIteratorState(edge, adjNode)).thenReturn(edgeState);
        when(edgeState.getAdjNode()).thenReturn(42);

        // Act
        int result = GHUtility.getAdjNode(graph, edge, adjNode);

        // Assert
        assertEquals(42, result);
        verify(graph).getEdgeIteratorState(edge, adjNode);
        verify(edgeState).getAdjNode();
    }

    @Test
    void testGetAdjNode_invalidEdge() {
        // Arrange
        Graph graph = mock(Graph.class);
        int edge = -1;
        int adjNode = 7;

        // Act
        int result = GHUtility.getAdjNode(graph, edge, adjNode);

        // Assert
        assertEquals(adjNode, result);
        verify(graph, never()).getEdgeIteratorState(anyInt(), anyInt());
    }

    @Test
    void testGetProblems_InvalidLatitude() {
        // Arrange
        Graph graph = mock(Graph.class);
        NodeAccess na = mock(NodeAccess.class);
        EdgeExplorer explorer = mock(EdgeExplorer.class);
        EdgeIterator iter = mock(EdgeIterator.class);

        when(graph.getNodes()).thenReturn(1);
        when(graph.getNodeAccess()).thenReturn(na);
        when(graph.createEdgeExplorer()).thenReturn(explorer);
        when(explorer.setBaseNode(0)).thenReturn(iter);

        when(na.getLat(0)).thenReturn(200.0);
        when(na.getLon(0)).thenReturn(0.0);

        when(iter.next()).thenReturn(false);

        // Act
        List<String> result = GHUtility.getProblems(graph);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).contains("latitude"));
    }        

    @Test
    void testGetCommonNode_LoopEdge_ThrowsException() {
        // Arrange
        BaseGraph baseGraph = mock(BaseGraph.class);
        EdgeIteratorState e1 = mock(EdgeIteratorState.class);
        EdgeIteratorState e2 = mock(EdgeIteratorState.class);

        when(baseGraph.getEdgeIteratorState(10, Integer.MIN_VALUE)).thenReturn(e1);
        when(baseGraph.getEdgeIteratorState(20, Integer.MIN_VALUE)).thenReturn(e2);

        when(e1.getBaseNode()).thenReturn(4);
        when(e1.getAdjNode()).thenReturn(4); 

        when(e2.getBaseNode()).thenReturn(3);
        when(e2.getAdjNode()).thenReturn(6);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> GHUtility.getCommonNode(baseGraph, 10, 20));
    }
}
