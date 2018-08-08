package notification;

import dbService.DBService;

import java.sql.SQLException;

public class NotificationService {

    public NotificationService() {
        createThread();
    }

    public void createThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        runCheckProcedure();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(3_600_000); //1 hour
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void runCheckProcedure() throws SQLException {
        DBService dbService = new DBService();
        dbService.checkStatusIntoDB();
    }
}
