$env:SPRING_PROFILES_ACTIVE="test-m7"
$env:JWT_SECRET="MySecretKeyForAIInterviewCoachProject2026SpringBootJWT"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/ai_interview_coach"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="Hardoi@5"
$env:OPENROUTER_API_KEY="dummy"
.\mvnw.cmd spring-boot:run
