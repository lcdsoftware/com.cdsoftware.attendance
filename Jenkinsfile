pipeline {
    agent none
    environment {
        PLUGIN_NAME = "com.cdsoftware.lirion.attendance"
        PLUGIN_NAME2 = "com.cdsoftware.lirion.payroll"
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
                    
                    withCredentials([sshUserPrivateKey(credentialsId: 'bitbucket', keyFileVariable: 'SSH_PRIVATE_KEY')]) {
                        sh 'echo "$SSH_PRIVATE_KEY" > ssh_key && chmod 600 ssh_key && ssh-agent bash -c "ssh-add ssh_key && git clone git@bitbucket.org:cdsoftware/com.cdsoftware.lirion.payroll.git"'
                    }        
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