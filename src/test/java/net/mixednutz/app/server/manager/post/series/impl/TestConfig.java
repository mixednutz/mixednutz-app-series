package net.mixednutz.app.server.manager.post.series.impl;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EntityScan("net.mixednutz.app.server.entity")
@EnableJpaRepositories("net.mixednutz.app.server.repository")
public class TestConfig {

}
