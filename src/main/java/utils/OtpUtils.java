package utils;

import org.opentripplanner.routing.impl.InputStreamGraphSource;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.standalone.CommandLineParameters;
import org.opentripplanner.standalone.OTPServer;

import java.io.File;

public final class OtpUtils {


    public static GraphService createGraphService() {
        GraphService graphService = makeGraphService();
        makeOtpServer(graphService);

        return graphService;
    }

    private static GraphService makeGraphService () {
        GraphService graphService = new GraphService(true);

        File basePath = new File(OtpUtils.class.getResource("").getPath());
        InputStreamGraphSource.FileFactory graphSourceFactory = new InputStreamGraphSource.FileFactory(basePath);
        graphSourceFactory.basePath = basePath;
        graphService.graphSourceFactory = graphSourceFactory;
        return graphService;
    }

    private static void makeOtpServer(GraphService graphService) {
        CommandLineParameters params = new CommandLineParameters();
        OTPServer otpServer = new OTPServer(params, graphService);
        otpServer.basePath = new File(OtpUtils.class.getResource("").getPath());
    }

    private OtpUtils() {}
}
