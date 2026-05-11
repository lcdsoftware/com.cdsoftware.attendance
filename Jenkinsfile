pipeline {
    agent none
    environment {
        PLUGIN_NAME = "com.cdsoftware.attendance"
        PLUGIN_NAME2 = "com.cdsoftware.payroll"
        PLUGIN_NAME3 = "com.cdsoftware.base"
        PLUGIN_NAME4 = "org.globalqss.idempiere.LCO.detailednames"
        PLUGIN_NAME5 = "com.cdsoftware.location"
        PLUGIN_NAME6 = "com.cdsoftware.pluginconfig" 
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
                             
                dir ('d2'){
                    checkout scmGit(branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@bitbucket.org:cdsoftware/com.cdsoftware.payroll.git']])                           
                }
                dir ('d3'){
                    checkout scmGit(branches: [[name: '*/10.0.0']], extensions: [], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@bitbucket.org:cdsoftware/com.cdsoftware.base.git']])             
                }  
                dir ('d4'){
                    git branch: '10.0.0', url: 'https://github.com/egil0902/globalqss-idempiere-lco.git'             
                }  
                 dir ('d5'){
                    checkout scmGit(branches: [[name: '*/10.0.0']], extensions: [], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@bitbucket.org:cdsoftware/com.cdsoftware.location.git']])             
                }
                 dir ('d6'){
                    checkout scmGit(branches: [[name: '*/10.0.0']], extensions: [], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@bitbucket.org:cdsoftware/com.cdsoftware.pluginconfig.git']])             
                } 
                dir('target-platform') {
                    git branch: '10', url: 'https://github.com/ingeint/idempiere-target-platform-plugin.git'
					sh './plugin-builder build ../${PLUGIN_NAME} ../${PLUGIN_NAME}.test ../d2/${PLUGIN_NAME2} ../d3/${PLUGIN_NAME3} ../d4/${PLUGIN_NAME4} ../d5/${PLUGIN_NAME5} ../d6/${PLUGIN_NAME6}'
                    archiveArtifacts artifacts: "target/${PLUGIN_NAME}-${IDEMPIERE_VERSION}.${BUILD_NUMBER}.jar", fingerprint: true
                    sh 'rm -rf target ../${PLUGIN_NAME}/target ../${PLUGIN_NAME}.test/target'
                }
            }
        }
    }
}
