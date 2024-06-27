pipeline {
  environment {
   	 PROJECT = "mobitel_pipeline"
 	   APP_NAME = "msmsent-web-backend"
     BRANCH_NAME = "staging"
     CIR = "stg-docker-reg.mobitel.lk"
      CIR_USER = 'mobitel'
      CIR_PW = credentials('cir-pw')
      KUB_NAMESPACE = "msmsent-rest"
      IMAGE_TAG = "${CIR}/${PROJECT}/${APP_NAME}:${BRANCH_NAME}.${env.BUILD_NUMBER}"
                }
    agent none 
    stages {  
      stage('Build & test') {
        agent {
                docker {
           image 'maven:3.9.4-amazoncorretto-20-al2023'
           args '-v /root/.m2:/root/.m2'
     }
            }
         steps {
            
            sh "mvn -Dmaven.test.skip=true clean install -X"
            //sh "mvn -U clean install"

          }
        }  
		stage('Building & Deploy Image') {
      agent any
		    steps{
          sh 'docker login -u ${CIR_USER} -p ${CIR_PW} ${CIR}'
          sh 'mkdir -p dockerImage/'
		     sh 'cp Dockerfile dockerImage/'
         sh 'cp target/*.jar dockerImage/'
		     sh 'docker build --tag=${IMAGE_TAG} dockerImage/.'
					sh 'docker push ${IMAGE_TAG}'
          sh 'docker image rm ${IMAGE_TAG}'
          sh 'rm -rf dockerImage/'
        }
        }
        stage('Deploy cluster') {
              agent {
                 docker {
                       image 'inovadockerimages/cicdtools:latest' 
                         args '-v /root/.cert:/root/.cert'   
                        }
                    }
             steps {
               sh 'mkdir -p /root/.kube/'
               sh 'cp /root/.cert/stg/config /root/.kube/'
                sh 'kubectl set image deployment/${APP_NAME}  ${APP_NAME}=${IMAGE_TAG}  --record -n ${KUB_NAMESPACE}'
                  }
        }
            }
            post {
            success {
              mail to: 'mobiteldev@mobitel.lk',
                         subject: "${env.JOB_NAME} - Build  ${env.BUILD_NUMBER} - Success!",
                       body: """${env.JOB_NAME} - Build  ${env.BUILD_NUMBER} - Success:
                             Check console output at ${env.BUILD_URL} to view the results."""
                    }
            failure {
                   mail to: "jenkins.notification@mobitel.lk",
                   cc: 'mobiteldev@mobitel.lk',
                       subject: "${env.JOB_NAME} - Build  ${env.BUILD_NUMBER} - Failed!",
                        body: """${env.JOB_NAME} - Build  ${env.BUILD_NUMBER} - Failed:
                             Check console output at ${env.BUILD_URL} to view the results."""
                  }
             }
            
}
