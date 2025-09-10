package ru.ext.edu.postgres.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ext.edu.postgres.blog.repository.PostRepository;

import javax.sql.DataSource;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HousekeepingService {
    private final PostRepository postRepository;
    private final DataSource dataSource;

    @Value("${app.housekeeping.posts.inactive-days:7}")
    private int inactiveDays;

    @Value("${app.housekeeping.posts.delete-limit:1000}")
    private int deleteLimit;

    @Scheduled(cron = "${app.housekeeping.cron:0 0 2 * * ?}")
    @Transactional
    public void performHousekeeping() {
        log.info("Run housekeeping task: delete not active posts (limit: {}) and VACUUM", deleteLimit);
        try {
            var olderThan = OffsetDateTime.now().minusDays(inactiveDays);
            log.debug("Delete not active posts olderThan: {}", olderThan);

            int deletedCount = postRepository.deleteInactivePostsOlderThanWithLimit(olderThan, deleteLimit);
            log.info("Posts deleted: {}", deletedCount);

            if (deletedCount > 0) {
                vacuumPostsTable();
            } else {
                log.info("No posts for deleting, VACUUM not performed");
            }

        } catch (Exception e) {
            log.error("Exception on housekeeping", e);
        }

        log.info("Housekeeping completed");
    }

    private void vacuumPostsTable() {
        String tableName = "posts";
        try (var connection = dataSource.getConnection();
            var statement = connection.createStatement()) {
            log.info("Init VACUUM on table: {}", tableName);
            statement.execute("VACUUM " + tableName);
            log.info("VACUUM on table {} completed", tableName);
        } catch (Exception e) {
            log.error("Exception on VACUUM, table {}", tableName, e);
        }
    }
}