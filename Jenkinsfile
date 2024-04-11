pipeline {
    agent none
    environment {
        PLUGIN_NAME = "com.cdsoftware.lirion.attendance"
        PLUGIN_NAME2 = "com.cdsoftware.lirion.payroll"
        IDEMPIERE_VERSION = "10.0.0"
        C_I = 'ATBBW2LZBmWB89yV5uNj3fxB4uGy01902518'
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
                dir ('v10'){
                    git branch: '10.0.0', credentialsId: '${C_I}' ,url 'https://Carl0jgr@bitbucket.org/cdsoftware/com.cdsoftware.lirion.payroll.git'

        
                }
                dir('target-platform') {
                    git branch: '10', url: 'https://github.com/ingeint/idempiere-target-platform-plugin.git'
					sh './plugin-builder build ../${PLUGIN_NAME} ../${PLUGIN_NAME}.test ../v10/${PLUGIN_NAME2}'
                    archiveArtifacts artifacts: "target/${PLUGIN_NAME}-${IDEMPIERE_VERSION}.${BUILD_NUMBER}.jar", fingerprint: true
                    sh 'rm -rf target ../${PLUGIN_NAME}/target ../${PLUGIN_NAME}.test/target'
                }
            }
        }
    }
}