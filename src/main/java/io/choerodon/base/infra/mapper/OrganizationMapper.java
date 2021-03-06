package io.choerodon.base.infra.mapper;

import java.util.List;
import java.util.Set;

import io.choerodon.base.api.vo.ProjectOverViewVO;
import org.apache.ibatis.annotations.Param;

import io.choerodon.base.api.dto.OrgSharesDTO;
import io.choerodon.base.api.dto.OrganizationSimplifyDTO;
import io.choerodon.base.infra.dto.OrganizationDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * @author wuguokai
 */
public interface OrganizationMapper extends Mapper<OrganizationDTO> {

    List<OrganizationDTO> fulltextSearch(
                                         @Param("name") String name,
                                         @Param("code") String code,
                                         @Param("ownerRealName") String ownerRealName,
                                         @Param("enabled") Boolean enabled,
                                         @Param("params") String params);

    Set<OrganizationDTO> selectFromMemberRoleByMemberId(@Param("memberId") Long memberId,
                                                        @Param("includedDisabled") Boolean includedDisabled);

    Set<OrganizationDTO> selectOrgByUserAndPros(@Param("memberId") Long memberId,
                                                @Param("includedDisabled") Boolean includedDisabled);

    List<OrganizationDTO> selectOrganizationsWithRoles(
            @Param("id") Long id,
            @Param("start") Integer start,
            @Param("size") Integer size,
            @Param("params") String params);

    List<Long> listMemberIds(@Param("orgId") Long orgId,
                             @Param("orgName") String orgName);


    Boolean organizationEnabled(@Param("sourceId") Long sourceId);

    List<OrganizationDTO> selectByIds(@Param("ids") Set<Long> ids);

    /**
     * 获取所有组织{id,name}
     *
     * @return 组织{id,name}
     */
    List<OrganizationSimplifyDTO> selectAllOrgIdAndName();

    /**
     * 获取 指定id范围 的 组织简要信息
     *
     * @param orgIds  指定的组织范围
     * @param name    组织名查询参数
     * @param code    组织编码查询参数
     * @param enabled 组织启停用查询参数
     * @param params  全局模糊搜索查询参数
     * @return 查询结果
     */
    List<OrgSharesDTO> selectSpecified(@Param("orgIds") Set<Long> orgIds,
                                       @Param("name") String name,
                                       @Param("code") String code,
                                       @Param("enabled") Boolean enabled,
                                       @Param("params") String params);


    /**
     * 根据用户Id查询用户所属组织信息.
     *
     * @param userId 用户Id
     * @return 用户所属组织信息
     */
    OrganizationDTO selectOwnOrgByUserId(@Param("userId") Long userId);

    ProjectOverViewVO projectOverview(@Param("organizationId") Long organizationId);
}
