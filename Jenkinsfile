pipeline {
    agent none
    options {
        checkoutToSubdirectory('argo.mon.status.api')
        newContainerPerStage()
    }
    environment {
        PROJECT_DIR = 'argo.mon.status.api'
        GH_USER = 'newgrnetci'
        GH_EMAIL = '<argo@grnet.gr>'
    }
    stages {
        stage('Argo Mon Status API Packaging & Testing') {
            agent {
                docker {
                    image 'argo.registry:5000/rocky9-java17-mvn3.9.9:latest'
                    args '-v $HOME/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock -u root:root'
                }
            }
            steps {
                echo 'Argo Mon Status Packaging & Testing'
                withCredentials([usernamePassword(
                    credentialsId: 'newgrnetci-read-maven-packages',
                    usernameVariable: 'GHPKG_USERNAME',
                    passwordVariable: 'GHPKG_TOKEN'
                )]) {
                    sh """
                    mkdir -p ~/.m2
                    cat > ~/.m2/settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>\${GHPKG_USERNAME}</username>
      <password>\${GHPKG_TOKEN}</password>
    </server>
  </servers>
</settings>
EOF
                    cd ${WORKSPACE}/${PROJECT_DIR}
                    mvn clean package -Dquarkus.package.type=uber-jar -U
                    """
                }
                junit '**/target/surefire-reports/*.xml'
                archiveArtifacts artifacts: '**/api/target/*.jar'
                step([ $class: 'JacocoPublisher' ])
            }
            post {
                always {
                    cleanWs()
                }
            }
        }
    }
    post {
        success {
            script {
                if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'devel') {
                    slackSend(message: ":rocket: New version for <$BUILD_URL|$PROJECT_DIR>:$BRANCH_NAME Job: $JOB_NAME !")
                }
            }
        }
        failure {
            script {
                if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'devel') {
                    slackSend(message: ":rain_cloud: Build Failed for <$BUILD_URL|$PROJECT_DIR>:$BRANCH_NAME Job: $JOB_NAME")
                }
            }
        }
    }
}
