package io.renren;

import io.renren.activiti.TestService;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.GroupEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by CarloJones on 2019/7/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LeaveFormKeyTest {
    @Test
    public void allPass() throws Exception {
        String str = "diagrams/leave-formkey.zip";

//        String str = "diagrams/diagrams.zip";

        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
                .createStandaloneProcessEngineConfiguration();
        // 连接数据库的配置
        processEngineConfiguration.setJdbcDriver("com.mysql.jdbc.Driver");
        processEngineConfiguration
                .setJdbcUrl("jdbc:mysql://localhost:3306/activitiexam?useUnicode=true&characterEncoding=utf8");
        processEngineConfiguration.setJdbcUsername("root");
        processEngineConfiguration.setJdbcPassword("root");
        processEngineConfiguration.setDatabaseSchemaUpdate("true");
        ProcessEngine processEngine = processEngineConfiguration
                .buildProcessEngine();

        System.out.println(processEngine);

        // 获取流程存储服务组件
        RepositoryService repositoryService = processEngine
                .getRepositoryService();

        // 获取运行时服务组件
        RuntimeService runtimeService = processEngine.getRuntimeService();

        // 获取流程任务组件
        TaskService taskService = processEngine.getTaskService();

        //获取流程的表单组件
        FormService formService= processEngine.getFormService();

        IdentityService identityService = processEngine.getIdentityService();

        HistoryService historyService = processEngine.getHistoryService();


        // 1、部署流程文件
        InputStream in =repositoryService.createDeployment().name("myProcess1111")
                .getClass().getClassLoader().getResourceAsStream(str);
        ZipInputStream zipInputStream = new ZipInputStream(in);
        Deployment deployment = processEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
                .createDeployment()// 创建一个部署对象
                .name("流程定义")// 添加部署的名称
                .addZipInputStream(zipInputStream)// 指定zip格式的文件完成部署
                .deploy();// 完成部署

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> variables = new HashMap<String, String>();
        Calendar ca = Calendar.getInstance();
        String startDate = sdf.format(ca.getTime());
        ca.add(Calendar.DAY_OF_MONTH, 2); // 当前日期加2天
        String endDate = sdf.format(ca.getTime());

        // 启动流程
        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");

        ProcessDefinitionQuery query = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("leave-formkey").active()
                .orderByProcessDefinitionVersion().desc();

//        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        System.out.println(query.list());
        ProcessDefinition processDefinition = query.list().get(0);

        // 读取启动表单
        Object renderedStartForm = formService.getRenderedStartForm(processDefinition.getId());
        System.out.println(renderedStartForm.toString());
        assertNotNull(renderedStartForm);

//        Map<String, String> formProperties = new HashMap<String, String>();
//        formProperties.put("reason", "20190711");
//        //此处如果第二个参数为null值则报错，即使不放入任何数据也要使用map格式的对象
//        ProcessInstance processInstance = formService
//                .submitStartFormData(processDefinition.getId(), formProperties);
        // 启动流程
        // 设置当前用户
        String currentUserId = "henryyan";
        identityService.setAuthenticatedUserId(currentUserId);
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        assertNotNull(processInstance);

        //创建角色
//        Group leaderGroup = identityService.newGroup("001");
//        leaderGroup.setName("deptLeader");
//        identityService.saveGroup(leaderGroup);
//
//        User leader = identityService.newUser("001");
//        leader.setLastName("张三");
//        identityService.saveUser(leader);

//        identityService.createMembership("001","001");

        // 部门领导审批通过
        Task deptLeaderTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId()).singleResult();
        ;
        assertNotNull(formService.getRenderedTaskForm(deptLeaderTask.getId()));
        variables = new HashMap<String, String>();
        variables.put("deptLeaderApproved", "true");
        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        // 人事审批通过
        Task hrTask = taskService.createTaskQuery().taskCandidateGroup("hr").singleResult();
        assertNotNull(formService.getRenderedTaskForm(hrTask.getId()));
        variables = new HashMap<String, String>();
        variables.put("hrApproved", "true");
        formService.submitTaskFormData(hrTask.getId(), variables);

        // 销假（根据申请人的用户ID读取）
        Task reportBackTask = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
        assertNotNull(formService.getRenderedTaskForm(reportBackTask.getId()));
        variables = new HashMap<String, String>();
        variables.put("reportBackDate", sdf.format(ca.getTime()));
        formService.submitTaskFormData(reportBackTask.getId(), variables);

        // 验证流程是否已经结束
//        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().finished().singleResult();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()//
                .processInstanceId(processInstance.getProcessDefinitionId())//
                .unfinished()//未完成的活动(任务)
                .singleResult();
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstance.getId()).singleResult();
//        assertNotNull(historicProcessInstance);
        assertNotNull(pi);
        // 读取历史变量
        Map<String, Object> historyVariables = packageVariables(processInstance,historyService);
        // 验证执行结果
        assertEquals("ok", historyVariables.get("result"));

    }

    /**
     * 读取历史变量并封装到Map中
     */
    private Map<String, Object> packageVariables(ProcessInstance processInstance,HistoryService historyService) {
        Map<String, Object> historyVariables = new HashMap<String, Object>();
        List<HistoricDetail> list = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).list();
        for (HistoricDetail historicDetail : list) {
            if (historicDetail instanceof HistoricFormProperty) {
                // 表单中的字段
                HistoricFormProperty field = (HistoricFormProperty) historicDetail;
                historyVariables.put(field.getPropertyId(), field.getPropertyValue());
                System.out.println("form field: taskId=" + field.getTaskId() + ", " + field.getPropertyId() + " = " + field.getPropertyValue());
            } else if (historicDetail instanceof HistoricVariableUpdate) {
                HistoricVariableUpdate variable = (HistoricVariableUpdate) historicDetail;
                historyVariables.put(variable.getVariableName(), variable.getValue());
                System.out.println("variable: " + variable.getVariableName() + " = " + variable.getValue());
            }
        }
        return historyVariables;
    }
}
