package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.KwMetrics;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface KwMetricsRepo extends CrudRepository<KwMetrics, Integer> {

  List<KwMetrics> findAllByEnv(String env);

  @Query(
      value =
          "select metricstime, metricsattributes from kwkafkametrics where "
              + " env=:envId and metricstype=:metricsType and metricsname=:metricsName"
              + " order by metricsid desc limit 30;",
      nativeQuery = true)
  List<Object[]> findAllByEnvAndMetricsTypeAndMetricsName(
      @Param("envId") String envId,
      @Param("metricsType") String metricsType,
      @Param("metricsName") String metricsName);

  @Query(value = "select max(metricsid) from kwkafkametrics", nativeQuery = true)
  Integer getNextId();
}
