Write-Host "Backend yeniden başlatılıyor..." -ForegroundColor Green
Set-Location $PSScriptRoot
Write-Host "Maven ile derleniyor..." -ForegroundColor Yellow
mvn clean compile
Write-Host "Backend başlatılıyor..." -ForegroundColor Green
mvn spring-boot:run

