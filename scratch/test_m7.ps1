$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080/api"

# 1. Register a test user
$randomId = Get-Random
$email = "testuser_$randomId@example.com"

Write-Host "Registering $email..."
$registerPayload = @{
    name = "Test User"
    email = $email
    password = "password123"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$baseUrl/v1/users/register" -Method Post -Body $registerPayload -ContentType "application/json"

$loginPayload = @{
    email = $email
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$baseUrl/v1/users/login" -Method Post -Body $loginPayload -ContentType "application/json"
$token = $loginResponse.token
Write-Host "Token: $token"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Start Interview 1
Write-Host "Starting Interview 1..."
$interviewPayload = @{
    interviewType = "JAVA"
    experienceLevel = "JUNIOR"
    role = "Java Backend Developer"
    resume = "My Java Resume"
} | ConvertTo-Json

$interview1 = Invoke-RestMethod -Uri "$baseUrl/interview/start" -Method Post -Body $interviewPayload -Headers $headers
$sessionId1 = $interview1.sessionId
Write-Host "Interview 1 Session ID: $sessionId1"

# 3. Submit Answer 1
Write-Host "Submitting Answer 1 (Strong Answer)..."
$answerPayload = @{
    answer = "In Java, polymorphism allows objects to be treated as instances of their parent class. It is achieved through method overriding (runtime) and method overloading (compile-time). I have extensively used Spring Boot and Hibernate to build scalable APIs using Java 21."
} | ConvertTo-Json

$eval1 = Invoke-RestMethod -Uri "$baseUrl/interview/$sessionId1/answer" -Method Post -Body $answerPayload -Headers $headers

# We can't see evaluated skills in AnswerResponse normally unless it's returned. AnswerResponse might just have nextQuestion.
# We will just verify it didn't crash.

# 4. Check Progress After 1st Attempt
Write-Host "Checking Progress..."
$progress1 = Invoke-RestMethod -Uri "$baseUrl/progress" -Method Get -Headers $headers
Write-Host "Progress: $($progress1 | ConvertTo-Json -Depth 5 -Compress)"

# 5. Start Interview 2
Write-Host "Starting Interview 2..."
$interview2 = Invoke-RestMethod -Uri "$baseUrl/interview/start" -Method Post -Body $interviewPayload -Headers $headers
$sessionId2 = $interview2.sessionId

# 6. Submit Answer 2
Write-Host "Submitting Answer 2 (Weak Answer)..."
$answerPayload2 = @{
    answer = "Java is a snake. I don't know much about Spring Boot but I can copy paste code from StackOverflow."
} | ConvertTo-Json

$eval2 = Invoke-RestMethod -Uri "$baseUrl/interview/$sessionId2/answer" -Method Post -Body $answerPayload2 -Headers $headers

# 7. Check Progress After 2nd Attempt
Write-Host "Checking Progress Again..."
$progress2 = Invoke-RestMethod -Uri "$baseUrl/progress/skills" -Method Get -Headers $headers
Write-Host "Skill Progress: $($progress2 | ConvertTo-Json -Depth 5 -Compress)"

Write-Host "Done."
