package com.sas.sasnettystarter.netty;

import java.util.Objects;

/**
 * 项目
 * 需要子类继承
 * 子类请勿加@Data注解
 * @author WQY
 * @version 1.0
 * @date 2024/1/18 13:34
 */
public abstract class ProjectAbstract implements ProjectInterface{

    public String projectName;

    public String projectCode;

    public String getProjectCode() {
        return projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectName, projectCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProjectAbstract that = (ProjectAbstract) obj;
        return Objects.equals(projectName, that.projectName) && Objects.equals(projectCode, that.projectCode);
    }

    @Override
    public String toString() {
        return "ProjectKey{" +
                "projectName='" + projectName + '\'' +
                ", projectCode='" + projectCode + '\'' +
                '}';
    }
}
