package dbService;

import config.Config;
import sendingMail.SendMail;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class DBService {

    private final Connection connection;
    private boolean badStatusTemp = false;
    private boolean badStatusHum = false;
    private boolean badStatusLight = false;

    public String[] statusForReport = new String[3];

    public DBService() throws SQLException {
        this.connection = getMysqlConnection();
        createEmptyTables();
    }

    public HashMap<String, String> getDataToday() throws SQLException {
        List<String> sanpinValues = new ArrayList<>();
        HashMap<String, String> pageVariables = new HashMap<>();

        String sqlStr = "SELECT * FROM data WHERE date='";

        Date dateSet = new Date();
        SimpleDateFormat formatForDateToday = new SimpleDateFormat("yyyy-MM-dd");
        //SimpleDateFormat formatForDateNow = new SimpleDateFormat("E yyyy.MM.dd 'и время' hh:mm:ss a zzz");
        String dateToday = formatForDateToday.format(dateSet);
        System.out.println("Показания в базе данных на сегодня - " + dateToday);
        sqlStr = sqlStr + dateToday + "'";

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlStr);
        int id=0;

        int tempChk = 0, humChk = 0, lightChk = 0; // суммируемые показатели для расчета среднего значения
        int countChk = 0; //среднее сначение

        try{
            while(resultSet.next()){
                id = resultSet.getInt("id");
                String cab = resultSet.getString("cab");
                int category = resultSet.getInt("category");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                int temp = resultSet.getInt("temp");
                int hum = resultSet.getInt("hum");
                int light = resultSet.getInt("light");
                System.out.println(id + " | " + cab + " | " + category + " | " + date + " | " + time + " | " + temp + " | " + hum + " | " + light);
                String temporal = ("</br>id: " + id + " | cabinet: " + cab + " | category: " + category + " | date: " + date + " | time: " + time + " | temp: " + temp + " | hum: " + hum + " | light: " + light);
                sanpinValues.add(temporal);
                pageVariables.put("sanpin", String.valueOf(sanpinValues));

                countChk++;
                tempChk = tempChk + temp;
                humChk = humChk + hum;
                lightChk = lightChk + light;
            }
            if (id==0) pageVariables.put("sanpin", "Today results is empty!");

            for (Map.Entry<String, String> tmpMap : pageVariables.entrySet()){
                System.out.println(tmpMap.getKey() + " = " + pageVariables.entrySet() );
            }

            //расчитать среднее значение и передать методу
            if (countChk!=0){
                tempChk = tempChk/countChk;
                humChk = humChk/countChk;
                lightChk = lightChk/countChk;
                researchStatus(tempChk, humChk, lightChk);
            }
            if (countChk == 0){
                statusForReport[0] = "Today values temp is empty!";
                statusForReport[1] = "Today values hum is empty!";
                statusForReport[2] = "Today values light is empty!";
            }

            //проверка на пустой результат запроса
            if (id == 0) {
                System.out.println("Today values is empty!");
            }
            System.out.println(sanpinValues);

        }catch (SQLException e){
            e.printStackTrace();
        }
        closeConnection();
        return pageVariables;
    }

    public HashMap<String, String> getNormToday() throws SQLException {
        HashMap<String, String> pageVariables = new HashMap<>();

        String sqlStr = "SELECT * FROM norm WHERE category=1";

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlStr);

        int minTemp = 0, maxTemp = 0, minHum = 0, maxHum = 0, minLight = 0, maxLight = 0;

        try{
            while(resultSet.next()) {
                minTemp = resultSet.getInt("temp_min");
                maxTemp = resultSet.getInt("temp_max");
                minHum = resultSet.getInt("hum_min");
                maxHum = resultSet.getInt("hum_max");
                minLight = resultSet.getInt("light_min");
                maxLight = resultSet.getInt("light_max");

                System.out.println("min temp: " + minTemp + "max temp: " + maxTemp + "min hum: " + minHum + "max hum: "
                        + maxHum + "min light " + minLight + "max light " + maxLight);

            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        if (minTemp != 0){
            String buildNorms = minTemp + "," + maxTemp + "," + minHum + "," + maxHum + "," + minLight + "," + maxLight;
            pageVariables.put("norms", buildNorms);
        }
        if (minTemp == 0){
            pageVariables.put("norms", "Error connection to DB!");
        }
        closeConnection();
        return pageVariables;
    }

    public void insertNewRecToData(String temp, String hum, String light, String cab) throws SQLException {
        String sqlInsert = "INSERT INTO data VALUES (NULL,?,?,?,?,?,?,?)";
        int category = 1; //todo: пока неопределились, что делать с категориями, поэтому везде 1

        Date dateSet = new Date();
        Date timeSet = new Date();

        SimpleDateFormat formatForDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatForTime = new SimpleDateFormat("hh:mm:ss");
        //example: SimpleDateFormat formatForDateNow = new SimpleDateFormat("E yyyy.MM.dd 'и время' hh:mm:ss a zzz");

        String date = formatForDate.format(dateSet);
        String time = formatForTime.format(timeSet);

        PreparedStatement preparedStatement = null;
        try{
            preparedStatement = connection.prepareStatement(sqlInsert);
            preparedStatement.setString(1, cab);
            preparedStatement.setInt(2, category);
            preparedStatement.setString(3, date);
            preparedStatement.setString(4, time);
            preparedStatement.setInt(5, Integer.valueOf(temp));
            preparedStatement.setInt(6, Integer.valueOf(hum));
            preparedStatement.setInt(7, Integer.valueOf(light));
            preparedStatement.execute();

        }catch(SQLException e){
            e.printStackTrace();
        }
        closeConnection();
    }

    public String getDataFromRange(String from, String to) throws SQLException {
        String result = "";
        String sqlString = "SELECT * FROM `data` WHERE date BETWEEN '" + from + "' AND '" + to + "'";
        System.out.println("Входящий запрос на выборку: " + sqlString);

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlString);

        List<String> buildString = new ArrayList<>();
        try{
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String cab = resultSet.getString("cab");
                int category = resultSet.getInt("category");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                int temp = resultSet.getInt("temp");
                int hum = resultSet.getInt("hum");
                int light = resultSet.getInt("light");
                System.out.println(id + " | " + cab + " | " + category + " | " + date + " | " + time + " | " + temp + " | " + hum + " | " + light);
                String temporal = ("id: " + id + " | cabinet: " + cab + " | category: " + category + " | date: " + date + " | time: " + time + " | temp: " + temp + " | hum: " + hum + " | light: " + light);
                buildString.add(temporal);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        for (int i = 0; i < buildString.size(); i++) {
            result += buildString.get(i) + "|";
        }
        closeConnection();
        return result;
    }

    public Map<String,String> getAllDate() throws SQLException {
        HashMap<String, String> pageVariables = new HashMap<>();
        String buildDates = "";
        String sqlStr = "SELECT date FROM `data`";

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlStr);

        String date;
        try{
            while (resultSet.next()){
                date = resultSet.getString("date");
                buildDates += date + ",";
            }
            pageVariables.put("date", buildDates);
        }catch (SQLException e){
            e.printStackTrace();
        }
        closeConnection();
        return pageVariables;
    }

    private void researchStatus(int tempChk, int humChk, int lightChk) throws SQLException {

        String sqlStr = "SELECT * FROM norm WHERE category='1'";

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlStr);

        int tempMin = 0, tempMax = 0, humMin = 0, humMax = 0, lightMin = 0, lightMax = 0;
        String statusTemp = null, statusHum = null, statusLight = null;
        try{
            while (resultSet.next()){
                tempMin = resultSet.getInt("temp_min");
                tempMax = resultSet.getInt("temp_max");
                humMin = resultSet.getInt("hum_min");
                humMax = resultSet.getInt("hum_max");
                lightMin = resultSet.getInt("light_min");
                lightMax = resultSet.getInt("light_max");
                System.out.println("Check: " + tempMin + "|" + tempMax + "|" + humMin + "|" + humMax + "|" + lightMin + "|" + lightMax);

            }
            if (tempChk > tempMax) {
                statusTemp = "The average temperature above the norm. Value:" + tempChk;
                badStatusTemp = true;
            }
            if (tempChk < tempMin) {
                statusTemp = "The average temperature is below the norm. Value: " + tempChk;
                badStatusTemp = true;
            }
            if ((tempChk >= tempMin) && (tempChk <= tempMax)) {
                statusTemp = "The temperature is normal. Value: " + tempChk;
            }

            if (humChk > humMax) {
                statusHum = "The average humidity above the norm. Value:" + humChk;
                badStatusHum = true;
            }
            if (humChk < humMin) {
                statusHum = "The average humidity is below the norm. Value: " + humChk;
                badStatusHum = true;
            }
            if ((humChk >= humMin) && (humChk <= humMax)) {
                statusHum = "The humidity is normal. Value: " + humChk;
            }

            if (lightChk > lightMax) {
                statusLight = "The average light above the norm. Value:" + lightChk;
                badStatusLight = true;
            }
            if (lightChk < lightMin) {
                statusLight = "The average light is below the norm. Value: " + lightChk;
                badStatusLight = true;
            }
            if ((lightChk >= lightMin) && (lightChk <= lightMax)) {
                statusLight = "The light is normal. Value: " + lightChk;
            }

            if (tempMin == 0) System.err.println("RESULTS IS EMPTY! CHECK THIS!");

            System.out.println("Temperature status - " + statusTemp);
            System.out.println("Humidity status - " + statusHum);
            System.out.println("Light status - " + statusLight);

            statusForReport[0] = statusTemp;
            statusForReport[1] = statusHum;
            statusForReport[2] = statusLight;

        }catch (SQLException e){
            e.printStackTrace();
        }
        closeConnection();
    }

    public void checkStatusIntoDB() throws SQLException {

        String sqlStr = "SELECT * FROM data WHERE date='";

        Date dateSet = new Date();
        SimpleDateFormat formatForDateToday = new SimpleDateFormat("yyyy-MM-dd");
        String dateToday = formatForDateToday.format(dateSet);
        System.out.println("Обработка данных на " + dateToday);

        sqlStr = sqlStr + dateToday + "'";

        HashMap<Integer, String> buildValuesForResearch = new HashMap<>();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlStr);

        int id=0;
        int max =0;
        Date dateInit = new Date();
        SimpleDateFormat formatForDate = new SimpleDateFormat("yyyy-MM-dd");
        String dateSearch = formatForDate.format(dateInit);

        try {
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                String cab = resultSet.getString("cab");
                int category = resultSet.getInt("category");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                int temp = resultSet.getInt("temp");
                int hum = resultSet.getInt("hum");
                int light = resultSet.getInt("light");
                System.out.println(id + " | " + cab + " | " + category + " | " + date + " | " + time + " | " + temp + " | " + hum + " | " + light);
                String temporal = (temp + "," + hum + "," + light + "," + date);
                buildValuesForResearch.put(id, temporal);
                if (id > max) {
                    max = id;
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        if (id == 0) {
            System.out.println("Данных на сегодня нет");
        } else {
            String lastRecord = buildValuesForResearch.get(max); //получили последнюю запись в базе
            String[] parseForAnalyze;
            String delimeter = ",";
            parseForAnalyze = lastRecord.split(delimeter);

            //parseForAnalyze[0] - temp, parseForAnalyze[1] - hum, parseForAnalyze[2] - light, parseForAnalyze[3] - date
            researchStatus(Integer.valueOf(parseForAnalyze[0]),Integer.valueOf(parseForAnalyze[1]),Integer.valueOf(parseForAnalyze[2]));
            if (dateSearch.equals(parseForAnalyze[3])){
                if ((badStatusTemp == true) || (badStatusHum == true) || (badStatusLight = true)){
                    System.out.println();
                    SendMail sendMail = new SendMail();
                    sendMail.sendReportToOperator(statusForReport);
                }
            } else {
                System.out.println("Данных на сегодня нет");
            }

        }
        closeConnection();
    }

    public static Connection getMysqlConnection() {
        Config config = new Config();
        config.readConfig();

        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());

            StringBuilder url = new StringBuilder();

            url.
                    append("jdbc:mysql://").                                        //db type
                    append(config.getDbServer()+":").                               //host name
                    append("3306/").                                                //port
                    append(config.getDbName()+"?").                                 //db name
                    append("user="+config.getDbUsername()+"&").                     //login
                    append("password="+config.getDbPassword());                     //password

            Connection connection = DriverManager.getConnection(url.toString());

            return connection;
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createEmptyTables() throws SQLException {
        Config config = new Config();
        config.readConfig();

        String sqlStrData = "CREATE TABLE data ( `id` INT NOT NULL AUTO_INCREMENT , `cab` TEXT NOT NULL , `category` INT NOT NULL , `date` TEXT NOT NULL , `time` TEXT NOT NULL , `temp` INT NOT NULL , `hum` INT NOT NULL , `light` INT NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;";

        String sqlStrNorms = "CREATE TABLE norm ( `category` INT NOT NULL , `temp_min` INT NOT NULL , `hum_min` INT NOT NULL , `light_min` INT NOT NULL , `temp_max` INT NOT NULL , `hum_max` INT NOT NULL , `light_max` INT NOT NULL) ENGINE = InnoDB;";

        String sqlDropTableData = "DROP TABLE data";
        String sqlDropTableNorm = "DROP TABLE norm";

        if (config.getEmptyDb().equals("yes")){
            Statement statement = connection.createStatement();
            statement.executeUpdate(sqlStrData);
            statement.executeUpdate(sqlStrNorms);
            System.out.println("Чистые базы созданы ...");
        }

        if (config.getEmptyDb().equals("no")){
            System.out.println("Старт конфигурации с предустановленной базой данных ...");
        }
    }

    public void printConnectInfo() {
        try {
            System.out.println("DB name: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("DB version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("Driver: " + connection.getMetaData().getDriverName());
            System.out.println("Autocommit: " + connection.getAutoCommit());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }
}
