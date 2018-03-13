pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh './mvnw -Pprod clean package -DskipTests'
            }
        }
    }
}
