//package io.renren.activiti;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.zip.ZipInputStream;
//
//import org.activiti.engine.FormService;
//import org.activiti.engine.ProcessEngine;
//import org.activiti.engine.ProcessEngineConfiguration;
//import org.activiti.engine.RepositoryService;
//import org.activiti.engine.RuntimeService;
//import org.activiti.engine.TaskService;
//import org.activiti.engine.repository.Deployment;
//import org.activiti.engine.repository.ProcessDefinition;
//import org.activiti.engine.repository.ProcessDefinitionQuery;
//import org.activiti.engine.runtime.ProcessInstance;
//import org.activiti.engine.task.Task;
//
///**
// * Created by CarloJones on 2019/7/11.
// */
//public class formprocess {
//    private static String readDataFromConsole(String prompt) {
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        String str = null;
//        try {
//            System.out.print(prompt);
//            str = br.readLine();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return str;
//    }
//
//    public static void main1(String[] args) {
//
//        String str = readDataFromConsole("Please input string：");
//        System.out.println("The information from console： " + str);
//
//        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
//                .createStandaloneProcessEngineConfiguration();
//        // 连接数据库的配置
//        processEngineConfiguration.setJdbcDriver("com.mysql.jdbc.Driver");
//        processEngineConfiguration
//                .setJdbcUrl("jdbc:mysql://localhost:3306/activitiexam?useUnicode=true&characterEncoding=utf8");
//        processEngineConfiguration.setJdbcUsername("root");
//        processEngineConfiguration.setJdbcPassword("root");
//        processEngineConfiguration.setDatabaseSchemaUpdate("create");
//        ProcessEngine processEngine = processEngineConfiguration
//                .buildProcessEngine();
//
//        System.out.println(processEngine);
//        // 获取流程存储服务组件
//        RepositoryService repositoryService = processEngine
//                .getRepositoryService();
//
//        // 获取运行时服务组件
//        RuntimeService runtimeService = processEngine.getRuntimeService();
//
//        // 获取流程任务组件
//        TaskService taskService = processEngine.getTaskService();
//
//        //获取流程的表单组件
//        FormService formService=processEngine.getFormService();
//
//        // 1、部署流程文件
//        InputStream in =repositoryService.createDeployment().name("MyProcess")
//                .getClass().getClassLoader().getResourceAsStream("diagrams/"+ str +".zip");
//        ZipInputStream zipInputStream = new ZipInputStream(in);
//        Deployment deployment = processEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
//                .createDeployment()// 创建一个部署对象
//                .name("流程定义")// 添加部署的名称
//                .addZipInputStream(zipInputStream)// 指定zip格式的文件完成部署
//                .deploy();// 完成部署
//
//
//        // 2、启动流程--方式一
//		/*ProcessInstance processInstance = runtimeService
//				.startProcessInstanceByKey("myProcess");
//		*/
//
//        /**
//         * 2、启动流程--方式二
//         * 此处使用这种方式，可能有问题，当所有的流程定义的key值均为myProcess时，查询出来的数据为多条
//         * 但是真正使用时，我们是根据前台显示的多条中选择其中之一，就可以直接获取processDefinitionID故也没有问题
//         */
//        ProcessDefinitionQuery query = repositoryService
//                .createProcessDefinitionQuery()
//                .processDefinitionKey("myProcess").active()
//                .orderByProcessDefinitionVersion().desc();
//        ProcessDefinition  processDefinition = query.list().get(0);
//        Map<String, String> formProperties = new HashMap<String, String>();
//
//        formProperties.put("numberOfDays", "20150601");
//        //此处如果第二个参数为null值则报错，即使不放入任何数据也要使用map格式的对象
//        ProcessInstance processInstance = formService
//                .submitStartFormData(processDefinition.getId(), formProperties);
//
//        String end="1";
//        while (end.equalsIgnoreCase("1")) {
//
//            // 3、查询任务
//            Task task = taskService.createTaskQuery()
//                    .processInstanceId(processInstance.getId()).singleResult();
//            ;
//
//            if (task != null) {
//                Object renderedTaskForm = formService.getRenderedTaskForm(task.getId());
//                System.out.println("表单内容---"+renderedTaskForm.toString());
//                System.out.println("任务名称---"+task.getName());
//
//                /**
//                 * 如果整个流程都是顺序执行可以不暂停的，则去掉此段代码即可
//                 * 注：现实中存在的是页面流转过程中是需要点击下一步操作的，可以将此暂停理解为下一步的操作
//                 */
//                str = readDataFromConsole("Please input next:");
//                Map<String, Object> variables = new HashMap<String, Object>();
//                variables.put("result", str);
//
//                //方式一启动流程的完成任务，如果下一个任务需要参数使用map传递，此处的变量均是给下一个任务设置的
//                taskService.complete(task.getId(),variables);
//                //方式二启动流程完成任务
//                //formService.submitTaskFormData(task.getId(), formProperties);
//
//                //注：以上的两种方式可以混用
//            }else {
//                end="0";
//                System.out.println("任务已经完成");
//            }
//
//        }
//
//    }
//}
