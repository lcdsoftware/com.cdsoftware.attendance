pipeline {
    agent any 
    environment {
        PLUGIN_NAME = "com.cdsoftware.attendance"
        PLUGIN_NAME2 = "com.cdsoftware.payroll"
        PLUGIN_NAME3 = "com.cdsoftware.base"
        PLUGIN_NAME4 = "org.globalqss.idempiere.LCO.detailednames"
        PLUGIN_NAME5 = "com.cdsoftware.location"
        PLUGIN_NAME6 = "com.cdsoftware.pluginconfig" 
        IDEMPIERE_VERSION = "12.0.0"
    }
    stages {
        stage('Checkout Dependencies') {
            steps {
                // Todas las descargas ocurren en el anfitrión de Jenkins donde la llave SSH funciona perfectamente
                dir ('d2') {
                    checkout scmGit(branches: [[name: '*/12.0.0']], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@github.com:lcdsoftware/com.cdsoftware.payroll.git']])                            
                }
                dir ('d3') {
                    checkout scmGit(branches: [[name: '*/12.0.0']], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@github.com:lcdsoftware/com.cdsoftware.base.git']])              
                }  
                dir ('d4') {
                    checkout scmGit(branches: [[name: '*/12.0.0']], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@github.com:lcdsoftware/globalqss-idempiere-lco.git']])              
                }
                 dir ('d5') {
                    checkout scmGit(branches: [[name: '*/12.0.0']], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@github.com:lcdsoftware/com.cdsoftware.location.git']])              
                }
                 dir ('d6') {
                    checkout scmGit(branches: [[name: '*/12.0.0']], userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@github.com:lcdsoftware/com.cdsoftware.pluginconfig.git']])              
                }                  
                dir('target-platform') {
                    git branch: '12.0', url: 'https://github.com/ingeint/idempiere-target-platform-plugin.git'
                }
            }
        }
        
        stage('Compile') {
            agent {
                docker {
                    image 'carl0jgr/idempiere-source-builder:12'
                    args '--entrypoint=\'\' -u root:root -v /var/jenkins_home/.m2:/root/.m2'
                    reuseNode true // Le dice a Docker que use los archivos que acabamos de descargar arriba
                }
            }
            steps {
                dir('target-platform') {
                    sh './plugin-builder build ../${PLUGIN_NAME}  ../d2/${PLUGIN_NAME2} ../d3/${PLUGIN_NAME3} ../d4/${PLUGIN_NAME4} ../d5/${PLUGIN_NAME5} ../d6/${PLUGIN_NAME6}'
                    archiveArtifacts artifacts: "target/${PLUGIN_NAME}-${IDEMPIERE_VERSION}.${BUILD_NUMBER}.jar", fingerprint: true
                    sh 'rm -rf target ../${PLUGIN_NAME}/target'
                }
            }
        }
    }
}
