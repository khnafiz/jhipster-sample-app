pipeline {
    agent any
    stages {
        stage('build') {
            when {
                branch 'master'
            }
            steps {
                sh './mvnw -Pprod clean package -DskipTests'
            }
        }
    }
}
