pipeline {
    agent none
    environment {
        PLUGIN_NAME = "com.cdsoftware.lirion.attendance"
        PLUGIN_NAME2 = "com.cdsoftware.lirion.payroll"
        PLUGIN_NAME3 = "org.globalqss.idempiere.LCO.detailednames"
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
                dir ('v10'){
                    git branch: 'master', url: 'https://github.com/globalqss/globalqss-idempiere-lco.git'
             
                }
                dir ('v10p'){
                    
                    checkout scmGit(branches: [[name: '*/10.0.0']], extensions: [], userRemoteConfigs: [[credentialsId: 'bitbucket', url: 'git@bitbucket.org:cdsoftware/com.cdsoftware.lirion.payroll.git']])
                
                }
                dir('target-platform') {
                    git branch: '10', url: 'https://github.com/ingeint/idempiere-target-platform-plugin.git'
					sh './plugin-builder build ../${PLUGIN_NAME} ../${PLUGIN_NAME}.test ../v10p/${PLUGIN_NAME2} ../v10/${PLUGIN_NAME3}'
                    archiveArtifacts artifacts: "target/${PLUGIN_NAME}-${IDEMPIERE_VERSION}.${BUILD_NUMBER}.jar", fingerprint: true
                    sh 'rm -rf target ../${PLUGIN_NAME}/target ../${PLUGIN_NAME}.test/target'
                }
            }
        }
    }
}