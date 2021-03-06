package io.choerodon.base.app.service;


import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import io.choerodon.base.infra.dto.AuditDTO;

/**
 * Created by Eugen on 01/03/2019.
 */
public interface AuditService {
    /**
     * 创建审计记录
     *
     * @param auditDTO 审计记录dto
     * @return 创建结果
     */
    AuditDTO create(AuditDTO auditDTO);

    /**
     * 分页查询审计记录
     *
     * @param userId       用户Id
     * @param businessType 业务类型
     * @param dataType     数据类型
     * @param Pageable  分页请求
     * @return 分页结果
     */
    PageInfo<AuditDTO> pagingQuery(Long userId, String businessType, String dataType, Pageable Pageable);
}
