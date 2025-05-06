package org.acme.projectjobschedule.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ImportFileCreator {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    private final Map<String, Object> jsonMap;
      private String startDate;

    public ImportFileCreator(String date){

        List<ResourceRecord> resourceList = createResourceList();
        List<JobRecord> jobList = createJobList();
        List<ProjectListCreator> projectList = createProjectList(date);
        this.jsonMap = new LinkedHashMap<>();
        LocalDateTime startDateTime = LocalDateTime.parse(startDate, inputFormatter);
        this.startDate = startDateTime.format(formatter);
        LocalDateTime endDateTime = startDateTime.withHour(23).withMinute(59).withSecond(59);
        String endDate = endDateTime.format(formatter);

        jsonMap.put("ID", "P1570C30");
        jsonMap.put("StartDate", startDate);
        jsonMap.put("EndDate", endDate);
        jsonMap.put("Termination","TS180;US10");
        jsonMap.put("ResourceList", resourceList);
        jsonMap.put("JobList",jobList);
        jsonMap.put("ProjectList",projectList);

    }
    public void convertToJsonFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);

        try {
            // Создаем все необходимые родительские директории
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, jsonMap);
            System.out.println("JSON файл успешно создан: " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при создании JSON файла: " + e.getMessage());
        }
    }

    private String formatStartDate(String startDate){
        if(startDate!=null){
            int dotIndex = startDate.indexOf('.');
            if(dotIndex!=-1){
            startDate = startDate.substring(0, dotIndex);
        }
        }
        return startDate;
    }

    private String getEndDate(String startDate){
        System.out.println("startDate:" + startDate);
        LocalDateTime date = LocalDateTime.parse(startDate, formatter);
        System.out.println("LocalDateTime:" + date);
        LocalDateTime endDate = date.withHour(23).withMinute(59).withSecond(59);
        System.out.println("LocalDateTime plus time:" + endDate);

        return String.valueOf(endDate);
    }
    private ExecutionModeList createExecutionModeList(String rid, int quantity, int efficiencyLine, String np){
        List<ResourceRequirementList> resourceRequirementList = new ArrayList<>();
        resourceRequirementList.add(new ResourceRequirementList(rid, 1));
        String jid = np + "," + rid;
        int duration = ( int ) Math.ceil ( ( double ) quantity / efficiencyLine );
        return  new ExecutionModeList(jid,duration,resourceRequirementList);
    }

    private List<ProjectListCreator> createProjectList(String date){
        List<ProjectListCreator> projectList = new ArrayList<>();

        String url = "jdbc:sqlserver://10.164.30.246;databaseName=MES;integratedSecurity=true;encrypt=true;trustServerCertificate=true";

        String sqlQuery = "SELECT v.KSK, v.SNPZ, v.DTI, v.DTM, v.KMC, v.EMK, v.KOLMV, v.MASSA, v.KOLEV, v.NP, v.UX, "
                + "m.MASSA, m.EAN13, m.SNM, m.NAME "
                + "FROM [MES].[dbo].[BD_VZPMC] as v, NS_MC as m "
                + "WHERE (v.KMC = m.KMC) AND (v.DTI = ?) AND (v.KSK = ?) AND (m.MASSA < ?) "
                + "ORDER BY v.SNPZ";

        try {
            // Установка соединения
            try (Connection connection = DriverManager.getConnection(url);
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

                // Установка параметров для SQL-запроса
                  preparedStatement.setString(1, date + "T00:00:00");     // Параметр для v.DTI
                  preparedStatement.setString(2, "0119030000");          // Параметр для v.KSK
                  preparedStatement.setDouble(3, 0.1);                  // Параметр для m.MASSA

                // Выполнение запроса
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    // Обработка результата
                    while (resultSet.next()) {
                        String snpz = resultSet.getString("SNPZ");
                        String ksk = resultSet.getString("KSK");
                        String dti = resultSet.getString("DTI");
                        String dtm = resultSet.getString("DTM");
                        String kmc = resultSet.getString("KMC");
                        String emk = resultSet.getString("EMK");
                        int kolmv = resultSet.getInt("KOLMV");
                        String vb = resultSet.getString("MASSA"); // MASSA из таблицы BD_VZPMC
                        int kolev = resultSet.getInt("KOLEV");
                        String np = resultSet.getString("NP");
                        String priority = resultSet.getString("UX");
                        double massaM = resultSet.getDouble("MASSA"); // MASSA из таблицы NS_MC
                        String ean13 = resultSet.getString("EAN13");
                        String snm = resultSet.getString("SNM");
                        String name = resultSet.getString("NAME");
                        this.startDate = dtm;
                        List<ExecutionModeList> executionModeList = new ArrayList<>();
                        List<ResourceRequirementList> requirementList = new ArrayList<>();
                        for(ExecutionModeRecord executionModeRecord : executionModes){

                            if(executionModeRecord.ean13.equals(ean13)){
                                executionModeList.add(new ExecutionModeList("SOURCE", 0, Collections.emptyList()));
                                if(executionModeRecord.line1 != 0){
                                    executionModeList.add(createExecutionModeList("Line1", kolev,executionModeRecord.line1, np));
                                }
                                if(executionModeRecord.line2 != 0){
                                    executionModeList.add(createExecutionModeList("Line2", kolev,executionModeRecord.line2, np));
                                }
                                if(executionModeRecord.line3 != 0){
                                    executionModeList.add(createExecutionModeList("Line3", kolev,executionModeRecord.line3, np));
                                }
                                if(executionModeRecord.line4 != 0){
                                    executionModeList.add(createExecutionModeList("Line4", kolev,executionModeRecord.line4, np));
                                }
                                if(executionModeRecord.line5 != 0){
                                    executionModeList.add(createExecutionModeList("Line5", kolev,executionModeRecord.line5, np));
                                }
                                if(executionModeRecord.line6 != 0){
                                    executionModeList.add(createExecutionModeList("Line6", kolev,executionModeRecord.line6, np));
                                }
                                executionModeList.add(new ExecutionModeList("SINK", 0, Collections.emptyList()));
                            }
                        }
                        if(executionModeList.isEmpty()){
                            System.out.println(name + " " + ean13);
                        }
                        projectList.add(new ProjectListCreator(snpz, priority,vb,ean13,np, executionModeList));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage()); // Выводим стек ошибок для отладки
        }
        return projectList;
    }

    private List<ResourceRecord> createResourceList(){
        List<ResourceRecord> resourceList = new ArrayList<>();
        for(int i=1; i<=6; ++i){
            String rid = "Line" + i;
            resourceList.add(new ResourceRecord(rid,1, "global", true, Collections.emptyList()));
        }
        return resourceList;
    }

    private List<JobRecord> createJobList(){
        List<JobRecord> jobList = new ArrayList<>();
        for(int i=0; i<2; i++){
            if(i==0){
                jobList.add(new JobRecord("SOURCE", List.of("Packaging")));
            }
            if(i==1){
                jobList.add(new JobRecord("Packaging", List.of("SINK")));
            }
            else{
                jobList.add(new JobRecord("SINK", Collections.emptyList()));
            }
        }
        return jobList;
    }

    @JsonPropertyOrder({"PID", "Priority", "VB", "GTIN", "NP","ExecutionModeList"})
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    record ProjectListCreator(String pid, String priority,String vb, String gtin, String np, List<ExecutionModeList> executionModeList) {

        @Override
        @JsonProperty("PID")
        public String pid() {
            return pid;
        }

        @Override
        @JsonProperty("Priority")
        public String priority() {
            return priority;
        }

        @Override
        @JsonProperty("VB")
        public String vb() {
            return vb;
        }

        @Override
        @JsonProperty("GTIN")
        public String gtin() {
            return gtin;
        }

        @Override
        @JsonProperty("NP")
        public String np() {
            return np;
        }

        @Override
        @JsonProperty("ExecutionModeList")
        public List<ExecutionModeList> executionModeList() {
            return executionModeList;
        }
    }

    @JsonPropertyOrder({"RID", "Capacity", "@type", "Renewable", "RestrictionList"})
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    record ResourceRecord(String rid, int capacity, String type, boolean renewable, List<String> restrictionList){

        @Override
        @JsonProperty("RID")
        public String rid() {
            return rid;
        }

        @Override
        @JsonProperty("Capacity")
        public int capacity() {
            return capacity;
        }

        @Override
        @JsonProperty("@type")
        public String type() {
            return type;
        }

        @Override
        @JsonProperty("Renewable")
        public boolean renewable() {
            return renewable;
        }

        @Override
        @JsonProperty("RestrictionList")
        public List<String> restrictionList() {
            return restrictionList;
        }
    }

    @JsonPropertyOrder({"JID", "SuccessorList"})
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    record JobRecord(String jid, List<String> successorList){

        @Override
        @JsonProperty("JID")
        public String jid() {
            return jid;
        }

        @Override
        @JsonProperty("SuccessorList")
        public List<String>  successorList() {
            return  successorList;
        }
    }

    @JsonPropertyOrder({"JID", "Duration", "ResourceRequirementList"})
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    record ExecutionModeList( String jid, int duration, List<ResourceRequirementList> resourceRequirementList){

        @Override
        @JsonProperty("JID")
        public String jid() {
            return jid;
        }

        @Override
        @JsonProperty("Duration")
        public int duration() {
            return duration;
        }

        @Override
        @JsonProperty("ResourceRequirementList")
        public List<ResourceRequirementList>  resourceRequirementList() {
            return  resourceRequirementList;
        }
    }

    @JsonPropertyOrder({"RID", "Requirement"})
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    record ResourceRequirementList(String rid, int requirement){

        @Override
        @JsonProperty("RID")
        public String rid() {
            return rid;
        }

        @Override
        @JsonProperty("Requirement")
        public int requirement() {
            return requirement;
        }
    }

    @JsonPropertyOrder({"EAN13", "Line1", "Line2", "Line3", "Line4", "Line5", "Line6", "ProductName"})
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    record ExecutionModeRecord(String ean13, int line1, int line2, int line3,int line4, int line5, int line6, String productName) {

        @Override
        @JsonProperty("EAN13")
        public String ean13() {
            return ean13;
        }

        @Override
        @JsonProperty("Line1")
        public int line1() {
            return line1;
        }

        @Override
        @JsonProperty("Line2")
        public int line2() {
            return line2;
        }

        @Override
        @JsonProperty("Line3")
        public int line3() {
            return 3;
        }

        @Override
        @JsonProperty("Line4")
        public int line4() {
            return line4;
        }
        @Override
        @JsonProperty("Line5")
        public int line5() {
            return line5;
        }

        @Override
        @JsonProperty("Line6")
        public int line6() {
            return line6;
        }

        @Override
        @JsonProperty("ProductName")
        public String productName() {
            return productName;
        }
    }

    private final List<ExecutionModeRecord> executionModes = List.of(
            new ExecutionModeRecord("4810268057748"	,200,196, 206, 220,220,220,
                    "Сырок тв.г.\"Со вкусом Тоффи\" мдж20%40г"
            ),
            new ExecutionModeRecord("4810268043727"	,200,196, 206, 220,220,220,
                    "Сырок тв.г.Кок-мин мдж20%40г"
            ),
            new ExecutionModeRecord("4810268043710",200,196,206,220, 220,220,
                    "Сырок тв. глаз. \"Моя Аленка\" мдж20% Флоупак 40г"
            ),
            new ExecutionModeRecord("4810268045042",200,196,206,220,220,220,
                    "Сырок тв. глаз. с ароматом ванили мдж 23 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268055492"	,200,196,206,220,220,220
                    ,	"Сырок тв. глаз. с наполнителем \"Кофе-карамель\" мдж 20,0 % ,  ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268040450"	,200,196, 206, 220,220,220,
                    "Сырок тв.  глаз.  \"Любимая Коровка\" с наполнителем мдж 23,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268043475"	,200,196, 206, 220,220,220,
                    "Сырок тв. глаз. с наполнителем \"Фисташка\" мдж 23,0 % ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268045066"	,200,196, 206, 220,220,220,
                    "Сырок тв.  глаз.  с наполнителем \"Молоко сгущенное вареное\" и ароматом ванили мдж 20 % ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268053870"	,200,196, 206, 220,220,220,
                    "Сырок тв. глаз. \"Картошка\" мдж 20,0 %,  ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268054228"	,200,196, 206, 220,220,220,
                    "Сырок тв. глаз. с печеньем \"Бискотти\" мдж 20,0 %,  ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268044984"	,200,0, 0, 0,0,0,
                    "Сырок тв. глаз.  \"Плюш\" с ароматом ванили мдж 26,0 %, фольга 45 г"
            ),
            new ExecutionModeRecord("4810268047657"	,200,0, 0, 0,0,0,
                    "Сырок тв. глаз.  \"Плюш\" с ароматом ванили мдж 26,0 %, фольга 45 г"
            ),
            new ExecutionModeRecord("4810268044977"	,200,0, 0, 0,0,0,
                    "Сырок тв. глаз. \"Плюш\" с какао и ароматом ванили мдж 18,0 %, фольга 45 г"
            ),
            new ExecutionModeRecord("4810268049859"	,200,0, 0, 0,0,0,
                    "Сырок тв. глаз. с какао и ароматом ванили мдж 23,0 % ТМ \"Мишка на полюсе\", фольга 45 г"
            ),
            new ExecutionModeRecord("4810268049866"	,200,0, 0, 0,0,0,
                    "Сырок тв.гл. Вар.сгущ. мдж 23,0 % ТМ \"Мишка на полюсе\", фольга 45 г"
            ),
            new ExecutionModeRecord("4810268050121"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз.\"ТОП\" с начинкой \"Клубника\" мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268050114"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТОП\" с начинкой \"Манго\" мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268053153"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТОП\" с начинкой \"Малина\" мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268050138"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТОП\" с какао и начинкой \"Шоколад\" мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268050107"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТОП\" с начинкой \"Молоко сгущенное вареное\" мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268054969"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТОП\" с начинкой \"Фундук\" мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268056826"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТОП\" с начинкой \"Карамель\" и вкусом арахиса мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268050664"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз.\"ТВОРОБУШКИ\" с начинкой \"Манго\" мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268050657"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТВОРОБУШКИ\" с начинкой \"Клубника\" мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268050282"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТВОРОБУШКИ\" с ароматом ванили в темной глазури мдж 20,0 %, флоупак 40г"
            ),
            new ExecutionModeRecord("4810268050640"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТВОРОБУШКИ\" с какао и начинкой \"Шоколад\"  мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268050671"	,0,0, 0, 198,198,198,
                    "Сырок тв. глаз. \"ТВОРОБУШКИ\" с начинкой \"Молоко сгущенное вареное\"  мдж 20,0 %, ФЛОУПАК 40 г"
            ),
            new ExecutionModeRecord("4810268050329"	,0,0, 170, 0,170,0,
                    "Сырок тв. глаз. \"ТОП КАРАМЕЛЬ\" со вкусом и кусочками глазури \"Соленая карамель\" мдж 20 % ФЛОУПАК 35 г"
            ),
            new ExecutionModeRecord("4810268053801"	,0,0, 170, 0,170,0,
                    "Сырок тв. глаз. \"ТОП МАЛИНА\" с кусочками цветной глазури с ароматом малины мдж 20 % ФЛОУПАК 35 г"
            ),
            new ExecutionModeRecord("4810268053818"	,0,0, 170, 0,170,0,
                    "Сырок тв. глаз. \"ТОП АПЕЛЬСИН\" с кусочками цветной глазури с ароматом апельсина мдж 20 % ФЛОУПАК 35 г"
            )
    ) ;
}
