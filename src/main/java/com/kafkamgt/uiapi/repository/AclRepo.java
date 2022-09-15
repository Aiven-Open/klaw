package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.dao.AclID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AclRepo extends CrudRepository<Acl, AclID> {
  Optional<Acl> findById(AclID aclID);

  List<Acl> findAllByEnvironmentAndTenantId(String environment, int tenantId);

  List<Acl> findAllByTopictypeAndTeamIdAndTenantId(String topicType, Integer teamId, int tenantId);

  List<Acl> findAllByTopictypeAndTenantId(String topicType, int tenantId);

  List<Acl> findAllByEnvironmentAndTopicnameAndTenantId(
      String environment, String topicName, int tenantId);

  List<Acl> findAllByEnvironmentAndAclPatternTypeAndTenantId(
      String environment, String aclPatternType, int tenantId);

  @Query(
      value = "select count(*) from kwacls where env = :envId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllAclsCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select count(*) from kwacls where teamid = :teamId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select teamid,  count(*) from kwacls where tenantid = :tenantId and topictype='Producer' "
              + "and teamid in (select teamid from kwteams where tenantid = :tenantId) group by teamid",
      nativeQuery = true)
  List<Object[]> findAllProducerAclsGroupByTeamId(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select  count(*) from kwacls where topictype='Producer' and teamid = :teamidVar and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllProducerAclsForTeamId(
      @Param("teamidVar") Integer teamidVar, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select teamid,  count(*) from kwacls where tenantid = :tenantId and topictype='Consumer' "
              + "and teamid in (select teamid from kwteams where tenantid = :tenantId) group by teamid",
      nativeQuery = true)
  List<Object[]> findAllConsumerAclsGroupByTeamId(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select  count(*) from kwacls where topictype='Consumer' and teamid = :teamIdVar and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllConsumerAclsForTeamId(
      @Param("teamIdVar") Integer teamnameVar, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select env, count(*) from kwacls "
              + "where env in (select id from kwenv where envtype='kafka' and tenantid = :tenantId) group by env",
      nativeQuery = true)
  List<Object[]> findAllAclsGroupByEnv(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select env, count(*) from kwacls where teamid = :teamIdVar and tenantid = :tenantId group by env",
      nativeQuery = true)
  List<Object[]> findAllAclsforTeamGroupByEnv(
      @Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

  @Query(value = "select max(aclid) from kwacls where tenantid = :tenantId", nativeQuery = true)
  Integer getNextAclId(@Param("tenantId") Integer tenantId);

  List<Acl> findAllByTenantId(int tenantId);
}
