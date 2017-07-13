package actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.routing.SmallestMailboxPool;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.routing.services.GraphService;

import static akka.actor.SupervisorStrategy.restart;
import static akka.event.Logging.getLogger;
import static akka.japi.pf.DeciderBuilder.matchAny;
import static java.util.concurrent.TimeUnit.SECONDS;
import static scala.concurrent.duration.Duration.create;

public class Distributor extends AbstractLoggingActor {

    private final LoggingAdapter log = getLogger(getContext().getSystem(), this);

    private final int workerCounts;
    private final GraphService graphService;

    public Distributor(int workerCounts, GraphService graphService) {
        this.workerCounts = workerCounts;
        this.graphService = graphService;
    }

    private ActorRef router;

    @Override
    public void preStart() throws Exception {
        router = getContext().actorOf(
                new SmallestMailboxPool(workerCounts)
                    .withSupervisorStrategy(oneForOneStrategy)
                    .props(Props.create(RequestWorker.class, graphService)),
                "workers");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RouteRequest.class, this::receiveRouteRequest)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void receiveRouteRequest(Object o) {
        router.forward(o, context());
    }

    public static class RouteRequest {

        final String routeName;
        final String path;

        public RouteRequest(String routeName, String path) {
            this.routeName = routeName;
            this.path = path;
        }

        @Override
        public String toString() {
            return path + " " + routeName;
        }
    }

    static class RouteResponse {

        final RouteRequest request;
        final TripPlan tripPlan;

        public RouteResponse(RouteRequest request, TripPlan tripPlan) {
            this.request = request;
            this.tripPlan = tripPlan;
        }
    }

    private static final OneForOneStrategy oneForOneStrategy = new OneForOneStrategy(
            10,
            create(2, SECONDS),
            matchAny(e -> restart()).build()
    );

}
