import actors.Distributor;
import akka.dispatch.OnFailure;
import utils.OtpUtils;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnSuccess;
import org.opentripplanner.routing.services.GraphService;
import scala.concurrent.Future;

import java.util.Scanner;

import static akka.pattern.Patterns.ask;

public class Main {

    private static final String ACTOR_SYSTEM = "OtpTestActorSystem";
    private static final String ACTOR_DISTRIBUTOR = "distributor";

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create(ACTOR_SYSTEM);

        GraphService graphService = OtpUtils.createGraphService();

        final ActorRef distributor = system.actorOf(Props.create(Distributor.class, 5, graphService), ACTOR_DISTRIBUTOR);
        listenPipeline(distributor, system);
    }

    private static void listenPipeline(ActorRef distributor, ActorSystem system) {
        System.out.println("Enter path and router separated by space");
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            String f = sc.nextLine();
            if (f.equals("exit")) {
                break;
            }
            String[] words = f.split(" ");
            if (words.length == 2) {
                processRequest(distributor, system, words);
            }
            System.out.println("Enter path and router separated by space");
        }
    }

    private static void processRequest(ActorRef distributor, ActorSystem system, String[] words) {
        Distributor.RouteRequest routeRequest = new Distributor.RouteRequest(words[1], words[0]);
        Future<Object> response = ask(distributor, routeRequest, 1000);
        response.onSuccess(requestSuccessResponse, system.dispatcher());
        response.onFailure(requestFailureResponse, system.dispatcher());
    }

    private static final OnSuccess<Object> requestSuccessResponse = new OnSuccess<Object>() {
        public void onSuccess(Object o) throws Throwable {
            System.out.println("Success response: " + o);
        }
    };

    private static final OnFailure requestFailureResponse = new OnFailure() {
        @Override
        public void onFailure(Throwable throwable) throws Throwable {
            System.out.println("Failure response " + throwable.getLocalizedMessage());
        }
    };

}
