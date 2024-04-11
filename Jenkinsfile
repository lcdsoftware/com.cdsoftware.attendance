pipeline {
    agent none
    environment {
        PLUGIN_NAME = "com.cdsoftware.lirion.attendance"
        IDEMPIERE_VERSION = "10.0.0"
    }
    stages {
        stage('Compile') {
            agent {
                docker {
                    image 'idempiereofficial/idempiere:source-release-10.0'
                    args '-u root:root'               
                  }
            }
            steps {
                dir('target-platform') {
                    git branch: '10', url: 'https://github.com/ingeint/idempiere-target-platform-plugin.git'
					sh './plugin-builder build ../${PLUGIN_NAME} ../${PLUGIN_NAME}.test'
                    archiveArtifacts artifacts: "target/${PLUGIN_NAME}-${IDEMPIERE_VERSION}.${BUILD_NUMBER}.jar", fingerprint: true
                    sh 'rm -rf target ../${PLUGIN_NAME}/target ../${PLUGIN_NAME}.test/target'
                }
            }
        }
    }
}