pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/sajaayyad-2021/OrangeHRM-Selenium-test-Automation-Suite.git'
            }
        }

        stage('Verify Java') {
            steps {
                bat 'java -version'
            }
        }

        stage('Run Automation JAR') {
            steps {
                bat '''
                java -jar automation.jar ^
                -out artifacts ^
                -browser chrome ^
                -testNmaes_login ALL ^
                -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
                '''
            }
        }
    }

    post {
        always {
            emailext(
                subject: "OrangeHRM Automation Result - Build #${BUILD_NUMBER}",
                body: """
Build Status: ${currentBuild.currentResult}

Project: OrangeHRM Automation
Build Number: ${BUILD_NUMBER}

Jenkins Build Link:
${BUILD_URL}

Extent Report Attached.
""",
                to: "sajayaser085@gmail.com",
                attachmentsPattern: "test-output/Reports/*.html",
                mimeType: 'text/html'
            )
        }
    }
}
