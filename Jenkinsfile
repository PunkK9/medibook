pipeline {
    agent any

    tools { maven 'Maven3'; jdk 'JDK17' }   // names configured in Manage Jenkins > Tools

    options { disableConcurrentBuilds() }
triggers { pollSCM("H/2 * * * *") }

    environment {
        IMAGE = "medibook"
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build') {
            steps { sh 'mvn -B clean compile' }
        }
        stage('Test') {                       // Component 1 - Option A
            steps { sh 'mvn -B test' }
            post { always { junit 'target/surefire-reports/*.xml' } }
        }
        stage('Package') {
            steps { sh 'mvn -B package -DskipTests' }
        }
        stage('Archive') {
            steps { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true }
        }
        stage('Docker Build') {               // Component 1 - Option B (tag = build id + git sha)
            steps {
                script {
                    env.GIT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
                sh 'docker build -t ${IMAGE}:${BUILD_NUMBER}-${GIT_SHA} .'
            }
        }
    }

    post {
        success { echo "Built ${IMAGE}:${BUILD_NUMBER}-${GIT_SHA}" }
        failure { echo 'Build failed' }
    }
}
