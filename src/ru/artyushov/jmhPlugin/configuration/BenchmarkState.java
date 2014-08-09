package ru.artyushov.jmhPlugin.configuration;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaCommandLine;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ex.JavaSdkUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.jps.model.java.compiler.ProcessorConfigProfile;

/**
 * User: nikart
 * Date: 14/07/14
 * Time: 21:36
 */
public class BenchmarkState extends JavaCommandLineState implements JavaCommandLine {

    private final Project project;
    private final JmhConfiguration configuration;

    public BenchmarkState(Project project, JmhConfiguration configuration, ExecutionEnvironment environment) {
        super(environment);
        this.project = project;
        this.configuration = configuration;
        Module module = configuration.getConfigurationModule().getModule();
        if (module != null) {
            CompilerConfigurationImpl compilerConfiguration =
                    (CompilerConfigurationImpl) CompilerConfiguration.getInstance(module.getProject());
            ProcessorConfigProfile processorConfigProfile = compilerConfiguration.getAnnotationProcessingConfiguration(module);
            processorConfigProfile.setEnabled(true);
            compilerConfiguration.getState();
        }
    }

    @Override
    protected JavaParameters createJavaParameters() throws ExecutionException {

        JavaParameters parameters = new JavaParameters();
        JavaParametersUtil.configureConfiguration(parameters, configuration);

        parameters.setMainClass(JmhConfiguration.JMH_START_CLASS);

        int classPathType = JavaParametersUtil.getClasspathType(configuration.getConfigurationModule(),
                JmhConfiguration.JMH_START_CLASS, true);
        JavaParametersUtil.configureModule(configuration.getConfigurationModule(), parameters, classPathType, null);

        Module module = configuration.getConfigurationModule().getModule();
        if (parameters.getJdk() == null){
            parameters.setJdk(module != null
                    ? ModuleRootManager.getInstance(module).getSdk()
                    : ProjectRootManager.getInstance(project).getProjectSdk());
        }
        parameters.getClassPath().add(JavaSdkUtil.getIdeaRtJarPath());

        return parameters;
    }
}