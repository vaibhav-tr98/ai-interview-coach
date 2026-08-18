package com.vaibhav.aiinterviewcoach.progress;

import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.enums.SkillCategory;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SkillDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SkillDataSeeder.class);
    private final SkillRepository skillRepository;

    @Override
    public void run(String... args) {
        log.info("Starting to seed initial skills taxonomy...");

        Map<SkillCategory, String[]> skillsToSeed = Map.of(
                SkillCategory.JAVA, new String[]{"JAVA", "OOP", "COLLECTIONS", "JVM", "MULTITHREADING", "EXCEPTIONS"},
                SkillCategory.SPRING_BOOT, new String[]{"SPRING_BOOT", "SPRING_SECURITY", "REST_API", "JPA", "HIBERNATE", "TRANSACTIONS", "DEPENDENCY_INJECTION"},
                SkillCategory.SQL, new String[]{"SQL", "POSTGRESQL", "DATABASE_DESIGN", "INDEXING", "NORMALIZATION"},
                SkillCategory.AI, new String[]{"SPRING_AI", "LLM_INTEGRATION", "PROMPT_ENGINEERING"},
                SkillCategory.DSA, new String[]{"ARRAYS", "STRINGS", "HASHING", "LINKED_LIST", "STACKS", "QUEUES", "TREES", "GRAPHS", "DYNAMIC_PROGRAMMING", "GREEDY", "BINARY_SEARCH"},
                SkillCategory.MERN, new String[]{"JAVASCRIPT", "TYPESCRIPT", "REACT", "NODE_JS", "EXPRESS", "MONGODB"},
                SkillCategory.SYSTEM_DESIGN, new String[]{"SYSTEM_DESIGN", "API_DESIGN"},
                SkillCategory.TOOLS, new String[]{"GIT", "TESTING"},
                SkillCategory.COMMUNICATION, new String[]{"COMMUNICATION"}
        );

        for (Map.Entry<SkillCategory, String[]> entry : skillsToSeed.entrySet()) {
            SkillCategory category = entry.getKey();
            for (String skillName : entry.getValue()) {
                seedSkillIfNotExists(skillName, category);
            }
        }
        
        seedSkillIfNotExists("JWT", SkillCategory.SPRING_BOOT);

        log.info("Finished seeding skills taxonomy.");
    }

    private void seedSkillIfNotExists(String name, SkillCategory category) {
        Optional<Skill> existing = skillRepository.findByName(name);
        if (existing.isEmpty()) {
            Skill skill = Skill.builder()
                    .name(name)
                    .category(category)
                    .description("Canonical skill for " + name)
                    .build();
            skillRepository.save(skill);
            log.info("Seeded skill: {}", name);
        }
    }
}
