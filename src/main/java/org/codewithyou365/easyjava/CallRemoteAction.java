package org.codewithyou365.easyjava;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

/**
 * @author codewithyou365
 */
public class CallRemoteAction extends AnAction {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String path = getPath(event);
        String callTemplate = getEnvValue(event, "__CALL_TEMPLATE");
        if (path == null || path.isEmpty()) {
            String defaultPath = getEnvValue(event, "__CALL_DEFAULT_PATH");
            if (defaultPath == null || defaultPath.isEmpty()) {
                defaultPath = "/__s";
            }
            path = defaultPath;
        }
        if (callTemplate == null || callTemplate.isEmpty()) {
            Messages.showMessageDialog("Please configure the __CALL_TEMPLATE environment variable, like:\nhttps://www.google.com/search?q={path}", "Result", Messages.getErrorIcon());
            return;
        }
        String url = callTemplate.replace("{path}", path);
        Messages.showMessageDialog("curl " + url + "\n\n" + call(url), "Result", Messages.getInformationIcon());
    }

    String call(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(1)).build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getEnvValue(AnActionEvent event, String env) {
        // 获取当前项目
        Project project = event.getProject();
        if (project == null) {
            Messages.showMessageDialog("无法获取当前项目！", "错误", Messages.getErrorIcon());
            return "";
        }

        // 获取 RunManager 实例
        RunManager runManager = RunManager.getInstance(project);


        // 获取当前选中的 Run Configuration
        RunConfiguration selectedConfig = runManager.getSelectedConfiguration() != null ? runManager.getSelectedConfiguration().getConfiguration() : null;
        if (selectedConfig != null) {
            Map<String, String> envVariables = getEnvironmentVariables(selectedConfig);
            return envVariables.get(env);
        }
        return "";
    }

    private Map<String, String> getEnvironmentVariables(RunConfiguration config) {
        if (config instanceof RunProfile) {
            RunProfile profile = (RunProfile) config;

            try {
                // 通过反射获取 EnvironmentVariablesData
                java.lang.reflect.Method method = profile.getClass().getMethod("getEnvs");
                return (Map<String, String>) method.invoke(profile);
            } catch (Exception e) {
                return Map.of(); // 反射失败，返回空
            }
        }
        return Map.of();
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(true);
    }

    public String getPath(@NotNull AnActionEvent event) {
        // 获取当前的编辑器
        Project project = event.getProject();
        if (project == null) return "";

        Editor editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        if (editor == null) return "";

        PsiFile psiFile = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE);
        if (psiFile == null) return "";

        PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
        if (element == null) return "";

        // 查找最近的 PsiMethod（方法）
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null) return "";

        // 提取方法的映射路径
        String methodUrl = findMethodMapping(method);
        if (methodUrl.isEmpty()) return "";


        // 获取方法所属的类
        PsiClass psiClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
        if (psiClass == null) return methodUrl;

        // 处理接口映射
        String interfaceUrl = "";
        for (PsiClass intf : psiClass.getInterfaces()) {
            interfaceUrl = extractMappingValue(intf);
            if (!interfaceUrl.isEmpty()) break; // 取第一个匹配的接口映射
        }

        // 获取当前类的映射
        String classUrl = extractMappingValue(psiClass);

        // 确定最终的 URL
        if (!classUrl.isEmpty()) {
            return Paths.get(classUrl, methodUrl).toString();
        } else {
            return Paths.get(interfaceUrl, methodUrl).toString();
        }
    }

    private String findMethodMapping(PsiMethod method) {
        // 1. 先检查当前方法是否有映射
        String mapping = extractMappingValue(method);
        if (!mapping.isEmpty()) return mapping;

        // 2. 查找父接口或父类
        PsiMethod superMethod = findSuperMethod(method);
        if (superMethod != null) {
            return extractMappingValue(superMethod);
        }

        return "";
    }

    private PsiMethod findSuperMethod(PsiMethod method) {
        PsiMethod[] superMethods = method.findSuperMethods();
        return superMethods.length > 0 ? superMethods[0] : null;
    }

    private String extractMappingValue(PsiModifierListOwner owner) {
        PsiAnnotation mapping = findMappingAnnotation(owner);
        if (mapping == null) return "";

        PsiAnnotationMemberValue value = mapping.findAttributeValue("value");
        if (value instanceof PsiLiteralExpression) {
            Object url = ((PsiLiteralExpression) value).getValue();
            return url != null ? url.toString() : "";
        }
        return "";
    }

    private PsiAnnotation findMappingAnnotation(PsiModifierListOwner owner) {
        PsiAnnotation[] annotations = owner.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) continue;

            if (qualifiedName.endsWith("RequestMapping") || qualifiedName.endsWith("GetMapping") || qualifiedName.endsWith("PostMapping")) {
                return annotation;
            }
        }
        return null;
    }
}