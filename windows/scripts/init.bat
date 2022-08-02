where /q choco
if %ERRORLEVEL%==1 (
    @"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command " [System.Net.ServicePointManager]::SecurityProtocol = 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
) else (
    echo "Chocolatey Installed"
)

choco feature enable --name="useEnhancedExitCodes"


choco list cmake --local-only
if %ERRORLEVEL%==2 (
    choco install cmake -y
)else (
    echo "cmake installed"
)
where /q cmake
if %ERRORLEVEL%==1 (
    setx /m PATH "%PATH%;C:\Program Files\CMake\bin\"
) else (
    echo "cmake in PATH"
)

choco list git --local-only
if %ERRORLEVEL%==2 (
    choco install git -y
)else (
    echo "git installed"
)

choco list make --local-only
if %ERRORLEVEL%==2 (
    choco install make -y
)else (
    echo "make installed"
)

choco list visualstudio2019community --local-only
if %ERRORLEVEL%==2 (
    choco install visualstudio2019community -y
)else (
    echo "visualstudio2019community installed"
)

choco list visualstudio2019-workload-vctools --local-only
if %ERRORLEVEL%==2 (
    choco install visualstudio2019-workload-vctools -y
)else (
    echo "visualstudio2019-workload-vctools installed"
)

choco list visualstudio2019buildtools --local-only
if %ERRORLEVEL%==2 (
    choco install visualstudio2019buildtools -y
)else (
    echo "visualstudio2019buildtools installed"
)

choco list windows-sdk-10.0 --local-only
if %ERRORLEVEL%==2 (
    choco install windows-sdk-10.0 -y
)else (
    echo "windows-sdk-10.0 installed"
)

refreshenv