package config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Config {

    private String dbServer;
    private String dbUsername;
    private String dbPassword;
    private String dbName;
    private String mailLogin;
    private String mailPassword;
    private String mailForSend;
    private String emptyDb;

    public void readConfig(){
        String[] configsInput = new String[8];
        try{
            FileInputStream fstream = new FileInputStream("config.conf");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String tmp;
            int i=0;
            while ((tmp=br.readLine()) != null){
                configsInput[i] = tmp;
                i++;
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
        dbServer = configsInput[0].replaceAll("server=","");
        dbName = configsInput[1].replaceAll("database=","");
        dbUsername = configsInput[2].replaceAll("login=","");
        dbPassword = configsInput[3].replaceAll("password=","");
        mailLogin = configsInput[4].replaceAll("login mail=","");
        mailPassword = configsInput[5].replaceAll("mail password=","");
        mailForSend = configsInput[6].replaceAll("email=","");
        emptyDb = configsInput[7].replaceAll("create empty db=","");
        initParam(dbServer, dbName, dbUsername, dbPassword, mailForSend, mailLogin, mailPassword, emptyDb);

    }

    public void initParam(String dbServer, String dbName, String dbUsername, String dbPassword, String mailForSend, String mailLogin,  String mailPassword, String emptyDb){
        this.dbServer = dbServer;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dbName = dbName;
        this.mailForSend = mailForSend;
        this.mailLogin = mailLogin;
        this.mailPassword = mailPassword;
        this.emptyDb = emptyDb;
    }

    public String getDbServer() {
        return dbServer;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public String getMailLogin() {
        return mailLogin;
    }

    public String getMailPassword() {
        return mailPassword;
    }

    public String getMailForSend() {
        return mailForSend;
    }

    public String getEmptyDb() {
        return emptyDb;
    }
}
