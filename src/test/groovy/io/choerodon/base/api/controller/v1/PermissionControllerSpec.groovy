package io.choerodon.base.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.base.IntegrationTestConfiguration
import io.choerodon.base.api.dto.CheckPermissionDTO
import io.choerodon.base.infra.dto.PermissionDTO
import io.choerodon.base.infra.dto.RoleDTO
import io.choerodon.base.infra.dto.RolePermissionDTO
import io.choerodon.base.infra.mapper.PermissionMapper
import io.choerodon.base.infra.mapper.RoleMapper
import io.choerodon.base.infra.mapper.RolePermissionMapper
import io.choerodon.core.exception.ExceptionResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author dengyouquan
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class PermissionControllerSpec extends Specification {
    private static final String BASE_PATH = "/v1/permissions"
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private PermissionMapper permissionMapper
    @Autowired
    private RoleMapper roleMapper
    @Autowired
    RolePermissionMapper rolePermissionMapper
    @Shared
    def needInit = true
    @Shared
    def needClean = false
    @Shared
    def permissions = new ArrayList<PermissionDTO>()
    @Shared
    def rolePermissions = new ArrayList<RolePermissionDTO>()
    @Shared
    def role = new RoleDTO()

    def setup() {
        if (needInit) {
            given: "清除parsePermissionService数据"
            //异步数据，无法用@Transactional，要么Mock，要么删除
            List<PermissionDTO> list = permissionMapper.selectAll()
            for (PermissionDTO permission : list) {
                permissionMapper.deleteByPrimaryKey(permission)
            }

            and: "构造参数"
            needInit = false
            for (int i = 0; i < 3; i++) {
                PermissionDTO permission = new PermissionDTO()
                permission.setCode("iam-service.permission.get" + i)
                permission.setPath("/v1/permission/" + i)
                permission.setMethod("get")
                switch (i) {
                    case 0:
                        permission.setResourceLevel("site")
                        role.setResourceLevel("site")
                        break
                    case 1:
                        permission.setResourceLevel("organization")
                        role.setResourceLevel("organization")
                        break
                    case 2:
                        permission.setResourceLevel("project")
                        role.setResourceLevel("project")
                        break
                }
                permission.setDescription("Description" + i)
                permission.setAction("get")
                permission.setController("service" + i)
                permission.setLoginAccess(false)
                permission.setPublicAccess(false)
                permission.setServiceCode("iam-service")
                permissions.add(permission)
            }
            role.setCode("role/site/default/permissioner")
            role.setName("权限管理员")
            role.setEnabled(true)
            role.setModified(true)
            role.setEnableForbidden(true)
            role.setBuiltIn(true)
            when: "插入记录"
            int count = 0
            for (PermissionDTO dto : permissions) {
                permissionMapper.insert(dto)
                count++
            }
            role.setPermissions(permissions)
            count += roleMapper.insert(role)
            for (int i = 0; i < 3; i++) {
                RolePermissionDTO rolePermission = new RolePermissionDTO()
                rolePermission.setPermissionCode(permissions.get(i).getCode())
                rolePermission.setRoleCode(role.getCode())
                rolePermissions.add(rolePermission)
            }
            for (RolePermissionDTO dto : rolePermissions) {
                rolePermissionMapper.insert(dto)
                count++
            }

            then: "校验结果"
            count == 7
        }
    }

    def cleanup() {
        if (needClean) {
            given: ""
            def count = 0
            needClean = false

            when: "删除记录"
            for (PermissionDTO permission : permissions) {
                count += permissionMapper.deleteByPrimaryKey(permission)
            }
            for (RolePermissionDTO rolePermission : rolePermissions) {
                count += rolePermissionMapper.deleteByPrimaryKey(rolePermission)
            }
            count += roleMapper.deleteByPrimaryKey(role)

            then: "校验结果"
            count == 7
        }
    }

    def "CheckPermission"() {
        given: "构造请求参数"
        def checkPermissions = new ArrayList<CheckPermissionDTO>()
        def checkPermissionDTO = new CheckPermissionDTO()
        checkPermissionDTO.setResourceType("organization")
        checkPermissionDTO.setCode("iam-service.permission.get")
        checkPermissions << checkPermissionDTO
        def checkPermissionDTO1 = new CheckPermissionDTO()
        checkPermissionDTO1.setResourceType("project")
        checkPermissionDTO1.setCode("iam-service.permission.get")
        checkPermissions << checkPermissionDTO1
        def checkPermissionDTO2 = new CheckPermissionDTO()
        checkPermissionDTO2.setResourceType("site")
        checkPermissionDTO2.setCode("iam-service.permission.get")
        checkPermissions << checkPermissionDTO2

        when: "调用方法"
        def entity = restTemplate.postForEntity(BASE_PATH + "/checkPermission", checkPermissions, List)

        then: "校验结果"
        entity.statusCode.is2xxSuccessful()
        !entity.getBody().isEmpty()
    }

    def "PagingQuery"() {
        given: "构造请求参数"
        def level = "error"

        when: "调用方法[异常-level不合法]"
        def entity = restTemplate.getForEntity(BASE_PATH + "?level={level}", ExceptionResponse, level)

        then: "校验结果"
        entity.statusCode.is2xxSuccessful()
//        entity.getBody().getCode().equals("error.level.illegal")

        when: "调用方法"
        level = "site"
        entity = restTemplate.getForEntity(BASE_PATH + "?level={level}", PageInfo, level)

        then: "校验结果"
        entity.statusCode.is2xxSuccessful()
        entity.getBody().total == 1
        entity.getBody().list.size() == 1
    }

    def "QueryByRoleIds"() {
        given: "构造请求参数"
        List<Long> roleIds = new ArrayList<>()
        roleIds.add(role.getId())

        when: "调用方法"
        def entity = restTemplate.postForEntity(BASE_PATH, roleIds, Set)

        then: "校验结果"
        entity.statusCode.is2xxSuccessful()
        entity.getBody().size() == 3
    }

    def "QueryByRoleIdsAtOrg"() {
        given: "构造请求参数"
        List<Long> roleIds = new ArrayList<>()
        roleIds.add(role.getId())
        def orgId = 1L

        when: "调用方法"
        def entity = restTemplate.postForEntity(BASE_PATH + "/through_roles_at_org/{orgId}", roleIds, Set, orgId)

        then: "校验结果"
        entity.statusCode.is2xxSuccessful()
        entity.getBody().size() == 3
    }

    def "Query"() {
        given: "构造请求参数"
        def paramsMap = new HashMap<String, Object>()

        when: "调用方法"
        paramsMap.put("level", "site")
        paramsMap.put("service_name", "iam-service")
        def entity = restTemplate.getForEntity(BASE_PATH + "/permissionList?level={level}&service_name={service_name}", List, paramsMap)

        then: "校验结果"
        entity.statusCode.is2xxSuccessful()
        entity.getBody().size() == 1
    }

    def "DeleteByCode"() {
        given: "构造请求参数"
        def httpEntity = new HttpEntity<Object>()
        def code = permissions.get(0).getCode()

        when: "调用方法"
        def entity = restTemplate.exchange(BASE_PATH + "?code={code}", HttpMethod.DELETE, httpEntity, ExceptionResponse, code)
        needClean = true

        then: "校验结果"
        entity.statusCode.is2xxSuccessful()
        entity.getBody().getCode().equals("error.permission.delete.fetch.swaggerJson")
    }
}
