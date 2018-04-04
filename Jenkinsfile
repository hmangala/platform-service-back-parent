/**
 * Copyright (C) Davinta Technologies 2017. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Davinta Technologies. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms and conditions
 * entered into with Davinta Technologies.
 */

pipeline {

	agent { label 'aeus-deploy' }

	options {
		buildDiscarder(logRotator(numToKeepStr: '10'))
		timeout(time: 30, unit: 'MINUTES')
		timestamps()
	}

	triggers { pollSCM('H 2,14 * * *') }

	parameters {
		string(name: 'DEPLOY_USER', defaultValue: 'ciuser', description: 'Please provide the password-less login enabled deployment user?')
		string(name: 'DEPLOY_HOST', defaultValue: '172.19.229.99', description: 'Please provide the server for deployment?')
	}

	stages {
		stage ('Checkout') {
			steps {
				script {
					if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME.startsWith('release-')) {
						echo 'Development buils not allowed on master or release branch!'
						sh 'exit 1'
					}
				}
				checkout scm
			}
		}

		stage('Build') {
			steps {
				sh './gradlew clean build distRpm $AEUS_JENKINS_BUILD_OPTS --refresh-dependencies --info --stacktrace'
			}
		}

		stage('Analyze') {
			steps {
				withCredentials([usernamePassword(credentialsId: 'SONAR_CIUSER', usernameVariable: 'SONAR_USERNAME', passwordVariable: 'SONAR_PASSWORD')]) {
					sh './gradlew sonarqube $AEUS_JENKINS_ANALYZE_OPTS -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_USERNAME -Dsonar.password=$SONAR_PASSWORD --info --stacktrace'
				}
			}
		}

		stage('Publish') {
			steps {
				withCredentials([usernamePassword(credentialsId: 'ARCHIVA_CIUSER', usernameVariable: 'ARCHIVA_USERNAME', passwordVariable: 'ARCHIVA_PASSWORD')]) {
					sh './gradlew publish $AEUS_JENKINS_PUBLISH_OPTS -DmavenUsername=$ARCHIVA_USERNAME -DmavenPassword=$ARCHIVA_PASSWORD -DsnapshotRepositoryUrl=$MAVEN_SNAPSHOT_REPOSITORY -DreleaseRepositoryUrl=$MAVEN_RELEASE_REPOSITORY --info --stacktrace'
				}
			}
		}

		stage('Deploy') {
			steps {
				script {
					def rpmFiles = findFiles(glob: '**/*.rpm')
					def rpmFile = rpmFiles[0]
					def rpmFileName = rpmFiles[0].name
					def rpmFileNameArray = rpmFileName.split("-")
					def rpmFileNamePrefix = rpmFileNameArray[0] + '-' + rpmFileNameArray[1] + '-' + rpmFileNameArray[2]

					sh "ssh -tt ${params.DEPLOY_USER}@${params.DEPLOY_HOST} 'sudo mkdir -p -m 777 /platform/deployment/${rpmFileNamePrefix}'"
					sh """ssh -tt ${params.DEPLOY_USER}@${params.DEPLOY_HOST} 'sudo find /platform/deployment/${rpmFileNamePrefix} -type f -mtime +7 -name "${rpmFileNamePrefix}*.rpm" -delete'"""
					sh "scp ${rpmFile} ${params.DEPLOY_USER}@${params.DEPLOY_HOST}:/platform/deployment/${rpmFileNamePrefix}"
					sh "ssh -tt ${params.DEPLOY_USER}@${params.DEPLOY_HOST} 'sudo rpm -Uvh --force /platform/deployment/${rpmFileNamePrefix}/${rpmFileName}'"
				}
			}
		}
	}

	post {
		success {
			emailext (recipientProviders: [[$class: 'RequesterRecipientProvider'], [$class: 'DevelopersRecipientProvider']], to: "DL-DAV-TECH-CICD-NOTIFY@davinta.com DL-DAV-TECH-AEUS-DEV@davinta.com DL-DAV-TECH-AEUS-QE@davinta.com", subject:"BUILD & DEPLOYMENT SUCCESS: ${currentBuild.fullDisplayName}", body: "Build & Deployment Successful! New Build Deployed to ${params.DEPLOY_HOST} with following changes: ${currentBuild.absoluteUrl}changes. Reports Attached. Please review the reports and take necessary actions.")
			cleanWs()
		}

		failure {
			emailext (recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']], to: "DL-DAV-TECH-CICD-NOTIFY@davinta.com", subject:"BUILD & DEPLOYMENT FAILURE: ${currentBuild.fullDisplayName}", body: "Build & Deployment Failed! Your commits is suspected to have caused the build failure. Please go to ${BUILD_URL} for details and resolve the build failure at the earliest.", attachLog: true, compressLog: true)
			cleanWs()
		}

		aborted {
			emailext (recipientProviders: [[$class: 'RequesterRecipientProvider'], [$class: 'DevelopersRecipientProvider']], subject:"BUILD & DEPLOYMENT ABORTED: ${currentBuild.fullDisplayName}", body: "Build & Deployment Aborted! Please go to ${BUILD_URL} and verify the build.", attachLog: false, compressLog: false)
			cleanWs()
		}
	}

}
