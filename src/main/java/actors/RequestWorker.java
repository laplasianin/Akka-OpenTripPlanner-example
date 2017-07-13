package actors;

import actors.Distributor.RouteRequest;
import actors.Distributor.RouteResponse;
import akka.actor.AbstractLoggingActor;
import akka.event.LoggingAdapter;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.impl.GraphScanner;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;

import java.io.File;
import java.util.List;

import static akka.event.Logging.getLogger;
import static java.util.Collections.singletonList;
import static org.opentripplanner.api.resource.GraphPathToTripPlanConverter.generatePlan;
import static org.opentripplanner.routing.core.TraverseMode.WALK;

public class RequestWorker extends AbstractLoggingActor {

    private final LoggingAdapter log = getLogger(getContext().getSystem(), this);

    private final GraphService graphService;

    public RequestWorker(GraphService graphService) {
        this.graphService = graphService;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RouteRequest.class, this::processRequest)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        System.out.println("Actor restated " + reason.toString());
        super.postRestart(reason);
    }

    private void processRequest(RouteRequest routeRequest) {
        searchForGraph(routeRequest);
        TripPlan tripPlan = makeTripPlan(routeRequest);
        getSender().tell(new RouteResponse(routeRequest, tripPlan), getSelf());
    }


    private void searchForGraph(RouteRequest routeRequest) {
        GraphScanner graphScanner = new GraphScanner(graphService, new File(routeRequest.path), false);
        graphScanner.autoRegister = singletonList(routeRequest.routeName);
        graphScanner.basePath = new File(routeRequest.path);
        graphScanner.startup();
    }

    private TripPlan makeTripPlan(RouteRequest routeRequest) {
        Router router = graphService.getRouter(routeRequest.routeName);
        RoutingRequest routingRequest = dummyRoutingRequest(router.graph);
        List<GraphPath> paths = new GraphPathFinder(router).graphPathFinderEntryPoint(routingRequest);
        return generatePlan(paths, routingRequest);
//        return new TripPlan();
    }

    private RoutingRequest dummyRoutingRequest(Graph graph) {
        RoutingRequest routingRequest = new RoutingRequest();
        routingRequest.setRoutingContext(graph, graph.getVertexById(0), graph.getVertexById(1));
        routingRequest.setModes(new TraverseModeSet(WALK));
        return routingRequest;
    }

}
