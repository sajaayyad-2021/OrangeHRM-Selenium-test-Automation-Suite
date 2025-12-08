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
}
