package udp.client;

import com.sas.sasnettystarter.netty.NettyGuideAbstract;
import com.sas.sasnettystarter.netty.ProjectAbstract;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: PTA0A02ANettyGuide
 * @Description: NettyGuide实现
 * @Author: Wqy
 * @Date: 2024-06-05 10:59
 * @Version: 1.0
 **/
@Slf4j
public class NettyUdpClientGuide extends NettyGuideAbstract {

    /**
     * 缓存项目信息引用，便于数据回复时处理
     * key:projectCode
     */
    private static Map<String, ProjectAbstract> PROJECT_MAP = new ConcurrentHashMap<>();

    public static ProjectAbstract getProject(String projectCode) {
        return NettyUdpClientGuide.PROJECT_MAP.get(projectCode);
    }

    public static void putProject(ProjectAbstract project) {
        NettyUdpClientGuide.PROJECT_MAP.put(project.getProjectCode(), project);
    }

}
