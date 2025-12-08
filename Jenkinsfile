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
      
        archiveArtifacts artifacts: 'test-output/Reports/*.html', fingerprint: true
    }

    success {
        emailext(
            subject: " OrangeHRM Regression PASSED",
            to: "sajayaser085@gmail.com",
            body: "Hi,\n\nAll tests passed successfully.\n\nAttached is the latest Extent Report.\n\nRegards,\nJenkins",
            attachmentsPattern: 'test-output/Reports/*.html'
        )
    }

    failure {
        emailext(
            subject: " OrangeHRM Regression FAILED",
            to: "sajayaser085@gmail.com",
            body: "Hi,\n\nSome tests failed. Please check the attached Extent Report.\n\nRegards,\nJenkins",
            attachmentsPattern: 'test-output/Reports/*.html'
        )
    }
}

}
