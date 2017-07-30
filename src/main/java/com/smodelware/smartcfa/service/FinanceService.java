package com.smodelware.smartcfa.service;

import com.google.cloud.bigquery.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by varun on 7/29/2017.
 */
public class FinanceService {

    private static final Set<String> CACHE = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public Set<String> getCompanyNames(String initials){
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();


        // [START run_query]
        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(
                        "SELECT name " +
                                "FROM [testscoreservice:sec_xlbr.submissions] " +
                                "group by name LIMIT 100;")
                        // Use standard SQL syntax for queries.
                        // See: https://cloud.google.com/bigquery/sql-reference/
                        .setUseLegacySql(false)
                        .build();
        // Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        // Wait for the query to complete.
        try {
            queryJob = queryJob.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        // Check for errors
        if (queryJob == null) {
            throw new RuntimeException("Job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            // You can also look at queryJob.getStatus().getExecutionErrors() for all
            // errors, not just the latest one.
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        // Get the results.
        QueryResponse response = bigquery.getQueryResults(jobId);
        // [END run_query]

        // [START print_results]
        QueryResult result = response.getResult();

        // Print all pages of the results.
        while (result != null) {
            for (List<FieldValue> row : result.iterateAll()) {
                //CACHE.add(row.get(0).getRecordValue())
            }
        }

        return null;
    }
}
