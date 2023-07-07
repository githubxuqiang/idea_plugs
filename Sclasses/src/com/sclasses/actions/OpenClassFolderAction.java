package com.sclasses.actions;

import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OpenClassFolderAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            // 获取配置中保存的编译后的路径
//            String compilerOutputPath = PropertiesComponent.getInstance().getValue("compilerOutputPath");
            String compilerOutputPath = projectCompilerOutputPath(project);
            // 获取当前编辑的Java文件
            PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            if ("JAVA".equalsIgnoreCase(psiFile.getFileType().getName()) ) {
                PsiJavaFile javaFile = (PsiJavaFile) psiFile;
                VirtualFile virtualFile = javaFile.getVirtualFile();
                if (virtualFile != null) {
                    // 构建Java文件编译后Class文件的路径
                    String classFolderPath = buildClassFolderPath(virtualFile, compilerOutputPath);

                    // 打开Class文件夹
                    openClassFolder(project, classFolderPath);
                }
            }

        }
    }

    private String projectCompilerOutputPath( Project project ){
        String compilerOutputPath ="";
        if (project != null) {
            CompilerProjectExtension extension = CompilerProjectExtension.getInstance(project);
            String compilerOutputUrl = extension.getCompilerOutputUrl();
            compilerOutputPath = VfsUtil.urlToPath(compilerOutputUrl);
            System.out.println("识别到项目默认编译路径："+compilerOutputPath);
            // 此时 compilerOutputPath 就是项目的默认编译路径
        }
        return compilerOutputPath;
    }

    private String buildClassFolderPath(VirtualFile virtualFile, String compilerOutputPath) {
        String name = virtualFile.getName();
        String nameClass = name.replaceAll("java","class");
        String path = virtualFile.getPath();
        String parentPath = virtualFile.getParent().getPath();

        if(!StringUtils.isEmpty(compilerOutputPath) ){
            String s = checkFile(new File(compilerOutputPath), nameClass);
            return s;
        }
        if( virtualFile.getParent().getPath().indexOf( "src/main/java")!=-1){
            return virtualFile.getParent().getPath().replaceAll("src/main/java","target/classes");
        }
        return virtualFile.getParent().getPath().replaceAll("src","target/classes");
    }

    private  String checkFile(File file, String fileName) {
        if (file.isFile() && file.getName().equalsIgnoreCase(fileName)) {
            return  file.getParent();
        }
        if (file.isFile()) {
            return  "";
        }
        List<String> collect = Arrays.stream(file.listFiles()).map(i -> checkFile(i, fileName)).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if(collect!=null &&collect.isEmpty()){
            return "";
        }
        return collect.get(0);
    }


    private void openClassFolder(Project project, String classFolderPath) {
        VirtualFile folder = LocalFileSystem.getInstance().findFileByPath(classFolderPath);
        if (folder != null && folder.isDirectory()) {
            ShowFilePathAction.openFile(new File(classFolderPath));
        }
    }
}
