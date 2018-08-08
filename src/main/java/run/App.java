package run;

import notification.NotificationService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import servlets.*;

public class App {

    public static void  main(String[] args) throws Exception {
        NotificationService notificationService = new NotificationService();

        WebServlet webServlet = new WebServlet();
        AppServlet appServlet = new AppServlet();
        NormServlet normServlet = new NormServlet();
        ArchiveServlet archiveServlet = new ArchiveServlet();
        SyncServlet syncServlet = new SyncServlet();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(new ServletHolder(webServlet), "/*");
        context.addServlet(new ServletHolder(appServlet), "/app");
        context.addServlet(new ServletHolder(normServlet), "/norms");
        context.addServlet(new ServletHolder(archiveServlet), "/archive");
        context.addServlet(new ServletHolder(syncServlet), "/sync");

        Server server = new Server(4040);
        server.setHandler(context);

        server.start();
        server.join();

    }

}
