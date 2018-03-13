pipeline {
    agent any
    stages {
        stage('build') {
            when {
                branch 'develop'
            }
            steps {
                sh './mvnw -Pprod clean package -DskipTests'
            }
        }
    }
}
