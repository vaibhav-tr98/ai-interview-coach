$ErrorActionPreference = "Stop"

Write-Host "Registering user A..."
$userBody = @{
    name = "User A"
    email = "userc@test.com"
    password = "password123"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/register" -Method Post -Body $userBody -ContentType "application/json" -ErrorAction Ignore

Write-Host "Logging in user A..."
$loginBody = @{
    email = "userc@test.com"
    password = "password123"
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/login" -Method Post -Body $loginBody -ContentType "application/json"
$tokenA = $loginRes.token
$headersA = @{
    Authorization = "Bearer $tokenA"
    "Content-Type" = "application/json"
}

Write-Host "Creating interview..."
$interviewBody = @{
    interviewType = "JAVA"
    experienceLevel = "MID_LEVEL"
    resume = "Java resume"
    jobDescription = "Java job"
    projectDescription = "Built a microservices architecture handling 10k RPS"
    role = "Java Backend Developer"
    durationMinutes = 30
} | ConvertTo-Json

$interviewRes = Invoke-RestMethod -Uri "http://localhost:8080/api/interview/start" -Method Post -Body $interviewBody -Headers $headersA

$interviewId = $interviewRes.interviewId
$sessionId = $interviewRes.sessionId

Write-Host "Answering question 1..."
$answerBody = @{
    answer = "Polymorphism is great."
} | ConvertTo-Json

$answerRes = Invoke-RestMethod -Uri "http://localhost:8080/api/interview/$sessionId/answer" -Method Post -Body $answerBody -Headers $headersA

Write-Host "Answering question 2 to trigger EVALUATION..."
$answerBody2 = @{
    answer = "snake case is cool."
} | ConvertTo-Json

$answerRes2 = Invoke-RestMethod -Uri "http://localhost:8080/api/interview/$sessionId/answer" -Method Post -Body $answerBody2 -Headers $headersA

Write-Host "Answering question 3..."
$answerRes3 = Invoke-RestMethod -Uri "http://localhost:8080/api/interview/$sessionId/answer" -Method Post -Body $answerBody -Headers $headersA

Write-Host "Answering question 4..."
$answerRes4 = Invoke-RestMethod -Uri "http://localhost:8080/api/interview/$sessionId/answer" -Method Post -Body $answerBody -Headers $headersA

Write-Host "Answering question 5..."
$answerRes5 = Invoke-RestMethod -Uri "http://localhost:8080/api/interview/$sessionId/answer" -Method Post -Body $answerBody -Headers $headersA


Write-Host "Getting final result..."
$finalRes = Invoke-RestMethod -Uri "http://localhost:8080/api/interview/$sessionId/result" -Method Get -Headers $headersA

Write-Host "Creating communication assessment (EXPECT 1 AI CALL)..."
$assessRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/communication/assess/$interviewId" -Method Post -Headers $headersA
Write-Host "Assess Result: $($assessRes.overallScore)"

Write-Host "Getting existing assessment (EXPECT 0 AI CALLS)..."
$getAssessRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/communication/interviews/$interviewId" -Method Get -Headers $headersA
Write-Host "Get Assess Result: $($getAssessRes.overallScore)"

Write-Host "Getting overview..."
$overviewRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/communication/overview" -Method Get -Headers $headersA
Write-Host "Overview Total: $($overviewRes.totalAssessments)"

Write-Host "Registering User B..."
$userBodyB = @{
    name = "User B"
    email = "userd@test.com"
    password = "password123"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/register" -Method Post -Body $userBodyB -ContentType "application/json" -ErrorAction Ignore

$loginBodyB = @{
    email = "userd@test.com"
    password = "password123"
} | ConvertTo-Json

$loginResB = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/login" -Method Post -Body $loginBodyB -ContentType "application/json"
$tokenB = $loginResB.token
$headersB = @{
    Authorization = "Bearer $tokenB"
    "Content-Type" = "application/json"
}

Write-Host "User B attempting to access User A's assessment..."
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/communication/assess/$interviewId" -Method Post -Headers $headersB
    Write-Host "ERROR: User B was able to access!"
    exit 1
} catch {
    Write-Host "SUCCESS: User B was denied access (expected). Status code: $($_.Exception.Response.StatusCode)"
}

Write-Host "E2E TESTS PASSED"
