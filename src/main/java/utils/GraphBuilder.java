package utils;

import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class GraphBuilder {

    public static void buildSimpleGraph(String name, GraphService graphService) throws IOException {
        // Create a small graph with 2 vertices and one edge and it's serialized form
        Graph smallGraph = new Graph();
        StreetVertex v1 = new IntersectionVertex(smallGraph, "v1", 0, 0);
        StreetVertex v2 = new IntersectionVertex(smallGraph, "v2", 0, 1);
        new StreetEdge(v1, v2, null, "v1v2", 110, StreetTraversalPermission.PEDESTRIAN, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        smallGraph.save(new ObjectOutputStream(baos));
        byte[] smallGraphData = baos.toByteArray();

        graphService.graphSourceFactory.save("built", new ByteArrayInputStream(smallGraphData));
    }

}
