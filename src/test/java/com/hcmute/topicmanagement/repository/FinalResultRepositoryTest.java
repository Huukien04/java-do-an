package com.hcmute.topicmanagement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class FinalResultRepositoryTest {

    @Autowired
    private FinalResultRepository finalResultRepository;

    @Test
    void resultQueriesSupportPublishedAndPeriodFilters() {
        assertTrue(finalResultRepository.findAllByOrderByTopicTitleAsc().isEmpty());
        assertTrue(finalResultRepository.findByTopicPeriodIdOrderByTopicTitleAsc(1L).isEmpty());
        assertTrue(finalResultRepository.findByIsPublishedTrueOrderByTopicTitleAsc().isEmpty());
        assertTrue(finalResultRepository.findByIsPublishedTrueAndTopicPeriodIdOrderByTopicTitleAsc(1L).isEmpty());
    }
}
