pipeline {
    agent any

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['local-tomcat', 'docker-container', 'staging', 'production'],
            description: 'Target deployment environment'
        )
        string(
            name: 'BRANCH_NAME',
            defaultValue: 'develop',
            description: 'Git branch to build and test (e.g. develop, main, feature/..., defect/...)'
        )
        string(
            name: 'APP_PORT',
            defaultValue: '8084',
            description: 'Target Tomcat application port (Must be 8084; forbidden: 8072, 8081, 8082)'
        )
        string(
            name: 'TOMCAT_HOME',
            defaultValue: 'E:\\Downloads\\apache-tomcat-10.1.61',
            description: 'Filesystem path to Apache Tomcat 10.1 installation'
        )
        booleanParam(
            name: 'DEPLOY_WAR',
            defaultValue: true,
            description: 'Deploy packaged WAR to standalone application server if Tomcat is present'
        )
        booleanParam(
            name: 'DEPLOY_DOCKER',
            defaultValue: true,
            description: 'Build versioned Docker image and deploy container to host port 8085'
        )
        string(
            name: 'DOCKER_HOST_PORT',
            defaultValue: '8085',
            description: 'Target Docker host port mapping (Must be 8085; forbidden: 8072, 8081, 8082)'
        )
        string(
            name: 'DOCKER_IMAGE_NAME',
            defaultValue: 'srishtiyadav1904/containerized-mobile-plan-recommendation-platform',
            description: 'Docker image repository name'
        )
        string(
            name: 'DOCKER_CONTAINER_NAME',
            defaultValue: 'mpr-platform-container',
            description: 'Target project container name'
        )
        booleanParam(
            name: 'PUSH_DOCKER_HUB',
            defaultValue: false,
            description: 'Attempt pushing image to Docker Hub (requires jenkins credentials docker-hub-credentials)'
        )
    }

    triggers {
        pollSCM('H/15 * * * *')
        githubPush()
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        FORBIDDEN_PORTS = '8072,8081,8082'
        APP_NAME = 'containerized-mobile-plan-recommendation-platform'
        BUILT_IMAGE_TAG = ''
        PATH = "C:\\Users\\Shreyas\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== Stage 1: Checkout SCM ==='
                checkout scm
                script {
                    echo "Checked out repository for branch: ${env.BRANCH_NAME ?: params.BRANCH_NAME ?: 'develop'}"
                    echo "Commit: ${env.GIT_COMMIT ?: 'HEAD'}"
                }
            }
        }

        stage('Environment & Port Validation') {
            steps {
                echo '=== Stage 2: Environment & Port Policy Validation ==='
                script {
                    def forbiddenList = env.FORBIDDEN_PORTS.split(',')
                    if (forbiddenList.contains(params.APP_PORT.trim())) {
                        error("[CRITICAL PORT VIOLATION] Tomcat Port ${params.APP_PORT} is in forbidden list (${env.FORBIDDEN_PORTS}). Aborting build.")
                    }
                    if (forbiddenList.contains(params.DOCKER_HOST_PORT.trim())) {
                        error("[CRITICAL PORT VIOLATION] Docker Port ${params.DOCKER_HOST_PORT} is in forbidden list (${env.FORBIDDEN_PORTS}). Aborting build.")
                    }
                    echo "[PORT CHECK] Approved ports: Tomcat=${params.APP_PORT}, Docker=${params.DOCKER_HOST_PORT}. Forbidden ports (8072, 8081, 8082) avoided."
                }
                bat '''
                    echo [TOOL VALIDATION]
                    git --version
                    where java
                    java -version
                    if exist mvnw.cmd (
                        echo [OK] Maven wrapper detected.
                        call mvnw.cmd -v
                    ) else (
                        echo [OK] Using system Maven.
                        call "C:\\Users\\Shreyas\\apache-maven-3.9.9\\bin\\mvn.cmd" -v
                    )
                    echo [DOCKER VALIDATION]
                    where docker
                    docker --version
                    echo [BROWSER AND DRIVER VALIDATION]
                    if exist "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe" echo [OK] Google Chrome detected at C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe
                    if exist "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe" echo [OK] Google Chrome detected at C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe
                '''
            }
        }

        stage('Unit & Integration Tests') {
            steps {
                echo '=== Stage 3: Unit & Integration Tests (Fatal Gate) ==='
                bat '''
                    if exist mvnw.cmd (
                        call mvnw.cmd -B clean test -Dtest="!MobilePlanSeleniumTests"
                    ) else (
                        call "C:\\Users\\Shreyas\\apache-maven-3.9.9\\bin\\mvn.cmd" -B clean test -Dtest="!MobilePlanSeleniumTests"
                    )
                '''
            }
        }

        stage('Selenium End-to-End Tests') {
            steps {
                echo '=== Stage 4: Selenium End-to-End Tests (Quality Gate) ==='
                script {
                    echo "[SELENIUM QUALITY GATE] Executing 5 E2E UI journeys with headless Chrome..."
                    echo "[SELENIUM QUALITY GATE] Target Application Base URL: http://localhost:${params.APP_PORT}/mpr-platform"
                }
                bat """
                    if exist mvnw.cmd (
                        call mvnw.cmd -B test -Dtest=MobilePlanSeleniumTests -Dtest.base.url=http://localhost:${params.APP_PORT}/mpr-platform -Dheadless=true
                    ) else (
                        call "C:\\Users\\Shreyas\\apache-maven-3.9.9\\bin\\mvn.cmd" -B test -Dtest=MobilePlanSeleniumTests -Dtest.base.url=http://localhost:${params.APP_PORT}/mpr-platform -Dheadless=true
                    )
                """
            }
        }

        stage('Package') {
            steps {
                echo '=== Stage 5: Package WAR Artifact ==='
                bat '''
                    if exist mvnw.cmd (
                        call mvnw.cmd -B package -DskipTests
                    ) else (
                        call "C:\\Users\\Shreyas\\apache-maven-3.9.9\\bin\\mvn.cmd" -B package -DskipTests
                    )
                '''
            }
        }

        stage('Deploy to Server') {
            when {
                expression { return params.DEPLOY_WAR == true }
            }
            steps {
                echo '=== Stage 6: Server Deployment & Health Verification ==='
                script {
                    def tomcatPath = params.TOMCAT_HOME
                    if (!fileExists("${tomcatPath}\\bin\\catalina.bat") && !fileExists("${tomcatPath}\\webapps")) {
                        if (fileExists("E:\\Downloads\\apache-tomcat-10.1.61\\bin\\catalina.bat")) {
                            tomcatPath = "E:\\Downloads\\apache-tomcat-10.1.61"
                        } else if (fileExists("C:\\apache-tomcat-10.1\\bin\\catalina.bat")) {
                            tomcatPath = "C:\\apache-tomcat-10.1"
                        }
                    }

                    def tomcatExists = fileExists("${tomcatPath}\\bin\\catalina.bat") || fileExists("${tomcatPath}\\webapps")

                    if (tomcatExists) {
                        echo "[DEPLOYMENT] Standalone Tomcat 10.1 detected at: ${tomcatPath}"
                        echo "[DEPLOYMENT] Deploying binary WAR artifact to ${tomcatPath}\\webapps\\mpr-platform.war"
                        
                        // Use binary PowerShell Copy-Item to prevent cmd.exe ASCII truncation
                        bat """
                            powershell -ExecutionPolicy Bypass -Command "Copy-Item -Path 'target/containerized-mobile-plan-recommendation-platform.war' -Destination '${tomcatPath}/webapps/mpr-platform.war' -Force"
                        """
                        
                        echo "[DEPLOYMENT] WAR successfully copied. Initiating health check verification..."
                        
                        // Execute robust health check script with retries and diagnostics
                        def checkStatus = bat(
                            script: """powershell -ExecutionPolicy Bypass -File scripts/health-check.ps1 -Port ${params.APP_PORT} -ContextPath mpr-platform -TimeoutSeconds 60 -RetryIntervalSeconds 3""",
                            returnStatus: true
                        )

                        if (checkStatus != 0) {
                            error("[DEPLOYMENT FAILED] Application at http://localhost:${params.APP_PORT}/mpr-platform/ failed health check verification. See logs above for details.")
                        }

                        echo "[DEPLOYMENT SUCCESS] Verification complete: Application is UP, healthy, and serving requests on http://localhost:${params.APP_PORT}/mpr-platform/"
                    } else {
                        echo "=========================================================================="
                        echo "[DEPLOYMENT STATUS: PENDING - TOMCAT NOT DETECTED]"
                        echo "Tomcat 10.1 was not detected at path: ${tomcatPath}"
                        echo "WAR is archived at target/*.war. Proceeding to Docker deployment."
                        echo "=========================================================================="
                    }
                }
            }
        }

        stage('Build Docker Image') {
            when {
                expression { return params.DEPLOY_DOCKER == true }
            }
            steps {
                echo '=== Stage 7: Build & Version Docker Image ==='
                script {
                    def shortCommit = env.GIT_COMMIT ? env.GIT_COMMIT.take(7) : 'manual'
                    def imageTag = "build-${env.BUILD_NUMBER}-${shortCommit}"
                    env.BUILT_IMAGE_TAG = imageTag
                    echo "[DOCKER BUILD] Packaging versioned image: ${params.DOCKER_IMAGE_NAME}:${imageTag}"
                    
                    bat """
                        powershell -ExecutionPolicy Bypass -File scripts/build-docker.ps1 -ImageName "${params.DOCKER_IMAGE_NAME}" -ImageTag "${imageTag}" -AdditionalTags "latest,v0.1.0" -Dockerfile Dockerfile
                    """
                }
            }
        }

        stage('Deploy Docker Container') {
            when {
                expression { return params.DEPLOY_DOCKER == true }
            }
            steps {
                echo '=== Stage 8: Deploy Container & Verify Continuous Deployment ==='
                script {
                    def forbiddenList = env.FORBIDDEN_PORTS.split(',')
                    if (forbiddenList.contains(params.DOCKER_HOST_PORT.trim())) {
                        error("[CRITICAL PORT VIOLATION] Docker host port ${params.DOCKER_HOST_PORT} is in forbidden list (${env.FORBIDDEN_PORTS}). Aborting container deployment.")
                    }

                    def tagToDeploy = env.BUILT_IMAGE_TAG ?: "latest"
                    echo "[DOCKER DEPLOY] Deploying container '${params.DOCKER_CONTAINER_NAME}' on host port ${params.DOCKER_HOST_PORT} using tag '${tagToDeploy}'"

                    def deployStatus = bat(
                        script: """powershell -ExecutionPolicy Bypass -File scripts/deploy-docker.ps1 -ImageName "${params.DOCKER_IMAGE_NAME}" -ImageTag "${tagToDeploy}" -ContainerName "${params.DOCKER_CONTAINER_NAME}" -HostPort ${params.DOCKER_HOST_PORT} -ContainerPort 8080 -StateFile "deployment-state.json" -TimeoutSeconds 90""",
                        returnStatus: true
                    )

                    if (deployStatus != 0) {
                        error("[DOCKER DEPLOYMENT FAILED] Container continuous deployment failed health verification on port ${params.DOCKER_HOST_PORT}.")
                    }

                    echo "[DOCKER DEPLOYMENT SUCCESS] Container '${params.DOCKER_CONTAINER_NAME}' verified UP and healthy on http://localhost:${params.DOCKER_HOST_PORT}/actuator/health"
                }
            }
        }

        stage('Publish Image to Registry') {
            when {
                expression { return params.PUSH_DOCKER_HUB == true }
            }
            steps {
                echo '=== Stage 9: Publish Image to Docker Hub Registry ==='
                script {
                    echo "[REGISTRY PUBLISH] Evaluating Docker Hub publishing credentials..."
                    try {
                        withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                            bat '''
                                echo Logging in to Docker Hub...
                                echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                            '''
                            def tagToPush = env.BUILT_IMAGE_TAG ?: "latest"
                            bat """
                                docker push ${params.DOCKER_IMAGE_NAME}:${tagToPush}
                                docker push ${params.DOCKER_IMAGE_NAME}:latest
                            """
                            echo "[REGISTRY SUCCESS] Successfully published ${params.DOCKER_IMAGE_NAME}:${tagToPush} to Docker Hub."
                        }
                    } catch (Exception e) {
                        echo "=========================================================================="
                        echo "[REGISTRY NOTICE: LOCAL IMAGE WORKFLOW ACTIVE]"
                        echo "Docker Hub credentials ('docker-hub-credentials') not found or push skipped."
                        echo "Image was built, tagged, and deployed locally as: ${params.DOCKER_IMAGE_NAME}:${env.BUILT_IMAGE_TAG ?: 'latest'}"
                        echo "To enable remote registry publishing in production, add Docker Hub credentials"
                        echo "with ID 'docker-hub-credentials' in Jenkins Credentials manager."
                        echo "=========================================================================="
                    }
                }
            }
        }
    }

    post {
        always {
            echo '========================================================'
            echo 'Publishing test reports & archiving diagnostics...'
            echo '========================================================'
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/screenshots/*.png,target/screenshots/*.html,target/surefire-reports/*.txt,deployment-state.json'
            echo "Build URL: ${env.BUILD_URL}"
            echo '========================================================'
        }
        success {
            echo 'SUCCESS: All pipeline stages, quality gates, and container deployments passed successfully.'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/*.war,target/*.jar,deployment-state.json', fingerprint: true
        }
        failure {
            echo 'FAILURE: Quality gate violation or pipeline execution error encountered.'
        }
    }
}
